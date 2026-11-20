package com.datasheet.backend.auth;

import com.datasheet.backend.auth.dto.LoginRequest;
import com.datasheet.backend.auth.dto.LoginResponse;
import com.datasheet.backend.auth.dto.LoginResult;
import com.datasheet.backend.auth.dto.UserResponse;
import com.datasheet.backend.security.AuthCookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	private final AuthCookieService authCookieService;

	@Operation(summary = "로그인", description = "사번/비밀번호로 로그인합니다. Access·Refresh Token을 HttpOnly 쿠키로 발급합니다.")
	@SecurityRequirements
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletResponse response
	) {
		LoginResult result = authService.login(request);
		writeAuthCookies(response, result);
		return ResponseEntity.ok(result.getResponse());
	}

	@Operation(summary = "토큰 갱신", description = "Refresh Token 쿠키로 Access Token을 재발급합니다. Refresh는 회전(rotate)됩니다.")
	@SecurityRequirements
	@PostMapping("/refresh")
	public ResponseEntity<LoginResponse> refresh(
			HttpServletRequest request,
			HttpServletResponse response
	) {
		LoginResult result = authService.refresh(extractCookie(request, AuthCookieService.REFRESH_TOKEN_COOKIE));
		writeAuthCookies(response, result);
		return ResponseEntity.ok(result.getResponse());
	}

	@Operation(summary = "로그아웃", description = "Refresh Token을 폐기하고 인증 쿠키를 삭제합니다.")
	@SecurityRequirements
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			HttpServletRequest request,
			HttpServletResponse response
	) {
		authService.logout(extractCookie(request, AuthCookieService.REFRESH_TOKEN_COOKIE));
		clearAuthCookies(response);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "내 정보 조회")
	@GetMapping("/me")
	public ResponseEntity<UserResponse> me(Authentication authentication) {
		return ResponseEntity.ok(authService.me(authentication));
	}

	private void writeAuthCookies(HttpServletResponse response, LoginResult result) {
		LoginResponse body = result.getResponse();
		addCookie(response, authCookieService.createAccessTokenCookie(
				result.getAccessToken(),
				body.getExpiresInMs()
		));
		addCookie(response, authCookieService.createRefreshTokenCookie(
				result.getRefreshToken(),
				result.getRefreshExpiresInMs()
		));
	}

	private void clearAuthCookies(HttpServletResponse response) {
		addCookie(response, authCookieService.clearAccessTokenCookie());
		addCookie(response, authCookieService.clearRefreshTokenCookie());
	}

	private void addCookie(HttpServletResponse response, org.springframework.http.ResponseCookie cookie) {
		response.addHeader(authCookieService.setCookieHeaderName(), authCookieService.toHeaderValue(cookie));
	}

	private String extractCookie(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (name.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
