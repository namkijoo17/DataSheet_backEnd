package com.datasheet.backend.datasheet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DatasheetItemRangeRuleRequest {

	@NotNull
	private Long dependsOnItemId;

	@Min(0)
	private int matchSlotIndex;

	private BigDecimal minValue;

	private BigDecimal maxValue;
}
