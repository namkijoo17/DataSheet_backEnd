package com.datasheet.backend.datasheet.dto;

import com.datasheet.backend.datasheet.Datasheet;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class DatasheetDetailResponse {

	private Long id;
	private String code;
	private String name;
	private int revision;
	private String fileName;
	private BigDecimal pageWidth;
	private BigDecimal pageHeight;
	private boolean active;
	private List<DatasheetItemResponse> items;

	public static DatasheetDetailResponse from(Datasheet datasheet) {
		return DatasheetDetailResponse.builder()
				.id(datasheet.getId())
				.code(datasheet.getCode())
				.name(datasheet.getName())
				.revision(datasheet.getRevision())
				.fileName(datasheet.getFileName())
				.pageWidth(datasheet.getPageWidth())
				.pageHeight(datasheet.getPageHeight())
				.active(datasheet.isActive())
				.items(datasheet.getItems().stream().map(DatasheetItemResponse::from).toList())
				.build();
	}
}
