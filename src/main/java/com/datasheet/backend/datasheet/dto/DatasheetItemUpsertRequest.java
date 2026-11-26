package com.datasheet.backend.datasheet.dto;

import com.datasheet.backend.datasheet.InputType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DatasheetItemUpsertRequest {

	@Size(max = 100)
	private String groupName;

	@NotBlank
	@Size(max = 200)
	private String label;

	private boolean inspectionItem = true;

	private boolean array = false;

	private Integer arraySize;

	@NotNull
	private InputType inputType;

	@Size(max = 20)
	private String unit;

	private Integer decimalPlaces;

	private BigDecimal minValue;

	private BigDecimal maxValue;

	private boolean allowNegative = false;

	private boolean required = true;

	private boolean skippable = false;

	@Size(max = 500)
	private String formula;

	@Size(max = 500)
	private String specDescription;

	private Integer sortOrder;

	private int pdfPage = 1;

	@NotNull
	private BigDecimal posX;

	@NotNull
	private BigDecimal posY;

	private BigDecimal posWidth;

	private BigDecimal posHeight;

	@Valid
	private List<DatasheetItemSlotRequest> slots = new ArrayList<>();

	/** 숫자 항목: 기종 등 다른 항목 슬롯에 따른 min/max */
	@Valid
	private List<DatasheetItemRangeRuleRequest> rangeRules = new ArrayList<>();
}
