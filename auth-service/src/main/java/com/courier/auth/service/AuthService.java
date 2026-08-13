package com.courier.auth.service;

import com.courier.auth.dto.AuthResponse;
import com.courier.auth.dto.LoginRequest;
import com.courier.auth.dto.RegisterRequest;
import com.courier.auth.entity.Role;
import com.courier.auth.entity.User;
import com.courier.auth.exception.ConflictException;
import com.courier.auth.exception.UnauthorizedException;
import com.courier.auth.repository.UserRepository;
import com.courier.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		String username = request.getUsername().trim();
		if (userRepository.existsByUsername(username)) {
			throw new ConflictException("Username already exists: " + username);
		}
		User user = User.builder()
				.username(username)
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.role(Role.USER)
				.build();
		userRepository.save(user);
		return issueToken(user);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		User user = userRepository
				.findByUsername(request.getUsername().trim())
				.orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new UnauthorizedException("Invalid username or password");
		}
		return issueToken(user);
	}

	private AuthResponse issueToken(User user) {
		Role role = user.getRole() != null ? user.getRole() : Role.USER;
		return AuthResponse.builder()
				.accessToken(jwtService.createToken(user.getUsername(), role.name()))
				.tokenType("Bearer")
				.expiresInSeconds(jwtService.getExpirationSeconds())
				.username(user.getUsername())
				.role(role.name())
				.build();
	}
}
