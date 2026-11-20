package com.datasheet.backend.security;

import com.datasheet.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
		return userRepository.findByEmployeeId(employeeId)
				.map(CustomUserDetails::new)
				.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + employeeId));
	}
}
