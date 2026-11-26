package com.datasheet.backend.datasheet.dto;

import com.datasheet.backend.datasheet.Datasheet;
import com.datasheet.backend.datasheet.DatasheetItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DatasheetDetailResponse {

	private Long id;
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
				.name(datasheet.getName())
				.revision(datasheet.getRevision())
				.fileName(datasheet.getFileName())
				.pageWidth(datasheet.getPageWidth())
				.pageHeight(datasheet.getPageHeight())
				.active(datasheet.isActive())
				.items(distinctItems(datasheet.getItems()).stream()
						.map(DatasheetItemResponse::from)
						.toList())
				.build();
	}

	/** join fetch 다중 컬렉션으로 인한 중복 행 제거 */
	private static List<DatasheetItem> distinctItems(List<DatasheetItem> items) {
		Map<Long, DatasheetItem> unique = new LinkedHashMap<>();
		for (DatasheetItem item : items) {
			unique.putIfAbsent(item.getId(), item);
		}
		return new ArrayList<>(unique.values());
	}
}
