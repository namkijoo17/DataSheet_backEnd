package com.datasheet.backend.datasheet.dto;

import com.datasheet.backend.datasheet.DatasheetItemSlot;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DatasheetItemSlotResponse {

	private Long id;
	private int slotIndex;
	private String label;
	private int pdfPage;
	private BigDecimal posX;
	private BigDecimal posY;
	private BigDecimal posWidth;
	private BigDecimal posHeight;

	public static DatasheetItemSlotResponse from(DatasheetItemSlot slot) {
		return DatasheetItemSlotResponse.builder()
				.id(slot.getId())
				.slotIndex(slot.getSlotIndex())
				.label(slot.getLabel())
				.pdfPage(slot.getPdfPage())
				.posX(slot.getPosX())
				.posY(slot.getPosY())
				.posWidth(slot.getPosWidth())
				.posHeight(slot.getPosHeight())
				.build();
	}
}
