package com.datasheet.backend.inspection.dto;

import com.datasheet.backend.inspection.Inspection;
import com.datasheet.backend.inspection.InspectionAnswer;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class InspectionDetailResponse {

	private Long id;
	private Long datasheetId;
	private String mode;
	private String status;
	private InspectionBasicResponse basic;
	/** 데이터시트 항목 ID → 답변 */
	private Map<Long, InspectionAnswerResponse> answers;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime completedAt;
	private String result;
	private Integer okCount;
	private Integer ngCount;
	private Integer skipCount;
	private String fileName;
	/** 재검사 이력(사유 포함), 오래된 순 */
	private List<InspectionRecheckResponse> rechecks;

	public static InspectionDetailResponse from(Inspection inspection) {
		Map<Long, InspectionAnswerResponse> answers = new LinkedHashMap<>();
		for (InspectionAnswer answer : inspection.getAnswers()) {
			if (answer.hasContent()) {
				answers.put(answer.getDatasheetItemId(), InspectionAnswerResponse.from(answer));
			}
		}
		return InspectionDetailResponse.builder()
				.id(inspection.getId())
				.datasheetId(inspection.getDatasheet().getId())
				.mode(inspection.getMode().name())
				.status(inspection.getStatus().name())
				.basic(InspectionBasicResponse.from(inspection))
				.answers(answers)
				.createdAt(inspection.getCreatedAt())
				.updatedAt(inspection.getUpdatedAt())
				.completedAt(inspection.getCompletedAt())
				.result(inspection.getResult())
				.okCount(inspection.getOkCount())
				.ngCount(inspection.getNgCount())
				.skipCount(inspection.getSkipCount())
				.fileName(inspection.getFileName())
				.rechecks(inspection.getRechecks().stream().map(InspectionRecheckResponse::from).toList())
				.build();
	}
}
