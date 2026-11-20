package com.datasheet.backend.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiErrorResponse {

	private String code;
	private String message;
}
