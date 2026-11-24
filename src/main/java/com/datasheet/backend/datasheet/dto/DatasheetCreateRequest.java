package com.datasheet.backend.datasheet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DatasheetCreateRequest {

	/** 미입력 시 파일명에서 자동 생성 */
	@Size(max = 50)
	private String code;

	@NotBlank
	@Size(max = 200)
	private String name;

	@Min(1)
	private Integer revision = 1;

	private BigDecimal pageWidth;

	private BigDecimal pageHeight;
}
