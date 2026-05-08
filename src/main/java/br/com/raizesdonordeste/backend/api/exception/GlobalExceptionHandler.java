package br.com.raizesdonordeste.backend.api.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
		List<FieldErrorResponse> details = ex.getBindingResult()
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
		List<FieldErrorResponse> details = ex.getConstraintViolations()
			.stream()
			.map(violation -> new FieldErrorResponse(violation.getPropertyPath().toString(), violation.getMessage()))
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
			"BAD_REQUEST",
			"Corpo da requisicao invalido.",
			List.of(),
			request
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingParameter(
		MissingServletRequestParameterException ex,
		HttpServletRequest request
	) {
		ApiErrorResponse body = ApiErrorFactory.build(
			"BAD_REQUEST",
			"Parametro obrigatorio ausente: " + ex.getParameterName(),
			List.of(new FieldErrorResponse(ex.getParameterName(), "parametro obrigatorio")),
			request
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
		MethodArgumentTypeMismatchException ex,
		HttpServletRequest request
	) {
		ApiErrorResponse body = ApiErrorFactory.build(
			"BAD_REQUEST",
			"Parametro com tipo invalido: " + ex.getName(),
			List.of(new FieldErrorResponse(ex.getName(), "tipo invalido")),
			request
		);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleBusinessException(
		BusinessException ex,
		HttpServletRequest request
	) {
		ApiErrorResponse body = ApiErrorFactory.build(
			ex.getErrorCode(),
			ex.getMessage(),
			ex.getDetails(),
			request
		);

		return ResponseEntity.status(ex.getStatus()).body(body);
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

	private FieldErrorResponse toDetail(FieldError fieldError) {
		String field = fieldError.getField();
		String message = fieldError.getDefaultMessage() == null ? "valor invalido" : fieldError.getDefaultMessage();
		return new FieldErrorResponse(field, message);
	}

	private String resolveErrorCode(HttpStatusCode statusCode) {
		HttpStatus status = HttpStatus.resolve(statusCode.value());
		if (status == null) {
			return "REQUEST_ERROR";
		}

		return switch (status) {
			case NOT_FOUND -> "NOT_FOUND";
			case BAD_REQUEST -> "BAD_REQUEST";
			case UNPROCESSABLE_ENTITY -> "VALIDATION_ERROR";
			case CONFLICT -> "CONFLICT";
			case FORBIDDEN -> "FORBIDDEN";
			case UNAUTHORIZED -> "UNAUTHORIZED";
			default -> "REQUEST_ERROR";
		};
	}
}
