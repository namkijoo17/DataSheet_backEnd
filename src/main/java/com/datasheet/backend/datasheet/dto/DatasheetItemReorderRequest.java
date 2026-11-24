package com.datasheet.backend.datasheet.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DatasheetItemReorderRequest {

	/** 원하는 표시 순서대로 항목 id 나열 */
	@NotEmpty
	private List<Long> itemIds = new ArrayList<>();
}
