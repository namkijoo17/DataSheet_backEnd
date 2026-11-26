package com.datasheet.backend.inspection.dto;

import com.datasheet.backend.inspection.InspectionAnswer;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
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
	private String ngAction;
	private LocalDateTime ngOccurredAt;

	public static InspectionAnswerResponse from(InspectionAnswer answer) {
		return InspectionAnswerResponse.builder()
				.itemId(answer.getDatasheetItemId())
				.skipped(answer.isSkipped())
				.value(answer.getValue())
				.slotIndex(answer.getSlotIndex())
				.judgment(answer.getJudgment())
				.slotValues(new LinkedHashMap<>(answer.getSlotValues()))
				.ngAction(answer.getNgAction() != null ? answer.getNgAction().name() : null)
				.ngOccurredAt(answer.getNgOccurredAt())
				.build();
	}
}
