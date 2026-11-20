package com.datasheet.backend.auth;

import com.datasheet.backend.auth.dto.LoginRequest;
import com.datasheet.backend.auth.dto.LoginResponse;
import com.datasheet.backend.auth.dto.LoginResult;
import com.datasheet.backend.auth.dto.UserResponse;
import com.datasheet.backend.common.BusinessException;
import com.datasheet.backend.security.AuthProperties;
import com.datasheet.backend.security.CustomUserDetails;
import com.datasheet.backend.security.JwtService;
import com.datasheet.backend.user.User;
import com.datasheet.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthProperties authProperties;
	private final LoginAttemptService loginAttemptService;
	private final RefreshTokenService refreshTokenService;

	@Transactional
	public LoginResult login(LoginRequest request) {
		User user = userRepository.findByEmployeeId(request.getEmployeeId())
				.orElseThrow(() -> new BusinessException(
						"INVALID_CREDENTIALS",
						"사번 또는 비밀번호가 올바르지 않습니다.",
						HttpStatus.UNAUTHORIZED
				));

		if (!user.isActive()) {
			throw new BusinessException(
					"ACCOUNT_INACTIVE",
					"비활성화된 계정입니다. 관리자에게 문의하세요.",
					HttpStatus.FORBIDDEN
			);
		}

		if (user.isLocked()) {
			throw new BusinessException(
					"ACCOUNT_LOCKED",
					"로그인 실패 횟수 초과로 계정이 잠겼습니다. 관리자에게 문의하세요.",
					HttpStatus.FORBIDDEN
			);
		}

		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			int failedCount = loginAttemptService.recordFailure(user.getId());
			int remaining = authProperties.getMaxFailedLogin() - failedCount;
			if (remaining <= 0) {
				throw new BusinessException(
						"ACCOUNT_LOCKED",
						"로그인 실패 횟수 초과로 계정이 잠겼습니다. 관리자에게 문의하세요.",
						HttpStatus.FORBIDDEN
				);
			}
			throw new BusinessException(
					"INVALID_CREDENTIALS",
					"사번 또는 비밀번호가 올바르지 않습니다. (남은 시도: " + remaining + "회)",
					HttpStatus.UNAUTHORIZED
			);
		}

		loginAttemptService.resetFailures(user.getId());
		refreshTokenService.revokeAllForUser(user.getId());

		return issueTokens(user);
	}

	@Transactional
	public LoginResult refresh(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			throw new BusinessException(
					"INVALID_REFRESH_TOKEN",
					"Refresh Token이 없습니다.",
					HttpStatus.UNAUTHORIZED
			);
		}

		User user = refreshTokenService.rotateAndGetUser(rawRefreshToken);
		return issueTokens(user);
	}

	@Transactional
	public void logout(String rawRefreshToken) {
		refreshTokenService.revoke(rawRefreshToken);
	}

	@Transactional(readOnly = true)
	public UserResponse me(Authentication authentication) {
		CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
		return UserResponse.from(principal.getUser());
	}

	private LoginResult issueTokens(User user) {
		String accessToken = jwtService.createAccessToken(user);
		String refreshToken = refreshTokenService.issue(user);
		long expiresInMs = jwtService.getExpirationMs();
		long refreshExpiresInMs = jwtService.getRefreshExpirationMs();

		return LoginResult.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.refreshExpiresInMs(refreshExpiresInMs)
				.response(LoginResponse.builder()
						.tokenType("Bearer")
						.expiresInMs(expiresInMs)
						.refreshExpiresInMs(refreshExpiresInMs)
						.user(LoginResponse.UserInfo.builder()
								.id(user.getId())
								.employeeId(user.getEmployeeId())
								.name(user.getName())
								.role(user.getRole())
								.build())
						.build())
				.build();
	}
}
