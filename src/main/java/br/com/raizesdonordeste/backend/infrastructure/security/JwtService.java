package br.com.raizesdonordeste.backend.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${app.security.jwt.secret}")
	private String jwtSecret;

	@Value("${app.security.jwt.expiration-minutes}")
	private long expirationMinutes;

	public String generateToken(String username, String role) {
		Instant now = Instant.now();

		return Jwts.builder()
			.subject(username)
			.claim("role", role)
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
			.signWith(getSigningKey())
			.compact();
	}

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractAllClaims(token).getExpiration().before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
			.verifyWith(getSigningKey())
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	private SecretKey getSigningKey() {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
				.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
			return Keys.hmacShaKeyFor(digest);
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("Falha ao inicializar chave JWT", ex);
		}
	}
}
