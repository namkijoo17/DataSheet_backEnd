package com.datasheet.backend.auth;

import com.datasheet.backend.security.AuthProperties;
import com.datasheet.backend.user.User;
import com.datasheet.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {

	private final UserRepository userRepository;
	private final AuthProperties authProperties;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public int recordFailure(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow();
		user.increaseFailedLoginCount();
		if (user.getFailedLoginCount() >= authProperties.getMaxFailedLogin()) {
			user.lock();
		}
		return user.getFailedLoginCount();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void resetFailures(Long userId) {
		userRepository.findById(userId).ifPresent(User::resetFailedLogin);
	}
}
