package com.datasheet.backend.inspection;

import com.datasheet.backend.inspection.dto.InspectionAnswerRequest;
import com.datasheet.backend.inspection.dto.InspectionDetailResponse;
import com.datasheet.backend.inspection.dto.InspectionStartRequest;
import com.datasheet.backend.inspection.dto.InspectionSummaryResponse;
import com.datasheet.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

import java.util.List;

@Tag(name = "Inspection", description = "검사 진행·항목별 저장 API")
@RestController
@RequestMapping("/api/inspections")
@RequiredArgsConstructor
public class InspectionController {

	private final InspectionService inspectionService;

	@Operation(summary = "검사 시작", description = "기본정보·모드로 진행 중(IN_PROGRESS) 검사를 생성합니다.")
	@PostMapping
	public ResponseEntity<InspectionDetailResponse> start(
			@AuthenticationPrincipal CustomUserDetails principal,
			@Valid @RequestBody InspectionStartRequest request
	) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(inspectionService.start(principal.getUser().getId(), request));
	}

	@Operation(summary = "내 검사 목록", description = "기본값은 진행 중(IN_PROGRESS) 검사입니다.")
	@GetMapping
	public ResponseEntity<List<InspectionSummaryResponse>> list(
			@AuthenticationPrincipal CustomUserDetails principal,
			@RequestParam(defaultValue = "IN_PROGRESS") InspectionStatus status
	) {
		return ResponseEntity.ok(inspectionService.list(principal.getUser().getId(), status));
	}

	@Operation(summary = "검사 상세", description = "기본정보와 항목별 저장된 답변을 반환합니다. (이어하기용)")
	@GetMapping("/{id}")
	public ResponseEntity<InspectionDetailResponse> detail(
			@AuthenticationPrincipal CustomUserDetails principal,
			@PathVariable Long id
	) {
		return ResponseEntity.ok(inspectionService.detail(principal.getUser().getId(), id));
	}

	@Operation(summary = "기본정보·모드 수정", description = "기본정보·모드를 교체합니다. 모드가 바뀌면 저장된 답변을 모두 비우고, 같으면 유지합니다.")
	@PutMapping("/{id}")
	public ResponseEntity<InspectionDetailResponse> restart(
			@AuthenticationPrincipal CustomUserDetails principal,
			@PathVariable Long id,
			@Valid @RequestBody InspectionStartRequest request
	) {
		return ResponseEntity.ok(inspectionService.restart(principal.getUser().getId(), id, request));
	}

	@Operation(summary = "항목 답변 저장", description = "항목 1개의 답변을 저장(덮어쓰기)합니다. 내용이 모두 비어 있으면 삭제됩니다.")
	@PutMapping("/{id}/answers/{itemId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void saveAnswer(
			@AuthenticationPrincipal CustomUserDetails principal,
			@PathVariable Long id,
			@PathVariable Long itemId,
			@Valid @RequestBody InspectionAnswerRequest request
	) {
		inspectionService.saveAnswer(principal.getUser().getId(), id, itemId, request);
	}

	@Operation(summary = "검사 완료", description = "검사를 완료(COMPLETED) 처리합니다.")
	@PostMapping("/{id}/complete")
	public ResponseEntity<InspectionDetailResponse> complete(
			@AuthenticationPrincipal CustomUserDetails principal,
			@PathVariable Long id
	) {
		return ResponseEntity.ok(inspectionService.complete(principal.getUser().getId(), id));
	}

	@Operation(summary = "진행 중 검사 삭제", description = "완료되지 않은 검사를 삭제합니다.")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void discard(
			@AuthenticationPrincipal CustomUserDetails principal,
			@PathVariable Long id
	) {
		inspectionService.discard(principal.getUser().getId(), id);
	}
}
