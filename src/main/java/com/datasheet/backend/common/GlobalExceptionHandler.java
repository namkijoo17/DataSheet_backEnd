package com.datasheet.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex) {
		return ResponseEntity.status(ex.getStatus())
				.body(ApiErrorResponse.builder()
						.code(ex.getCode())
						.message(ex.getMessage())
						.build());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse("입력값이 올바르지 않습니다.");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiErrorResponse.builder()
						.code("VALIDATION_ERROR")
						.message(message)
						.build());
	}

	/**
	 * 잘못된 JSON/Enum 등 본문 파싱 실패.
	 * 여기서 처리하지 않으면 /error 로 넘어가 JWT 없이 401이 나와 프론트가 로그아웃한다.
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
		String detail = ex.getMostSpecificCause() != null
				? ex.getMostSpecificCause().getMessage()
				: ex.getMessage();
		String message = "요청 본문을 읽을 수 없습니다.";
		if (detail != null && detail.contains("InputType")) {
			message = "지원하지 않는 입력 타입입니다. 서버를 최신으로 재시작했는지 확인하세요.";
		} else if (detail != null && detail.toLowerCase().contains("enum")) {
			message = "허용되지 않은 값이 포함되어 있습니다.";
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiErrorResponse.builder()
						.code("INVALID_REQUEST_BODY")
						.message(message)
						.build());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleOther(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiErrorResponse.builder()
						.code("INTERNAL_ERROR")
						.message(ex.getMessage() != null ? ex.getMessage() : "서버 오류가 발생했습니다.")
						.build());
	}
}
