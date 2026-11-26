package com.datasheet.backend.datasheet;

import com.datasheet.backend.common.BusinessException;
import com.datasheet.backend.datasheet.dto.DatasheetCreateRequest;
import com.datasheet.backend.datasheet.dto.DatasheetDetailResponse;
import com.datasheet.backend.datasheet.dto.DatasheetItemResponse;
import com.datasheet.backend.datasheet.dto.DatasheetItemRangeRuleRequest;
import com.datasheet.backend.datasheet.dto.DatasheetItemReorderRequest;
import com.datasheet.backend.datasheet.dto.DatasheetItemSlotRequest;
import com.datasheet.backend.datasheet.dto.DatasheetItemUpsertRequest;
import com.datasheet.backend.datasheet.dto.DatasheetPositionRequest;
import com.datasheet.backend.datasheet.dto.DatasheetSummaryResponse;
import com.datasheet.backend.file.DatasheetFileService;
import com.datasheet.backend.inspection.InspectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatasheetService {

	private final DatasheetRepository datasheetRepository;
	private final DatasheetFileService datasheetFileService;
	private final InspectionRepository inspectionRepository;

	@Transactional(readOnly = true)
	public List<DatasheetSummaryResponse> listActive() {
		return datasheetRepository.findByActiveTrueOrderByNameAscRevisionDesc().stream()
				.map(DatasheetSummaryResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public DatasheetDetailResponse getDetail(Long id) {
		return DatasheetDetailResponse.from(requireSheetWithItems(id));
	}

	@Transactional
	public DatasheetDetailResponse create(DatasheetCreateRequest request, MultipartFile file) {
		if (request.getName() == null || request.getName().isBlank()) {
			throw new BusinessException("INVALID_NAME", "이름을 입력해주세요.", HttpStatus.BAD_REQUEST);
		}

		String name = request.getName().trim();
		int revision = request.getRevision() == null || request.getRevision() < 1
				? 1
				: request.getRevision();

		String storedFileName = datasheetFileService.storePdf(file);
		try {
			BigDecimal pageWidth = request.getPageWidth() != null
					? request.getPageWidth()
					: new BigDecimal("595.44");
			BigDecimal pageHeight = request.getPageHeight() != null
					? request.getPageHeight()
					: new BigDecimal("841.92");

			Datasheet sheet = Datasheet.builder()
					.name(name)
					.revision(revision)
					.fileName(storedFileName)
					.pageWidth(pageWidth)
					.pageHeight(pageHeight)
					.active(true)
					.build();

			datasheetRepository.saveAndFlush(sheet);
			return DatasheetDetailResponse.from(sheet);
		} catch (RuntimeException ex) {
			datasheetFileService.deleteIfExists(storedFileName);
			throw ex;
		}
	}

	@Transactional
	public void delete(Long id) {
		Datasheet sheet = datasheetRepository.findById(id)
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
		if (inspectionRepository.existsByDatasheetId(id)) {
			throw new BusinessException(
					"DATASHEET_IN_USE",
					"검사 기록이 있는 데이터시트는 삭제할 수 없습니다.",
					HttpStatus.CONFLICT
			);
		}
		String fileName = sheet.getFileName();
		datasheetRepository.delete(sheet);
		datasheetRepository.flush();
		// 다른 시트가 같은 파일을 참조하면 디스크 파일은 유지
		if (!datasheetRepository.existsByFileName(fileName)) {
			datasheetFileService.deleteIfExists(fileName);
		}
	}

	/**
	 * 기존 시트의 PDF만 교체합니다. 항목·좌표 등 메타는 유지됩니다.
	 */
	@Transactional
	public DatasheetDetailResponse replaceFile(
			Long id,
			MultipartFile file,
			BigDecimal pageWidth,
			BigDecimal pageHeight
	) {
		Datasheet sheet = datasheetRepository.findById(id)
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));

		String previousFileName = sheet.getFileName();
		String storedFileName = datasheetFileService.storePdf(file);
		try {
			sheet.replaceFile(storedFileName, pageWidth, pageHeight);
			datasheetRepository.saveAndFlush(sheet);

			if (!previousFileName.equals(storedFileName)
					&& !datasheetRepository.existsByFileName(previousFileName)) {
				datasheetFileService.deleteIfExists(previousFileName);
			}

			return getDetail(id);
		} catch (RuntimeException ex) {
			datasheetFileService.deleteIfExists(storedFileName);
			throw ex;
		}
	}

	@Transactional
	public DatasheetItemResponse createItem(Long datasheetId, DatasheetItemUpsertRequest request) {
		Datasheet sheet = requireSheetWithItems(datasheetId);
		validateSlots(request);
		validateRangeRules(sheet, null, request);

		int nextNo = sheet.getItems().stream()
				.mapToInt(DatasheetItem::getItemNo)
				.max()
				.orElse(0) + 1;
		int sortOrder = request.getSortOrder() != null
				? request.getSortOrder()
				: sheet.getItems().stream().mapToInt(DatasheetItem::getSortOrder).max().orElse(0) + 1;

		DatasheetItem item = DatasheetItem.builder()
				.itemNo(nextNo)
				.groupName(request.getGroupName())
				.label(request.getLabel())
				.inspectionItem(request.isInspectionItem())
				.array(request.isArray())
				.arraySize(resolveArraySize(request))
				.inputType(request.getInputType())
				.unit(request.getUnit())
				.decimalPlaces(request.getDecimalPlaces())
				.minValue(request.getMinValue())
				.maxValue(request.getMaxValue())
				.allowNegative(request.isAllowNegative())
				.required(request.isRequired())
				.skippable(request.isSkippable())
				.formula(request.getFormula())
				.specDescription(blankToNull(request.getSpecDescription()))
				.pdfPage(request.getPdfPage())
				.posX(request.getPosX())
				.posY(request.getPosY())
				.posWidth(request.getPosWidth())
				.posHeight(request.getPosHeight())
				.sortOrder(sortOrder)
				.build();

		replaceSlots(item, request.getSlots());
		replaceRangeRules(item, request.getRangeRules());
		sheet.addItem(item);
		datasheetRepository.saveAndFlush(sheet);
		return DatasheetItemResponse.from(item);
	}

	@Transactional
	public DatasheetItemResponse updateItem(Long datasheetId, Long itemId, DatasheetItemUpsertRequest request) {
		Datasheet sheet = requireSheetWithItems(datasheetId);
		DatasheetItem item = requireItem(sheet, itemId);
		validateSlots(request);
		validateRangeRules(sheet, itemId, request);

		int sortOrder = request.getSortOrder() != null ? request.getSortOrder() : item.getSortOrder();
		item.updateMeta(
				request.getGroupName(),
				request.getLabel(),
				request.isInspectionItem(),
				request.isArray(),
				resolveArraySize(request),
				request.getInputType(),
				request.getUnit(),
				request.getDecimalPlaces(),
				request.getMinValue(),
				request.getMaxValue(),
				request.isAllowNegative(),
				request.isRequired(),
				request.isSkippable(),
				request.getFormula(),
				blankToNull(request.getSpecDescription()),
				sortOrder
		);
		item.updatePosition(
				request.getPdfPage(),
				request.getPosX(),
				request.getPosY(),
				request.getPosWidth(),
				request.getPosHeight()
		);
		replaceSlots(item, request.getSlots());
		replaceRangeRules(item, request.getRangeRules());
		datasheetRepository.flush();
		return DatasheetItemResponse.from(item);
	}

	@Transactional
	public void deleteItem(Long datasheetId, Long itemId) {
		Datasheet sheet = requireSheetWithItems(datasheetId);
		DatasheetItem item = requireItem(sheet, itemId);
		// 이 항목을 기준으로 하는 다른 항목의 범위 규칙 제거
		for (DatasheetItem other : sheet.getItems()) {
			if (other.getId().equals(itemId)) {
				continue;
			}
			other.getRangeRules().removeIf(rule -> itemId.equals(rule.getDependsOnItemId()));
		}
		sheet.removeItem(item);
	}

	@Transactional
	public DatasheetDetailResponse reorderItems(Long datasheetId, DatasheetItemReorderRequest request) {
		Datasheet sheet = requireSheetWithItems(datasheetId);
		List<Long> orderedIds = request.getItemIds();
		// join fetch(slots+rangeRules) 시 List에 동일 항목이 중복될 수 있음
		Map<Long, DatasheetItem> byId = sheet.getItems().stream()
				.collect(Collectors.toMap(
						DatasheetItem::getId,
						Function.identity(),
						(existing, ignored) -> existing
				));

		if (orderedIds.size() != byId.size()) {
			throw new BusinessException(
					"INVALID_REORDER",
					"모든 항목 id를 순서대로 보내주세요.",
					HttpStatus.BAD_REQUEST
			);
		}

		Set<Long> seen = new HashSet<>();
		for (Long id : orderedIds) {
			if (!seen.add(id) || !byId.containsKey(id)) {
				throw new BusinessException(
						"INVALID_REORDER",
						"항목 id가 올바르지 않습니다.",
						HttpStatus.BAD_REQUEST
				);
			}
		}

		int order = 1;
		for (Long id : orderedIds) {
			byId.get(id).updateSortOrder(order++);
		}
		datasheetRepository.flush();
		return DatasheetDetailResponse.from(sheet);
	}

	@Transactional
	public DatasheetItemResponse updateItemPosition(Long datasheetId, Long itemId, DatasheetPositionRequest request) {
		Datasheet sheet = requireSheetWithItems(datasheetId);
		DatasheetItem item = requireItem(sheet, itemId);
		item.updatePosition(
				request.getPdfPage(),
				request.getPosX(),
				request.getPosY(),
				request.getPosWidth(),
				request.getPosHeight()
		);
		return DatasheetItemResponse.from(item);
	}

	@Transactional
	public DatasheetItemResponse updateSlotPosition(
			Long datasheetId,
			Long itemId,
			Long slotId,
			DatasheetPositionRequest request
	) {
		Datasheet sheet = requireSheetWithItems(datasheetId);
		DatasheetItem item = requireItem(sheet, itemId);
		DatasheetItemSlot slot = item.getSlots().stream()
				.filter(s -> slotId.equals(s.getId()))
				.findFirst()
				.orElseThrow(() -> new BusinessException(
						"SLOT_NOT_FOUND",
						"슬롯을 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
		slot.updatePosition(
				request.getPdfPage(),
				request.getPosX(),
				request.getPosY(),
				request.getPosWidth(),
				request.getPosHeight()
		);
		return DatasheetItemResponse.from(item);
	}

	@Transactional(readOnly = true)
	public Resource loadFile(Long id) {
		Datasheet datasheet = datasheetRepository.findById(id)
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
		return datasheetFileService.loadAsResource(datasheet.getFileName());
	}

	@Transactional(readOnly = true)
	public String contentType(Long id) {
		Datasheet datasheet = datasheetRepository.findById(id)
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
		return datasheetFileService.detectContentType(datasheet.getFileName());
	}

	@Transactional(readOnly = true)
	public String fileName(Long id) {
		return datasheetRepository.findById(id)
				.map(Datasheet::getFileName)
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
	}

	private Datasheet requireSheetWithItems(Long id) {
		return datasheetRepository.findByIdWithItems(id)
				.orElseThrow(() -> new BusinessException(
						"DATASHEET_NOT_FOUND",
						"데이터시트를 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
	}

	private DatasheetItem requireItem(Datasheet sheet, Long itemId) {
		return sheet.getItems().stream()
				.filter(item -> itemId.equals(item.getId()))
				.findFirst()
				.orElseThrow(() -> new BusinessException(
						"ITEM_NOT_FOUND",
						"항목을 찾을 수 없습니다.",
						HttpStatus.NOT_FOUND
				));
	}

	private Integer resolveArraySize(DatasheetItemUpsertRequest request) {
		if (!request.isArray()) {
			return null;
		}
		if (request.getArraySize() != null) {
			return request.getArraySize();
		}
		return request.getSlots() == null ? 0 : request.getSlots().size();
	}

	private void validateSlots(DatasheetItemUpsertRequest request) {
		List<DatasheetItemSlotRequest> slots = request.getSlots();
		if (!request.isArray()) {
			if (slots != null && !slots.isEmpty()) {
				throw new BusinessException(
						"INVALID_SLOTS",
						"배열이 아닌 항목에는 슬롯을 넣을 수 없습니다.",
						HttpStatus.BAD_REQUEST
				);
			}
			return;
		}
		if (slots == null || slots.isEmpty()) {
			throw new BusinessException(
					"INVALID_SLOTS",
					"배열 항목에는 슬롯이 필요합니다.",
					HttpStatus.BAD_REQUEST
			);
		}
		long distinctIndexes = slots.stream().map(DatasheetItemSlotRequest::getSlotIndex).distinct().count();
		if (distinctIndexes != slots.size()) {
			throw new BusinessException(
					"INVALID_SLOTS",
					"슬롯 인덱스가 중복되었습니다.",
					HttpStatus.BAD_REQUEST
			);
		}
		for (DatasheetItemSlotRequest slot : slots) {
			if (slot.getPosX() == null || slot.getPosY() == null) {
				throw new BusinessException(
						"INVALID_SLOTS",
						"모든 슬롯의 좌표를 지정해주세요.",
						HttpStatus.BAD_REQUEST
				);
			}
		}
	}

	private void replaceSlots(DatasheetItem item, List<DatasheetItemSlotRequest> slotRequests) {
		// orphanRemoval + UNIQUE(item_id, slot_index) 충돌 방지: 삭제 먼저 flush
		item.clearSlots();
		datasheetRepository.flush();
		if (slotRequests == null) {
			return;
		}
		for (DatasheetItemSlotRequest req : slotRequests) {
			item.addSlot(DatasheetItemSlot.builder()
					.slotIndex(req.getSlotIndex())
					.label(req.getLabel())
					.pdfPage(req.getPdfPage())
					.posX(req.getPosX())
					.posY(req.getPosY())
					.posWidth(req.getPosWidth())
					.posHeight(req.getPosHeight())
					.build());
		}
	}

	private void validateRangeRules(Datasheet sheet, Long selfItemId, DatasheetItemUpsertRequest request) {
		List<DatasheetItemRangeRuleRequest> rules = request.getRangeRules();
		if (rules == null || rules.isEmpty()) {
			return;
		}
		if (request.getInputType() != InputType.NUMBER) {
			throw new BusinessException(
					"INVALID_RANGE_RULES",
					"범위 규칙은 숫자 항목에만 설정할 수 있습니다.",
					HttpStatus.BAD_REQUEST
			);
		}

		Long resolvedDependsOnId = null;
		for (DatasheetItemRangeRuleRequest rule : rules) {
			if (rule.getDependsOnItemId() == null) {
				throw new BusinessException(
						"INVALID_RANGE_RULES",
						"기준 항목을 지정해주세요.",
						HttpStatus.BAD_REQUEST
				);
			}
			if (resolvedDependsOnId == null) {
				resolvedDependsOnId = rule.getDependsOnItemId();
			} else if (!resolvedDependsOnId.equals(rule.getDependsOnItemId())) {
				throw new BusinessException(
						"INVALID_RANGE_RULES",
						"범위 규칙의 기준 항목은 하나만 지정할 수 있습니다.",
						HttpStatus.BAD_REQUEST
				);
			}
			if (selfItemId != null && selfItemId.equals(rule.getDependsOnItemId())) {
				throw new BusinessException(
						"INVALID_RANGE_RULES",
						"자기 자신을 기준 항목으로 지정할 수 없습니다.",
						HttpStatus.BAD_REQUEST
				);
			}
		}

		final Long dependsOnId = resolvedDependsOnId;
		DatasheetItem dependsOn = sheet.getItems().stream()
				.filter(i -> dependsOnId.equals(i.getId()))
				.findFirst()
				.orElseThrow(() -> new BusinessException(
						"INVALID_RANGE_RULES",
						"기준 항목을 찾을 수 없습니다. 기종 항목을 먼저 저장하세요.",
						HttpStatus.BAD_REQUEST
				));

		if (!dependsOn.isArray()
				|| (dependsOn.getInputType() != InputType.CIRCLE
				&& dependsOn.getInputType() != InputType.CHECK
				&& dependsOn.getInputType() != InputType.OK_NG)) {
			throw new BusinessException(
					"INVALID_RANGE_RULES",
					"기준 항목은 동그라미/체크/OK_NG 배열 항목이어야 합니다.",
					HttpStatus.BAD_REQUEST
			);
		}

		for (DatasheetItemRangeRuleRequest rule : rules) {
			boolean slotExists = dependsOn.getSlots().stream()
					.anyMatch(s -> s.getSlotIndex() == rule.getMatchSlotIndex());
			if (!slotExists) {
				throw new BusinessException(
						"INVALID_RANGE_RULES",
						"기준 항목에 슬롯 인덱스 " + rule.getMatchSlotIndex() + " 가 없습니다.",
						HttpStatus.BAD_REQUEST
				);
			}
		}
	}

	private void replaceRangeRules(DatasheetItem item, List<DatasheetItemRangeRuleRequest> ruleRequests) {
		item.clearRangeRules();
		datasheetRepository.flush();
		if (ruleRequests == null || ruleRequests.isEmpty()) {
			return;
		}
		for (DatasheetItemRangeRuleRequest req : ruleRequests) {
			item.addRangeRule(DatasheetItemRangeRule.builder()
					.dependsOnItemId(req.getDependsOnItemId())
					.matchSlotIndex(req.getMatchSlotIndex())
					.minValue(req.getMinValue())
					.maxValue(req.getMaxValue())
					.build());
		}
	}

	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
