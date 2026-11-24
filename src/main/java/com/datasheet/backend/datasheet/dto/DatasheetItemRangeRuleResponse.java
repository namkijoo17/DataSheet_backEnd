package com.datasheet.backend.datasheet.dto;

import com.datasheet.backend.datasheet.DatasheetItemRangeRule;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DatasheetItemRangeRuleResponse {

	private Long id;
	private Long dependsOnItemId;
	private int matchSlotIndex;
	private BigDecimal minValue;
	private BigDecimal maxValue;

	public static DatasheetItemRangeRuleResponse from(DatasheetItemRangeRule rule) {
		return DatasheetItemRangeRuleResponse.builder()
				.id(rule.getId())
				.dependsOnItemId(rule.getDependsOnItemId())
				.matchSlotIndex(rule.getMatchSlotIndex())
				.minValue(rule.getMinValue())
				.maxValue(rule.getMaxValue())
				.build();
	}
}
