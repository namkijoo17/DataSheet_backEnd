package com.datasheet.backend.inspection;

import com.datasheet.backend.common.BusinessException;
import com.datasheet.backend.datasheet.Datasheet;
import com.datasheet.backend.datasheet.DatasheetItem;
import com.datasheet.backend.datasheet.DatasheetRepository;
import com.datasheet.backend.inspection.dto.InspectionAnswerRequest;
import com.datasheet.backend.inspection.dto.InspectionBasicRequest;
import com.datasheet.backend.inspection.dto.InspectionDetailResponse;
import com.datasheet.backend.inspection.dto.InspectionStartRequest;
import com.datasheet.backend.inspection.dto.InspectionSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InspectionService {

	private final InspectionRepository inspectionRepository;
	private final DatasheetRepository datasheetRepository;

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
				skipped ? null : request.getSlotValues()
		);
		inspection.touch();
	}

	@Transactional
	public InspectionDetailResponse complete(Long userId, Long id) {
		Inspection inspection = requireEditable(userId, id);
		inspection.complete();
		return InspectionDetailResponse.from(inspection);
	}

	@Transactional
	public void discard(Long userId, Long id) {
		Inspection inspection = requireEditable(userId, id);
		inspectionRepository.delete(inspection);
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
				.mode(inspection.getMode().name())
				.status(inspection.getStatus().name())
				.answeredCount(answered)
				.totalCount(inspectionItemIds.size())
				.updatedAt(inspection.getUpdatedAt())
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
