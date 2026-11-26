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
	private String unit;
	private String inspector;
	private String mode;
	private String status;
	private int answeredCount;
	private int totalCount;
	/** 완료된 검사의 판정 집계 (진행 중이면 null) */
	private String result;
	private Integer okCount;
	private Integer ngCount;
	private Integer skipCount;
	/** 저장된 결과 PDF 의 표시용 파일명 (없으면 null) */
	private String fileName;
	private LocalDateTime updatedAt;
	private LocalDateTime completedAt;
}
