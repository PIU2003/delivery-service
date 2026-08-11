package com.courier.courier.config;

import com.courier.courier.entity.Courier;
import com.courier.courier.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Boots a small demo fleet when courierdb is empty.
 * Owned by Courier Service — areas cover Colombo, Kandy, and Galle for dispatch demos.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

	private final CourierRepository courierRepository;

	@Override
	public void run(String... args) {
		if (courierRepository.count() > 0) {
			log.debug("Courier fleet already seeded — skipping DataSeeder");
			return;
		}

		// Metro runners (Colombo)
		courierRepository.save(Courier.builder()
				.name("Kasun Bandara")
				.phone("+94771234567")
				.vehicleType("Motorcycle")
				.currentArea("Colombo")
				.isAvailable(true)
				.build());

		courierRepository.save(Courier.builder()
				.name("Dilani Wickramasinghe")
				.phone("+94772345678")
				.vehicleType("Van")
				.currentArea("Colombo")
				.isAvailable(true)
				.build());

		// Hill country
		courierRepository.save(Courier.builder()
				.name("Tharindu Gunasekara")
				.phone("+94773456789")
				.vehicleType("Bicycle")
				.currentArea("Kandy")
				.isAvailable(true)
				.build());

		// Southern coastal coverage
		courierRepository.save(Courier.builder()
				.name("Malsha Fernando")
				.phone("+94774567890")
				.vehicleType("Motorcycle")
				.currentArea("Galle")
				.isAvailable(true)
				.build());

		log.info("Courier Service seeded {} demo fleet members", courierRepository.count());
	}
}
