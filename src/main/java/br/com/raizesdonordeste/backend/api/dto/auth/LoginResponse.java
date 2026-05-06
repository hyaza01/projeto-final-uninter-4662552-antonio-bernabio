package br.com.raizesdonordeste.backend.api.dto.auth;

public record LoginResponse(
	String accessToken,
	String tokenType,
	UserResponse usuario
) {
}
