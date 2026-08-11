package com.courier.courier.config;

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
	public OpenAPI courierOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Courier Service — fleet & availability")
						.description(
								"Courier Service APIs for fleet profiles, area lookup, and shift availability. "
										+ "Protect endpoints with the X-API-KEY header.")
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
