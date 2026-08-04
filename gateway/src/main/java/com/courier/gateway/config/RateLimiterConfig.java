package com.courier.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

@Configuration
@Profile("!test")
public class RateLimiterConfig {

	@Bean
	KeyResolver ipKeyResolver() {
		return exchange -> {
			var remote = exchange.getRequest().getRemoteAddress();
			if (remote == null || remote.getAddress() == null) {
				return Mono.just("unknown");
			}
			return Mono.just(remote.getAddress().getHostAddress());
		};
	}

	@Bean
	RedisRateLimiter redisRateLimiter() {
		return new RedisRateLimiter(20, 40, 1);
	}
}
