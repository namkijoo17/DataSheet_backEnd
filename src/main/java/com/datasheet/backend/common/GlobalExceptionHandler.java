package com.datasheet.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
