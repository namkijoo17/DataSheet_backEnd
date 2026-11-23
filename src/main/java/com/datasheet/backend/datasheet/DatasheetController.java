package com.datasheet.backend.datasheet;

import com.datasheet.backend.datasheet.dto.DatasheetDetailResponse;
import com.datasheet.backend.datasheet.dto.DatasheetSummaryResponse;
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

@Tag(name = "Datasheet", description = "데이터시트 기준정보·파일 조회 API")
@RestController
@RequestMapping("/api/datasheets")
@RequiredArgsConstructor
public class DatasheetController {

	private final DatasheetService datasheetService;

	@Operation(summary = "데이터시트 목록", description = "활성 데이터시트 메타 목록을 반환합니다.")
	@GetMapping
	public ResponseEntity<List<DatasheetSummaryResponse>> list() {
		return ResponseEntity.ok(datasheetService.listActive());
	}

	@Operation(summary = "데이터시트 상세", description = "항목(검사항목 여부·범위·PDF 좌표 포함)을 반환합니다.")
	@GetMapping("/{id}")
	public ResponseEntity<DatasheetDetailResponse> detail(@PathVariable Long id) {
		return ResponseEntity.ok(datasheetService.getDetail(id));
	}

	@Operation(summary = "데이터시트 파일 조회", description = "DB의 file_name 기준으로 폴더에서 PDF를 스트리밍합니다.")
	@GetMapping("/{id}/file")
	public ResponseEntity<Resource> file(@PathVariable Long id) {
		Resource resource = datasheetService.loadFile(id);
		String fileName = datasheetService.fileName(id);
		String contentType = datasheetService.contentType(id);
		String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encoded)
				.body(resource);
	}
}
