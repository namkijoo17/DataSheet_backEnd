package com.datasheet.backend.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthCookieService {

	public static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";

	public ResponseCookie createAccessTokenCookie(String token, long expiresInMs) {
		return ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
				.httpOnly(true)
				.secure(false)
				.path("/")
				.maxAge(Duration.ofMillis(expiresInMs))
				.sameSite("Lax")
				.build();
	}

	public ResponseCookie clearAccessTokenCookie() {
		return ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
				.httpOnly(true)
				.secure(false)
				.path("/")
				.maxAge(0)
				.sameSite("Lax")
				.build();
	}

	public String toHeaderValue(ResponseCookie cookie) {
		return cookie.toString();
	}

	public String setCookieHeaderName() {
		return HttpHeaders.SET_COOKIE;
	}
}
