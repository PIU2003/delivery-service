package com.courier.delivery.config;

import com.courier.delivery.client.CourierServiceClient;
import com.courier.delivery.dto.CourierDto;
import com.courier.delivery.entity.Delivery;
import com.courier.delivery.messaging.DeliveryEventPublisher;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestBeansConfig {

	@Bean
	@Primary
	public CourierServiceClient stubCourierServiceClient() {
		return area -> List.of(CourierDto.builder()
				.id("test-courier-id")
				.name("Test Courier")
				.phone("000")
				.vehicleType("Bike")
				.currentArea(area)
				.isAvailable(true)
				.build());
	}

	@Bean
	@Primary
	public DeliveryEventPublisher noOpDeliveryEventPublisher() {
		return new DeliveryEventPublisher() {
			@Override
			public void publishAssigned(Delivery delivery) {
				// no-op in tests
			}

			@Override
			public void publishPickedUp(Delivery delivery) {
				// no-op in tests
			}

			@Override
			public void publishDelivered(Delivery delivery) {
				// no-op in tests
			}
		};
	}
}
