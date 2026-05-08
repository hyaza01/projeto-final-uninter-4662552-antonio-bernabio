package br.com.raizesdonordeste.backend.api.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
	String error,
	String message,
	List<FieldErrorResponse> details,
	Instant timestamp,
	String path,
	String requestId
) {
}
