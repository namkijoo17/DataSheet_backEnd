package com.datasheet.backend.datasheet.dto;

import com.datasheet.backend.datasheet.Datasheet;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class DatasheetSummaryResponse {

	private Long id;
	private String name;
	private int revision;
	private String fileName;
	private BigDecimal pageWidth;
	private BigDecimal pageHeight;
	private boolean active;

	public static DatasheetSummaryResponse from(Datasheet datasheet) {
		return DatasheetSummaryResponse.builder()
				.id(datasheet.getId())
				.name(datasheet.getName())
				.revision(datasheet.getRevision())
				.fileName(datasheet.getFileName())
				.pageWidth(datasheet.getPageWidth())
				.pageHeight(datasheet.getPageHeight())
				.active(datasheet.isActive())
				.build();
	}
}
