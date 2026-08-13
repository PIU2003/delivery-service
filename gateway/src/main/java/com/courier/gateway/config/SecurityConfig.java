package com.courier.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	SecurityWebFilterChain springSecurityFilterChain(
			ServerHttpSecurity http,
			Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter) {
		http.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers(HttpMethod.OPTIONS, "/**")
						.permitAll()
						.pathMatchers("/auth/**", "/actuator/**")
						.permitAll()
						// Public customer tracking — no login required
						.pathMatchers(HttpMethod.GET, "/api/parcels/*/status")
						.permitAll()
						.pathMatchers(HttpMethod.GET, "/api/parcels/*")
						.permitAll()
						.pathMatchers(HttpMethod.GET, "/api/deliveries/track/**")
						.permitAll()
						// Ops console APIs require ADMIN
						.pathMatchers("/api/**")
						.hasRole("ADMIN")
						.anyExchange()
						.permitAll())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(
						jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		return http.build();
	}
}
