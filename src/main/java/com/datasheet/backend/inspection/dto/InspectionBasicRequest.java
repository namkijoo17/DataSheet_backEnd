package com.datasheet.backend.inspection.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** 검사 기본정보 (기번·유닛트·검사자·환경·부품번호) */
@Getter
@Setter
@NoArgsConstructor
public class InspectionBasicRequest {

	@Size(max = 100)
	private String serialNo;

	@Size(max = 100)
	private String unit;

	@Size(max = 100)
	private String line;

	private LocalDate inspectDate;

	@Size(max = 100)
	private String inspector;

	@Size(max = 200)
	private String inspectorChangeReason;

	@Size(max = 20)
	private String temperature;

	@Size(max = 20)
	private String humidity;

	private Map<String, @Size(max = 100) String> parts = new LinkedHashMap<>();
}
