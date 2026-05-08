package br.com.raizesdonordeste.backend.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.fidelidade.FidelidadeConsentimentoRequest;
import br.com.raizesdonordeste.backend.api.dto.fidelidade.FidelidadeHistoricoResponse;
import br.com.raizesdonordeste.backend.api.dto.fidelidade.FidelidadeSaldoResponse;
import br.com.raizesdonordeste.backend.application.services.FidelidadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fidelidade")
@Tag(name = "Fidelidade", description = "Consulta de saldo e historico de pontos com consentimento")
public class FidelidadeController {

	private final FidelidadeService fidelidadeService;

	@GetMapping("/me")
	@Operation(summary = "Consultar saldo de fidelidade", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<FidelidadeSaldoResponse> saldo(Authentication authentication) {
		return ResponseEntity.ok(fidelidadeService.consultarSaldo(authentication.getName()));
	}

	@GetMapping("/me/historico")
	@Operation(summary = "Listar historico de pontos", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<List<FidelidadeHistoricoResponse>> historico(Authentication authentication) {
		return ResponseEntity.ok(fidelidadeService.listarHistorico(authentication.getName()));
	}

	@PatchMapping("/me/consentimento")
	@Operation(summary = "Atualizar consentimento do programa de fidelidade", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<FidelidadeSaldoResponse> atualizarConsentimento(
		@Valid @RequestBody FidelidadeConsentimentoRequest request,
		Authentication authentication
	) {
		return ResponseEntity.ok(
			fidelidadeService.atualizarConsentimento(authentication.getName(), request.consentimentoFidelidade())
		);
	}
}
