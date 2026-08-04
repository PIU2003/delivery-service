package com.courier.auth.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Service
public class JwtService {

	private final JwtEncoder jwtEncoder;
	private final String issuer;
	private final long expirationMinutes;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.issuer}") String issuer,
			@Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
		this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
		this.issuer = issuer;
		this.expirationMinutes = expirationMinutes;
	}

	public String createToken(String username) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(expirationMinutes * 60);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(issuer)
				.issuedAt(now)
				.expiresAt(expiresAt)
				.subject(username)
				.claim("roles", List.of("USER"))
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

	public long getExpirationSeconds() {
		return expirationMinutes * 60;
	}
}
