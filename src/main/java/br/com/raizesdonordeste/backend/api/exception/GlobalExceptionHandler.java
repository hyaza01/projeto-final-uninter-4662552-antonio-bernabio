package br.com.raizesdonordeste.backend.api.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
		MethodArgumentNotValidException ex,
		HttpServletRequest request
	) {
		List<ApiErrorDetail> details = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(this::toDetail)
			.toList();

		ApiErrorResponse body = ApiErrorFactory.build(
			"VALIDATION_ERROR",
			"Falha de validacao dos campos enviados.",
			details,
			request
		);

		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
		ConstraintViolationException ex,
		HttpServletRequest request
	) {
		List<ApiErrorDetail> details = ex.getConstraintViolations()
			.stream()
			.map(violation -> new ApiErrorDetail(violation.getPropertyPath().toString(), violation.getMessage()))
			.toList();

		ApiErrorResponse body = ApiErrorFactory.build(
			"VALIDATION_ERROR",
			"Falha de validacao dos campos enviados.",
			details,
			request
		);

		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleNotReadable(
		HttpMessageNotReadableException ex,
		HttpServletRequest request
	) {
		ApiErrorResponse body = ApiErrorFactory.build(
			"VALIDATION_ERROR",
			"Corpo da requisicao invalido.",
			List.of(),
			request
		);

		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatus(
		ResponseStatusException ex,
		HttpServletRequest request
	) {
		HttpStatusCode statusCode = ex.getStatusCode();
		String message = ex.getReason() != null ? ex.getReason() : "Falha ao processar requisicao.";

		ApiErrorResponse body = ApiErrorFactory.build(
			resolveErrorCode(statusCode),
			message,
			List.of(),
			request
		);

		return ResponseEntity.status(statusCode).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(
		Exception ex,
		HttpServletRequest request
	) {
		ApiErrorResponse body = ApiErrorFactory.build(
			"INTERNAL_ERROR",
			"Erro interno inesperado.",
			List.of(),
			request
		);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	private ApiErrorDetail toDetail(FieldError fieldError) {
		String field = fieldError.getField();
		String message = fieldError.getDefaultMessage() == null ? "valor invalido" : fieldError.getDefaultMessage();
		return new ApiErrorDetail(field, message);
	}

	private String resolveErrorCode(HttpStatusCode statusCode) {
		HttpStatus status = HttpStatus.resolve(statusCode.value());
		if (status == null) {
			return "REQUEST_ERROR";
		}

		return switch (status) {
			case NOT_FOUND -> "NOT_FOUND";
			case BAD_REQUEST, UNPROCESSABLE_ENTITY -> "VALIDATION_ERROR";
			case CONFLICT -> "CONFLICT";
			case FORBIDDEN -> "FORBIDDEN";
			case UNAUTHORIZED -> "UNAUTHORIZED";
			default -> "REQUEST_ERROR";
		};
	}
}
