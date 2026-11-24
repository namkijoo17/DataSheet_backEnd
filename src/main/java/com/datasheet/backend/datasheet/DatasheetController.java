package com.datasheet.backend.datasheet;

import com.datasheet.backend.datasheet.dto.DatasheetCreateRequest;
import com.datasheet.backend.datasheet.dto.DatasheetDetailResponse;
import com.datasheet.backend.datasheet.dto.DatasheetItemResponse;
import com.datasheet.backend.datasheet.dto.DatasheetItemReorderRequest;
import com.datasheet.backend.datasheet.dto.DatasheetItemUpsertRequest;
import com.datasheet.backend.datasheet.dto.DatasheetPositionRequest;
import com.datasheet.backend.datasheet.dto.DatasheetSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Datasheet", description = "데이터시트 기준정보·파일·항목 편집 API")
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

	@Operation(summary = "데이터시트 추가", description = "PDF 업로드와 함께 데이터시트를 생성합니다. 코드·개정은 자동 처리됩니다.")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<DatasheetDetailResponse> create(
			@RequestParam String name,
			@RequestParam(required = false) BigDecimal pageWidth,
			@RequestParam(required = false) BigDecimal pageHeight,
			@RequestParam("file") MultipartFile file
	) {
		DatasheetCreateRequest request = new DatasheetCreateRequest();
		request.setName(name);
		request.setRevision(1);
		request.setPageWidth(pageWidth);
		request.setPageHeight(pageHeight);
		return ResponseEntity.status(HttpStatus.CREATED).body(datasheetService.create(request, file));
	}

	@Operation(summary = "데이터시트 삭제", description = "시트 메타·항목과 PDF 파일을 삭제합니다.")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		datasheetService.delete(id);
	}

	@Operation(summary = "항목 추가")
	@PostMapping("/{id}/items")
	public ResponseEntity<DatasheetItemResponse> createItem(
			@PathVariable Long id,
			@Valid @RequestBody DatasheetItemUpsertRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED).body(datasheetService.createItem(id, request));
	}

	@Operation(summary = "항목 수정", description = "메타·좌표·슬롯을 갱신합니다. 슬롯은 요청 목록으로 교체됩니다.")
	@PutMapping("/{id}/items/{itemId}")
	public ResponseEntity<DatasheetItemResponse> updateItem(
			@PathVariable Long id,
			@PathVariable Long itemId,
			@Valid @RequestBody DatasheetItemUpsertRequest request
	) {
		return ResponseEntity.ok(datasheetService.updateItem(id, itemId, request));
	}

	@Operation(summary = "항목 삭제")
	@DeleteMapping("/{id}/items/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteItem(@PathVariable Long id, @PathVariable Long itemId) {
		datasheetService.deleteItem(id, itemId);
	}

	@Operation(summary = "항목 표시 순서 변경", description = "itemIds 배열 순서대로 sortOrder를 다시 부여합니다.")
	@PutMapping("/{id}/items-order")
	public ResponseEntity<DatasheetDetailResponse> reorderItems(
			@PathVariable Long id,
			@Valid @RequestBody DatasheetItemReorderRequest request
	) {
		return ResponseEntity.ok(datasheetService.reorderItems(id, request));
	}

	@Operation(summary = "항목 좌표만 갱신")
	@PutMapping("/{id}/items/{itemId}/position")
	public ResponseEntity<DatasheetItemResponse> updateItemPosition(
			@PathVariable Long id,
			@PathVariable Long itemId,
			@Valid @RequestBody DatasheetPositionRequest request
	) {
		return ResponseEntity.ok(datasheetService.updateItemPosition(id, itemId, request));
	}

	@Operation(summary = "슬롯 좌표만 갱신")
	@PutMapping("/{id}/items/{itemId}/slots/{slotId}/position")
	public ResponseEntity<DatasheetItemResponse> updateSlotPosition(
			@PathVariable Long id,
			@PathVariable Long itemId,
			@PathVariable Long slotId,
			@Valid @RequestBody DatasheetPositionRequest request
	) {
		return ResponseEntity.ok(datasheetService.updateSlotPosition(id, itemId, slotId, request));
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
