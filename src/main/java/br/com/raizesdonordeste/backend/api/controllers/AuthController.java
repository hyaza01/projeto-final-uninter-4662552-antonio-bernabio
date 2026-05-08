package br.com.raizesdonordeste.backend.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.auth.LoginRequest;
import br.com.raizesdonordeste.backend.api.dto.auth.LoginResponse;
import br.com.raizesdonordeste.backend.api.dto.auth.RegisterRequest;
import br.com.raizesdonordeste.backend.api.dto.auth.UserResponse;
import br.com.raizesdonordeste.backend.application.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Cadastro, login e identificacao do usuario autenticado")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	@Operation(summary = "Registrar novo cliente")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
	}

	@PostMapping("/login")
	@Operation(summary = "Autenticar usuario e gerar JWT")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@GetMapping("/me")
	@Operation(summary = "Retornar dados do usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<UserResponse> me(Authentication authentication) {
		return ResponseEntity.ok(authService.me(authentication.getName()));
	}
}
