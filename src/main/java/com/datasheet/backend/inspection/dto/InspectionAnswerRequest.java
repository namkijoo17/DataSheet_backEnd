package com.datasheet.backend.inspection.dto;

import com.datasheet.backend.inspection.NgAction;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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

	/** NG 발생 시 선택한 처리 방법 (없으면 null) */
	private NgAction ngAction;

	/** NG 발생 시각 (yyyy-MM-dd'T'HH:mm:ss, 서버 로컬 시간) */
	private LocalDateTime ngOccurredAt;
}
