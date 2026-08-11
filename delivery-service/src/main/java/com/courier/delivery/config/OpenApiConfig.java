package com.courier.delivery.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Value("${app.api-key-header:X-API-KEY}")
	private String apiKeyHeader;

	@Bean
	public OpenAPI deliveryOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Delivery Service — orchestration & tracking")
						.description(
								"Delivery Service APIs for assigning couriers, advancing pickup/complete, "
										+ "listing active runs, and tracking by parcel. Protect with X-API-KEY.")
						.version("v1"))
				.components(new Components()
						.addSecuritySchemes(
								"ApiKey",
								new SecurityScheme()
										.type(SecurityScheme.Type.APIKEY)
										.in(SecurityScheme.In.HEADER)
										.name(apiKeyHeader)));
	}
}
