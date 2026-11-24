package com.datasheet.backend.datasheet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class DatasheetPositionRequest {

	@Min(1)
	private int pdfPage = 1;

	@NotNull
	private BigDecimal posX;

	@NotNull
	private BigDecimal posY;

	@DecimalMin("0.0")
	private BigDecimal posWidth;

	@DecimalMin("0.0")
	private BigDecimal posHeight;
}
