package com.datasheet.backend.inspection.dto;

import com.datasheet.backend.inspection.InspectionRecheck;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InspectionRecheckResponse {

	private Long itemId;
	private String itemLabel;
	private String reason;
	private String previousSummary;
	private String inspector;
	private LocalDateTime createdAt;

	public static InspectionRecheckResponse from(InspectionRecheck recheck) {
		return InspectionRecheckResponse.builder()
				.itemId(recheck.getDatasheetItemId())
				.itemLabel(recheck.getItemLabel())
				.reason(recheck.getReason())
				.previousSummary(recheck.getPreviousSummary())
				.inspector(recheck.getInspector())
				.createdAt(recheck.getCreatedAt())
				.build();
	}
}
