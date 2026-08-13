package com.courier.delivery.client;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class RestParcelServiceClient implements ParcelServiceClient {

	private final RestClient parcelRestClient;

	@Override
	public void updateStatus(String parcelId, String parcelStatus) {
		try {
			parcelRestClient
					.put()
					.uri("/api/parcels/{id}/status", parcelId)
					.body(Map.of("status", parcelStatus))
					.retrieve()
					.toBodilessEntity();
			log.info("Synced parcel {} status to {} via parcel-service HTTP", parcelId, parcelStatus);
		} catch (RestClientException ex) {
			log.warn(
					"Failed to sync parcel {} status to {} via HTTP: {}",
					parcelId,
					parcelStatus,
					ex.getMessage());
		}
	}
}
