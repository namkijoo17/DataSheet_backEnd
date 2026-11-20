package com.datasheet.backend.auth;

import com.datasheet.backend.common.BusinessException;
import com.datasheet.backend.security.JwtService;
import com.datasheet.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtService jwtService;
	private final SecureRandom secureRandom = new SecureRandom();

	@Transactional
	public String issue(User user) {
		String rawToken = generateRawToken();
		LocalDateTime expiresAt = LocalDateTime.now()
				.plusSeconds(jwtService.getRefreshExpirationMs() / 1000);

		RefreshToken refreshToken = RefreshToken.builder()
				.user(user)
				.tokenHash(hash(rawToken))
				.expiresAt(expiresAt)
				.build();
		refreshTokenRepository.save(refreshToken);
		return rawToken;
	}

	@Transactional
	public User rotateAndGetUser(String rawToken) {
		RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawToken))
				.orElseThrow(() -> new BusinessException(
						"INVALID_REFRESH_TOKEN",
						"유효하지 않은 Refresh Token입니다.",
						HttpStatus.UNAUTHORIZED
				));

		if (!stored.isUsable()) {
			throw new BusinessException(
					"INVALID_REFRESH_TOKEN",
					"만료되었거나 폐기된 Refresh Token입니다.",
					HttpStatus.UNAUTHORIZED
			);
		}

		User user = stored.getUser();
		if (!user.isActive() || user.isLocked()) {
			stored.revoke();
			throw new BusinessException(
					"ACCOUNT_UNAVAILABLE",
					"사용할 수 없는 계정입니다.",
					HttpStatus.FORBIDDEN
			);
		}

		stored.revoke();
		return user;
	}

	@Transactional
	public void revoke(String rawToken) {
		if (rawToken == null || rawToken.isBlank()) {
			return;
		}
		refreshTokenRepository.findByTokenHash(hash(rawToken))
				.ifPresent(RefreshToken::revoke);
	}

	@Transactional
	public void revokeAllForUser(Long userId) {
		refreshTokenRepository.revokeAllByUserId(userId);
	}

	private String generateRawToken() {
		byte[] bytes = new byte[32];
		secureRandom.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String rawToken) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
