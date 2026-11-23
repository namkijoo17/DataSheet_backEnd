package com.datasheet.backend.file.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DatasheetFileResponse {

	private String fileName;
	private long sizeBytes;
	private String contentType;
	private Instant lastModified;
}
