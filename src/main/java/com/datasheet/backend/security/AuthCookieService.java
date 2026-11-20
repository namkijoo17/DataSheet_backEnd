package com.datasheet.backend.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCookieService {

	public static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
	public static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";

	public ResponseCookie createAccessTokenCookie(String token, long expiresInMs) {
		return baseCookie(ACCESS_TOKEN_COOKIE, token)
				.maxAge(Duration.ofMillis(expiresInMs))
				.build();
	}

	public ResponseCookie createRefreshTokenCookie(String token, long expiresInMs) {
		return baseCookie(REFRESH_TOKEN_COOKIE, token)
				.maxAge(Duration.ofMillis(expiresInMs))
				.build();
	}

	public ResponseCookie clearAccessTokenCookie() {
		return baseCookie(ACCESS_TOKEN_COOKIE, "")
				.maxAge(0)
				.build();
	}

	public ResponseCookie clearRefreshTokenCookie() {
		return baseCookie(REFRESH_TOKEN_COOKIE, "")
				.maxAge(0)
				.build();
	}

	public String toHeaderValue(ResponseCookie cookie) {
		return cookie.toString();
	}

	public String setCookieHeaderName() {
		return HttpHeaders.SET_COOKIE;
	}

	private ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value) {
		return ResponseCookie.from(name, value)
				.httpOnly(true)
				.secure(false)
				.path("/")
				.sameSite("Lax");
	}
}
