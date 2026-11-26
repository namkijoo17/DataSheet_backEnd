package com.datasheet.backend.inspection.dto;

import com.datasheet.backend.inspection.InspectionMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 검사 시작(생성) / 재시작(기본정보·모드 교체) 공용 요청. 생성 시에만 datasheetId 필요. */
@Getter
@Setter
@NoArgsConstructor
public class InspectionStartRequest {

	private Long datasheetId;

	@NotNull
	private InspectionMode mode;

	@Valid
	@NotNull
	private InspectionBasicRequest basic;
}
