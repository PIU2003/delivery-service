package com.courier.gateway.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import reactor.core.publisher.Mono;

@Configuration
public class JwtAuthConverterConfig {

	@Bean
	Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
		return new ReactiveJwtAuthenticationConverterAdapter(converter);
	}

	private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
		Object claim = jwt.getClaim("roles");
		List<String> roles = new ArrayList<>();
		if (claim instanceof Collection<?> collection) {
			for (Object item : collection) {
				if (item != null) {
					roles.add(item.toString());
				}
			}
		} else if (claim instanceof String single) {
			roles.add(single);
		}
		if (roles.isEmpty()) {
			roles.add("USER");
		}
      Collection<GrantedAuthority> authorities = new ArrayList<>();
		for (String role : roles) {
			String name = role.startsWith("ROLE_") ? role : "ROLE_" + role;
			authorities.add(new SimpleGrantedAuthority(name));
		}
		return authorities;
	}
}
