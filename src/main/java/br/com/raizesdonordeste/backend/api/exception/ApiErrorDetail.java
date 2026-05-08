package br.com.raizesdonordeste.backend.api.exception;

public record ApiErrorDetail(
	String field,
	String issue
) {
}
