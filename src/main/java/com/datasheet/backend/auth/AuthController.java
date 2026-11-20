package com.datasheet.backend.auth;

import com.datasheet.backend.auth.dto.LoginRequest;
import com.datasheet.backend.auth.dto.LoginResponse;
import com.datasheet.backend.auth.dto.LoginResult;
import com.datasheet.backend.auth.dto.UserResponse;
import com.datasheet.backend.security.AuthCookieService;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;
	private final AuthCookieService authCookieService;

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletResponse response
	) {
		LoginResult result = authService.login(request);
		LoginResponse body = result.getResponse();

		response.addHeader(
				authCookieService.setCookieHeaderName(),
				authCookieService.toHeaderValue(
						authCookieService.createAccessTokenCookie(
								result.getAccessToken(),
								body.getExpiresInMs()
						)
				)
		);

		return ResponseEntity.ok(body);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletResponse response) {
		response.addHeader(
				authCookieService.setCookieHeaderName(),
				authCookieService.toHeaderValue(authCookieService.clearAccessTokenCookie())
		);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponse> me(Authentication authentication) {
		return ResponseEntity.ok(authService.me(authentication));
	}
}
