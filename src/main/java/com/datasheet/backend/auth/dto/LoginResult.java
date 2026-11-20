package com.datasheet.backend.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResult {

	private String accessToken;
	private String refreshToken;
	private long refreshExpiresInMs;
	private LoginResponse response;
}
