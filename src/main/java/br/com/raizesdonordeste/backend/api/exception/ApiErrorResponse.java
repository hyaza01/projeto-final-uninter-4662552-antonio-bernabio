package br.com.raizesdonordeste.backend.api.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
	String error,
	String message,
	List<ApiErrorDetail> details,
	Instant timestamp,
	String path,
	String requestId
) {
}
