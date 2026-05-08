package br.com.raizesdonordeste.backend.api.exception;

import java.util.List;

import org.springframework.http.HttpStatus;

public class ValidationException extends BusinessException {

	public ValidationException(String message, List<FieldErrorResponse> details) {
		super(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_ERROR", message, details);
	}
}
