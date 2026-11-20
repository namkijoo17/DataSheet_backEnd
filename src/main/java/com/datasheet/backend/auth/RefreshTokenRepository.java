package com.datasheet.backend.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	@Query("select r from RefreshToken r join fetch r.user where r.tokenHash = :tokenHash")
	Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);

	@Modifying(clearAutomatically = true)
	@Query("update RefreshToken r set r.revoked = true where r.user.id = :userId and r.revoked = false")
	int revokeAllByUserId(@Param("userId") Long userId);
}
