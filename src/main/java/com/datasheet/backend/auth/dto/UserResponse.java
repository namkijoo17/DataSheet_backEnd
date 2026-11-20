package com.datasheet.backend.auth.dto;

import com.datasheet.backend.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

	private Long id;
	private String employeeId;
	private String name;
	private String role;

	public static UserResponse from(User user) {
		return UserResponse.builder()
				.id(user.getId())
				.employeeId(user.getEmployeeId())
				.name(user.getName())
				.role(user.getRole())
				.build();
	}
}
