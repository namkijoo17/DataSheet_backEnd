package com.datasheet.backend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "employee_id", nullable = false, length = 50, unique = true)
	private String employeeId;

	@Column(nullable = false, length = 255)
	private String password;

	@Column(nullable = false, length = 100, columnDefinition = "nvarchar(100)")
	private String name;

	@Column(nullable = false, length = 20)
	private String role;

	@Column(name = "failed_login_count", nullable = false)
	private int failedLoginCount = 0;

	@Column(nullable = false)
	private boolean locked = false;

	@Column(nullable = false)
	private boolean active = true;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Builder
	public User(String employeeId, String password, String name, String role) {
		this.employeeId = employeeId;
		this.password = password;
		this.name = name;
		this.role = role;
	}

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public void increaseFailedLoginCount() {
		this.failedLoginCount++;
	}

	public void lock() {
		this.locked = true;
	}

	public void resetFailedLogin() {
		this.failedLoginCount = 0;
	}
}
