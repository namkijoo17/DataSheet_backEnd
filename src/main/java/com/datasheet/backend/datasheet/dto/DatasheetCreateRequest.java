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

	@NotBlank
	@Size(max = 200)
	private String name;

	@Min(1)
	private Integer revision = 1;

	private BigDecimal pageWidth;

	private BigDecimal pageHeight;
}
