package br.com.raizesdonordeste.backend.infrastructure.security;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.raizesdonordeste.backend.api.exception.ApiErrorFactory;
import br.com.raizesdonordeste.backend.api.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	public SecurityExceptionHandler(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) throws IOException {
		ApiErrorResponse body = ApiErrorFactory.build(
			"UNAUTHORIZED",
			"Autenticacao necessaria para acessar este recurso.",
			List.of(),
			request
		);

		write(response, HttpStatus.UNAUTHORIZED, body);
	}

	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException {
		ApiErrorResponse body = ApiErrorFactory.build(
			"FORBIDDEN",
			"Usuario sem permissao para acessar este recurso.",
			List.of(),
			request
		);

		write(response, HttpStatus.FORBIDDEN, body);
	}

	private void write(HttpServletResponse response, HttpStatus status, ApiErrorResponse body) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getWriter(), body);
	}
}
