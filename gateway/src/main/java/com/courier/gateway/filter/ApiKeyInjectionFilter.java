package com.courier.gateway.filter;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Strips any client-supplied API key and injects the correct downstream service key
 * so clients cannot call microservices with forged keys through the gateway.
 */
@Component
public class ApiKeyInjectionFilter implements GlobalFilter, Ordered {

	@Value("${app.api-key-header:X-API-KEY}")
	private String apiKeyHeader;

	@Value("${app.downstream-keys.parcel}")
	private String parcelApiKey;

	@Value("${app.downstream-keys.courier}")
	private String courierApiKey;

	@Value("${app.downstream-keys.delivery}")
	private String deliveryApiKey;

	@Value("${app.downstream-keys.auth}")
	private String authApiKey;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		String path = exchange.getRequest().getURI().getPath();
		String apiKey = resolveApiKey(path);
		if (apiKey == null) {
			return chain.filter(exchange);
		}

		ServerHttpRequest request = exchange.getRequest().mutate()
				.headers(headers -> {
					headers.remove(apiKeyHeader);
					headers.set(apiKeyHeader, apiKey);
				})
				.build();
		return chain.filter(exchange.mutate().request(request).build());
	}

	private String resolveApiKey(String path) {
		String normalized = path == null ? "" : path.toLowerCase(Locale.ROOT);
		if (normalized.startsWith("/api/parcels")) {
			return parcelApiKey;
		}
		if (normalized.startsWith("/api/couriers")) {
			return courierApiKey;
		}
		if (normalized.startsWith("/api/deliveries")) {
			return deliveryApiKey;
		}
		if (normalized.startsWith("/auth")) {
			return authApiKey;
		}
		return null;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 10;
	}
}
