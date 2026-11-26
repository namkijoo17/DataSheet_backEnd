package com.datasheet.backend.inspection.dto;

import com.datasheet.backend.inspection.InspectionAnswer;
import lombok.Builder;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Builder
public class InspectionAnswerResponse {

	private Long itemId;
	private boolean skipped;
	private String value;
	private Integer slotIndex;
	private String judgment;
	private Map<Integer, String> slotValues;

	public static InspectionAnswerResponse from(InspectionAnswer answer) {
		return InspectionAnswerResponse.builder()
				.itemId(answer.getDatasheetItemId())
				.skipped(answer.isSkipped())
				.value(answer.getValue())
				.slotIndex(answer.getSlotIndex())
				.judgment(answer.getJudgment())
				.slotValues(new LinkedHashMap<>(answer.getSlotValues()))
				.build();
	}
}
