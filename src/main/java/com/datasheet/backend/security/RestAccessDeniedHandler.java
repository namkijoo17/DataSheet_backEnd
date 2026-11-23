package com.datasheet.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	@Override
	public void handle(
			HttpServletRequest request,
			HttpServletResponse response,
			org.springframework.security.access.AccessDeniedException accessDeniedException
	) throws IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// 비로그인(Anonymous)은 403이 아니라 401
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			RestAuthenticationEntryPoint.writeUnauthorized(response);
			return;
		}

		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("{\"code\":\"FORBIDDEN\",\"message\":\"접근 권한이 없습니다.\"}");
	}
}
