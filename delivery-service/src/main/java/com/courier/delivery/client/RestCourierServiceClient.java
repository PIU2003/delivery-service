package com.courier.delivery.client;

import com.courier.delivery.dto.CourierDto;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class RestCourierServiceClient implements CourierServiceClient {

	private final RestClient courierRestClient;

	@Override
	public List<CourierDto> findAvailable(String area) {
		try {
			List<CourierDto> couriers = courierRestClient
					.get()
					.uri(uriBuilder -> uriBuilder
							.path("/api/couriers/available")
							.queryParam("area", area)
							.build())
					.retrieve()
					.body(new ParameterizedTypeReference<>() {});
			return couriers != null ? couriers : Collections.emptyList();
		} catch (RestClientException ex) {
			log.error("Failed to fetch available couriers for area={}: {}", area, ex.getMessage());
			throw new IllegalStateException("Courier service unavailable: " + ex.getMessage(), ex);
		}
	}
}
