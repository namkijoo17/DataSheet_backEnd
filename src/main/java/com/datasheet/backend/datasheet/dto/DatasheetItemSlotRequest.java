package com.datasheet.backend.datasheet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class DatasheetItemSlotRequest {

	/** 기존 슬롯 수정 시 id. 없으면 신규 */
	private Long id;

	@Min(0)
	private int slotIndex;

	@NotBlank
	@Size(max = 100)
	private String label;

	@Min(1)
	private int pdfPage = 1;

	@NotNull
	private BigDecimal posX;

	@NotNull
	private BigDecimal posY;

	private BigDecimal posWidth;

	private BigDecimal posHeight;
}
