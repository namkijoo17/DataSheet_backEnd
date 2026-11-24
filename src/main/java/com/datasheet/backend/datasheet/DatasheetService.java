package com.datasheet.backend.datasheet;

import com.datasheet.backend.common.BusinessException;
import com.datasheet.backend.datasheet.dto.DatasheetDetailResponse;
import com.datasheet.backend.datasheet.dto.DatasheetItemResponse;
import com.datasheet.backend.datasheet.dto.DatasheetItemSlotRequest;
import com.datasheet.backend.datasheet.dto.DatasheetItemUpsertRequest;
import com.datasheet.backend.datasheet.dto.DatasheetPositionRequest;
import com.datasheet.backend.datasheet.dto.DatasheetSummaryResponse;
import com.datasheet.backend.file.DatasheetFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DatasheetService {

	private final DatasheetRepository datasheetRepository;
	private final DatasheetFileService datasheetFileService;

	@Transactional(readOnly = true)
	public List<DatasheetSummaryResponse> listActive() {
		return datasheetRepository.findByActiveTrueOrderByCodeAscRevisionDesc().stream()
				.map(DatasheetSummaryResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public DatasheetDetailResponse getDetail(Long id) {
		return DatasheetDetailResponse.from(requireSheetWithItems(id));
	}

	@Transactional
	public DatasheetItemResponse createItem(Long datasheetId, DatasheetItemUpsertRequest request) {
		Datasheet sheet = requireSheetWithItems(datasheetId);
		validateSlots(request);

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
				.pdfPage(request.getPdfPage())
				.posX(request.getPosX())
				.posY(request.getPosY())
				.posWidth(request.getPosWidth())
				.posHeight(request.getPosHeight())
				.sortOrder(sortOrder)
				.build();

		replaceSlots(item, request.getSlots());
		sheet.addItem(item);
		datasheetRepository.saveAndFlush(sheet);
		return DatasheetItemResponse.from(item);
	}

	@Transactional
	public DatasheetItemResponse updateItem(Long datasheetId, Long itemId, DatasheetItemUpsertRequest request) {
		Datasheet sheet = requireSheetWithItems(datasheetId);
		DatasheetItem item = requireItem(sheet, itemId);
		validateSlots(request);

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
		datasheetRepository.flush();
		return DatasheetItemResponse.from(item);
	}

	@Transactional
	public void deleteItem(Long datasheetId, Long itemId) {
		Datasheet sheet = requireSheetWithItems(datasheetId);
		DatasheetItem item = requireItem(sheet, itemId);
		sheet.removeItem(item);
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
	}

	private void replaceSlots(DatasheetItem item, List<DatasheetItemSlotRequest> slotRequests) {
		item.clearSlots();
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
}
