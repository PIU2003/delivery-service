package com.courier.delivery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("!test")
public class RestClientConfig {

	@Bean
	public RestClient courierRestClient(
			@Value("${app.courier-service.base-url}") String baseUrl,
			@Value("${app.courier-service.api-key}") String apiKey,
			@Value("${app.api-key-header:X-API-KEY}") String apiKeyHeader) {
		return RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader(apiKeyHeader, apiKey)
				.build();
	}

	@Bean
	public RestClient parcelRestClient(
			@Value("${app.parcel-service.base-url}") String baseUrl,
			@Value("${app.parcel-service.api-key}") String apiKey,
			@Value("${app.api-key-header:X-API-KEY}") String apiKeyHeader) {
		return RestClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader(apiKeyHeader, apiKey)
				.build();
	}
}
