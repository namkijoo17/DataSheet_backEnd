package com.datasheet.backend.inspection.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InspectionSummaryResponse {

	private Long id;
	private Long datasheetId;
	private String datasheetName;
	private String serialNo;
	private String mode;
	private String status;
	private int answeredCount;
	private int totalCount;
	private LocalDateTime updatedAt;
}
