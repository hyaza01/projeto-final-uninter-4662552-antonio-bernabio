package br.com.raizesdonordeste.backend.api.exception;

import java.util.List;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

	private final HttpStatus status;
	private final String errorCode;
	private final List<FieldErrorResponse> details;

	public BusinessException(HttpStatus status, String errorCode, String message) {
		this(status, errorCode, message, List.of());
	}

	public BusinessException(HttpStatus status, String errorCode, String message, List<FieldErrorResponse> details) {
		super(message);
		this.status = status;
		this.errorCode = errorCode;
		this.details = details == null ? List.of() : List.copyOf(details);
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public List<FieldErrorResponse> getDetails() {
		return details;
	}
}
