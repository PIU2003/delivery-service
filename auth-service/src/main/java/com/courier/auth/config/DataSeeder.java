package com.courier.auth.config;

import com.courier.auth.entity.User;
import com.courier.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.seed.username}")
	private String seedUsername;

	@Value("${app.seed.password}")
	private String seedPassword;

	@Override
	public void run(String... args) {
		if (!userRepository.existsByUsername(seedUsername)) {
			userRepository.save(User.builder()
					.username(seedUsername)
					.passwordHash(passwordEncoder.encode(seedPassword))
					.build());
			log.info("Seeded default user '{}'", seedUsername);
		}
	}
}
