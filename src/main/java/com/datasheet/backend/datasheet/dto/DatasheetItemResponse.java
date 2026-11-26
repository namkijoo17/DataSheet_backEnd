package com.datasheet.backend.datasheet.dto;

import com.datasheet.backend.datasheet.DatasheetItem;
import com.datasheet.backend.datasheet.InputType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DatasheetItemResponse {

	private Long id;
	private int itemNo;
	private String groupName;
	private String label;
	/** true = 검사항목(판정 대상), false = 정보/환경 등 */
	private boolean inspectionItem;
	/** true = 배열(다중 값) 입력 */
	private boolean array;
	private Integer arraySize;
	private InputType inputType;
	private String unit;
	private Integer decimalPlaces;
	private BigDecimal minValue;
	private BigDecimal maxValue;
	private boolean allowNegative;
	private boolean required;
	private boolean skippable;
	private String formula;
	private String specDescription;
	private int pdfPage;
	private BigDecimal posX;
	private BigDecimal posY;
	private BigDecimal posWidth;
	private BigDecimal posHeight;
	private int sortOrder;
	/** 배열일 때 요소별 PDF 좌표 (단일이면 빈 배열) */
	private List<DatasheetItemSlotResponse> slots;

	/** 기종 슬롯별 min/max (없으면 빈 배열 → 항목 기본 min/max 사용) */
	private List<DatasheetItemRangeRuleResponse> rangeRules;

	public static DatasheetItemResponse from(DatasheetItem item) {
		return DatasheetItemResponse.builder()
				.id(item.getId())
				.itemNo(item.getItemNo())
				.groupName(item.getGroupName())
				.label(item.getLabel())
				.inspectionItem(item.isInspectionItem())
				.array(item.isArray())
				.arraySize(item.getArraySize())
				.inputType(item.getInputType())
				.unit(item.getUnit())
				.decimalPlaces(item.getDecimalPlaces())
				.minValue(item.getMinValue())
				.maxValue(item.getMaxValue())
				.allowNegative(item.isAllowNegative())
				.required(item.isRequired())
				.skippable(item.isSkippable())
				.formula(item.getFormula())
				.specDescription(item.getSpecDescription())
				.pdfPage(item.getPdfPage())
				.posX(item.getPosX())
				.posY(item.getPosY())
				.posWidth(item.getPosWidth())
				.posHeight(item.getPosHeight())
				.sortOrder(item.getSortOrder())
				.slots(item.getSlots().stream().map(DatasheetItemSlotResponse::from).toList())
				.rangeRules(item.getRangeRules().stream().map(DatasheetItemRangeRuleResponse::from).toList())
				.build();
	}
}
