package com.datasheet.backend.inspection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 항목 재검사 사유 기록 요청 */
@Getter
@Setter
@NoArgsConstructor
public class InspectionRecheckRequest {

	@NotBlank(message = "재검사 사유를 입력해 주세요.")
	@Size(max = 200, message = "재검사 사유는 200자 이내로 입력해 주세요.")
	private String reason;

	@Size(max = 200)
	private String itemLabel;

	/** 다시 검사하기 전 입력값 요약 */
	@Size(max = 300)
	private String previousSummary;
}
