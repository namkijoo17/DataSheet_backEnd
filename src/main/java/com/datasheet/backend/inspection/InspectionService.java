package com.datasheet.backend.inspection;

import com.datasheet.backend.common.BusinessException;
import com.datasheet.backend.datasheet.Datasheet;
import com.datasheet.backend.datasheet.DatasheetItem;
import com.datasheet.backend.datasheet.DatasheetRepository;
import com.datasheet.backend.file.InspectionFileService;
import com.datasheet.backend.inspection.dto.InspectionAnswerRequest;
import com.datasheet.backend.inspection.dto.InspectionBasicRequest;
import com.datasheet.backend.inspection.dto.InspectionDetailResponse;
import com.datasheet.backend.inspection.dto.InspectionStartRequest;
import com.datasheet.backend.inspection.dto.InspectionSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionService {

	private final InspectionRepository inspectionRepository;
	private final DatasheetRepository datasheetRepository;
	private final InspectionFileService inspectionFileService;

	@Transactional
	public InspectionDetailResponse start(Long userId, InspectionStartRequest request) {
		if (request.getDatasheetId() == null) {
			throw new BusinessException("VALIDATION_ERROR", "데이터시트를 선택해 주세요.", HttpStatus.BAD_REQUEST);
		}
		Datasheet datasheet = datasheetRepository.findById(request.getDatasheetId())
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));

		Inspection inspection = new Inspection(datasheet, userId, request.getMode());
		applyBasic(inspection, request.getMode(), request.getBasic());
		return InspectionDetailResponse.from(inspectionRepository.save(inspection));
	}

	/**
	 * 기본정보·모드를 교체한다. 모드가 바뀌면 입력된 답변을 모두 비우고,
	 * 모드가 같으면(기번·검사자 등 기본정보만 수정) 답변은 유지한다.
	 */
	@Transactional
	public InspectionDetailResponse restart(Long userId, Long id, InspectionStartRequest request) {
		Inspection inspection = requireEditable(userId, id);
		if (inspection.getMode() != request.getMode()) {
			inspection.clearAnswers();
		}
		applyBasic(inspection, request.getMode(), request.getBasic());
		return InspectionDetailResponse.from(inspection);
	}

	@Transactional(readOnly = true)
	public List<InspectionSummaryResponse> list(Long userId, InspectionStatus status) {
		return inspectionRepository.findByUserAndStatus(userId, status).stream()
				.map(this::toSummary)
				.toList();
	}

	@Transactional(readOnly = true)
	public InspectionDetailResponse detail(Long userId, Long id) {
		return InspectionDetailResponse.from(requireOwned(userId, id));
	}

	/** 항목 1개 저장. 내용이 모두 비어 있으면 기존 답변을 삭제한다. */
	@Transactional
	public void saveAnswer(Long userId, Long id, Long itemId, InspectionAnswerRequest request) {
		Inspection inspection = requireEditable(userId, id);
		InspectionAnswer answer = inspection.findAnswer(itemId);
		boolean empty = isEmpty(request);

		if (empty) {
			if (answer != null) {
				inspection.removeAnswer(answer);
				inspection.touch();
			}
			return;
		}

		if (answer == null) {
			answer = inspection.addAnswer(itemId);
		}
		boolean skipped = request.isSkipped();
		answer.apply(
				skipped,
				skipped ? null : blankToNull(request.getValue()),
				skipped ? null : request.getSlotIndex(),
				skipped ? null : request.getJudgment(),
				skipped ? null : request.getSlotValues(),
				skipped ? null : request.getNgAction(),
				request.getNgOccurredAt()
		);
		inspection.touch();
	}

	/** 검사를 완료하고, 입력값이 채워진 결과 PDF 를 파일 보관함에 저장한다. */
	@Transactional
	public InspectionDetailResponse complete(
			Long userId,
			Long id,
			MultipartFile file,
			String fileName,
			int okCount,
			int ngCount,
			int skipCount
	) {
		Inspection inspection = requireEditable(userId, id);
		String storedName = inspectionFileService.storePdf(file);
		try {
			inspection.complete(
					Math.max(okCount, 0),
					Math.max(ngCount, 0),
					Math.max(skipCount, 0),
					sanitizeFileName(fileName, id),
					storedName
			);
			inspectionRepository.flush();
			return InspectionDetailResponse.from(inspection);
		} catch (RuntimeException ex) {
			inspectionFileService.deleteIfExists(storedName);
			throw ex;
		}
	}

	/** 완료된 검사의 결과 PDF (본인 검사만) */
	@Transactional(readOnly = true)
	public ResultFile loadResultFile(Long userId, Long id) {
		Inspection inspection = requireOwned(userId, id);
		if (inspection.getStoredFileName() == null) {
			throw new BusinessException(
					"FILE_NOT_FOUND",
					"저장된 결과 파일이 없습니다.",
					HttpStatus.NOT_FOUND
			);
		}
		Resource resource = inspectionFileService.load(inspection.getStoredFileName());
		return new ResultFile(resource, inspection.getFileName());
	}

	public record ResultFile(Resource resource, String fileName) {
	}

	@Transactional
	public void discard(Long userId, Long id) {
		Inspection inspection = requireEditable(userId, id);
		inspectionRepository.delete(inspection);
	}

	private static String sanitizeFileName(String fileName, Long id) {
		String name = fileName == null ? "" : fileName.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
		if (name.isEmpty()) {
			name = "inspection_" + id;
		}
		if (!name.toLowerCase().endsWith(".pdf")) {
			name = name + ".pdf";
		}
		return name.length() > 255 ? name.substring(name.length() - 255) : name;
	}

	private void applyBasic(Inspection inspection, InspectionMode mode, InspectionBasicRequest basic) {
		inspection.updateBasic(
				mode,
				blankToNull(basic.getSerialNo()),
				blankToNull(basic.getUnit()),
				blankToNull(basic.getLine()),
				basic.getInspectDate(),
				blankToNull(basic.getInspector()),
				blankToNull(basic.getInspectorChangeReason()),
				blankToNull(basic.getTemperature()),
				blankToNull(basic.getHumidity()),
				basic.getParts()
		);
	}

	private Inspection requireOwned(Long userId, Long id) {
		return inspectionRepository.findOwnedWithAnswers(id, userId)
				.orElseThrow(() -> new BusinessException(
						"INSPECTION_NOT_FOUND",
						"검사를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
	}

	private Inspection requireEditable(Long userId, Long id) {
		Inspection inspection = requireOwned(userId, id);
		if (!inspection.isEditable()) {
			throw new BusinessException(
					"INSPECTION_NOT_EDITABLE",
					"이미 완료된 검사는 수정할 수 없습니다.",
					HttpStatus.CONFLICT
			);
		}
		return inspection;
	}

	private InspectionSummaryResponse toSummary(Inspection inspection) {
		Set<Long> inspectionItemIds = inspection.getDatasheet().getItems().stream()
				.filter(InspectionService::isInspectionItem)
				.map(DatasheetItem::getId)
				.collect(Collectors.toSet());
		int answered = (int) inspection.getAnswers().stream()
				.filter(answer -> inspectionItemIds.contains(answer.getDatasheetItemId()))
				.filter(InspectionAnswer::hasContent)
				.count();

		return InspectionSummaryResponse.builder()
				.id(inspection.getId())
				.datasheetId(inspection.getDatasheet().getId())
				.datasheetName(inspection.getDatasheet().getName())
				.serialNo(inspection.getSerialNo())
				.unit(inspection.getUnit())
				.inspector(inspection.getInspector())
				.mode(inspection.getMode().name())
				.status(inspection.getStatus().name())
				.answeredCount(answered)
				.totalCount(inspectionItemIds.size())
				.result(inspection.getResult())
				.okCount(inspection.getOkCount())
				.ngCount(inspection.getNgCount())
				.skipCount(inspection.getSkipCount())
				.fileName(inspection.getFileName())
				.updatedAt(inspection.getUpdatedAt())
				.completedAt(inspection.getCompletedAt())
				.build();
	}

	/** 프론트(inspection-sequence.ts)와 같은 규칙 */
	private static boolean isInspectionItem(DatasheetItem item) {
		if ("검사 항목".equals(item.getGroupName())) {
			return true;
		}
		if ("기본정보".equals(item.getGroupName())) {
			return false;
		}
		return item.isInspectionItem();
	}

	private static boolean isEmpty(InspectionAnswerRequest request) {
		if (request.isSkipped()) {
			return false;
		}
		if (request.getValue() != null && !request.getValue().isBlank()) {
			return false;
		}
		if (request.getSlotIndex() != null || request.getJudgment() != null) {
			return false;
		}
		return request.getSlotValues() == null
				|| request.getSlotValues().values().stream().allMatch(v -> v == null || v.isBlank());
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
