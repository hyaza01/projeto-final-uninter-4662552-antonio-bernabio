package br.com.raizesdonordeste.backend.api.exception;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

public final class ApiErrorFactory {

	private ApiErrorFactory() {
	}

	public static ApiErrorResponse build(
		String error,
		String message,
		List<ApiErrorDetail> details,
		HttpServletRequest request
	) {
		String requestId = Optional.ofNullable(request.getHeader("X-Request-Id"))
			.map(String::trim)
			.filter(value -> !value.isBlank())
			.orElse(UUID.randomUUID().toString());

		return new ApiErrorResponse(
			error,
			message,
			details,
			Instant.now(),
			request.getRequestURI(),
			requestId
		);
	}
}
