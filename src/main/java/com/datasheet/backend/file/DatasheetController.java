package com.datasheet.backend.file;

import com.datasheet.backend.file.dto.DatasheetFileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Datasheet", description = "데이터시트 파일 조회 API (서버 폴더 수동 배치)")
@RestController
@RequestMapping("/api/datasheets")
@RequiredArgsConstructor
public class DatasheetController {

	private final DatasheetFileService datasheetFileService;

	@Operation(summary = "데이터시트 목록", description = "설정된 폴더의 파일 목록을 반환합니다.")
	@GetMapping
	public ResponseEntity<List<DatasheetFileResponse>> list() {
		return ResponseEntity.ok(datasheetFileService.listFiles());
	}

	@Operation(summary = "데이터시트 파일 조회", description = "파일 내용을 스트리밍합니다. 프론트에서 PDF/이미지로 표시할 때 사용합니다.")
	@GetMapping("/{fileName}/file")
	public ResponseEntity<Resource> download(@PathVariable String fileName) {
		Resource resource = datasheetFileService.loadAsResource(fileName);
		String contentType = datasheetFileService.detectContentType(fileName);
		String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encoded)
				.body(resource);
	}
}
