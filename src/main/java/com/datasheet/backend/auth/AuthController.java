package com.datasheet.backend.auth;

import com.datasheet.backend.auth.dto.LoginRequest;
import com.datasheet.backend.auth.dto.LoginResponse;
import com.datasheet.backend.auth.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "로그인·인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "로그인", description = "사번/비밀번호로 로그인하고 JWT를 발급합니다. 5회 실패 시 계정이 잠깁니다.")
	@SecurityRequirements
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자 정보를 반환합니다.")
	@GetMapping("/me")
	public ResponseEntity<UserResponse> me(Authentication authentication) {
		return ResponseEntity.ok(authService.me(authentication));
	}
}
