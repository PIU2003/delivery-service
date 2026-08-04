package com.courier.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Value("${app.api-key-header:X-API-KEY}")
	private String apiKeyHeader;

	@Bean
	OpenAPI openAPI() {
		final String schemeName = "ApiKey";
		return new OpenAPI()
				.info(new Info()
						.title("Auth Service API")
						.description("JWT login and registration")
						.version("1.0"))
				.addSecurityItem(new SecurityRequirement().addList(schemeName))
				.components(new Components()
						.addSecuritySchemes(
								schemeName,
								new SecurityScheme()
										.name(apiKeyHeader)
										.type(SecurityScheme.Type.APIKEY)
										.in(SecurityScheme.In.HEADER)));
	}
}
