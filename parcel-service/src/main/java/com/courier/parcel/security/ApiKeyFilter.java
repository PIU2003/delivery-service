package com.courier.parcel.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

	@Value("${app.api-key}")
	private String apiKey;

	@Value("${app.api-key-header:X-API-KEY}")
	private String apiKeyHeader;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return path.startsWith("/actuator")
				|| path.startsWith("/swagger-ui")
				|| path.startsWith("/v3/api-docs")
				|| path.equals("/swagger-ui.html");
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String provided = request.getHeader(apiKeyHeader);
		if (provided == null || !provided.equals(apiKey)) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.getWriter()
					.write("{\"error\":\"Unauthorized\",\"message\":\"Missing or invalid API key\"}");
			return;
		}
		filterChain.doFilter(request, response);
	}
}
