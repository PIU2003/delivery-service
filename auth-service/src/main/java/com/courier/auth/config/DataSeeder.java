package com.courier.auth.config;

import com.courier.auth.entity.Role;
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

	@Value("${app.seed.customer-username:customer}")
	private String customerUsername;

	@Value("${app.seed.customer-password:password}")
	private String customerPassword;

	@Override
	public void run(String... args) {
		userRepository.findByUsername(seedUsername).ifPresentOrElse(user -> {
			if (user.getRole() != Role.ADMIN) {
				user.setRole(Role.ADMIN);
				userRepository.save(user);
				log.info("Updated seed user '{}' to ADMIN", seedUsername);
			}
		}, () -> {
			userRepository.save(User.builder()
					.username(seedUsername)
					.passwordHash(passwordEncoder.encode(seedPassword))
					.role(Role.ADMIN)
					.build());
			log.info("Seeded ADMIN user '{}'", seedUsername);
		});

		if (!userRepository.existsByUsername(customerUsername)) {
			userRepository.save(User.builder()
					.username(customerUsername)
					.passwordHash(passwordEncoder.encode(customerPassword))
					.role(Role.USER)
					.build());
			log.info("Seeded track-only USER '{}'", customerUsername);
		}
	}
}
