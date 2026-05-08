package br.com.raizesdonordeste.backend.api.exception;

public record FieldErrorResponse(
	String field,
	String issue
) {
}
