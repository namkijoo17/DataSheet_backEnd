package com.datasheet.backend.inspection.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/** 항목 1개의 답변. 내용이 모두 비어 있으면 저장된 답변을 삭제(재검사)한다. */
@Getter
@Setter
@NoArgsConstructor
public class InspectionAnswerRequest {

	private boolean skipped;

	@Size(max = 1000)
	private String value;

	private Integer slotIndex;

	@Pattern(regexp = "OK|NG", message = "판정은 OK 또는 NG 여야 합니다.")
	private String judgment;

	private Map<Integer, @Size(max = 500) String> slotValues = new LinkedHashMap<>();
}
