package com.datasheet.backend.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	/** Access Token 만료 (기본 30분) */
	private String secret;
	private long expirationMs = 1_800_000L;

	/** Refresh Token 만료 (기본 12시간) */
	private long refreshExpirationMs = 43_200_000L;
}
