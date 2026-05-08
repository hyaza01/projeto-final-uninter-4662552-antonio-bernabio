package br.com.raizesdonordeste.backend.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/api/v1/clientes/{clienteId}/fidelidade")
@Tag(name = "Fidelidade", description = "Consulta de fidelidade por cliente")
public class ClienteFidelidadeController {

	private final FidelidadeService fidelidadeService;

	@GetMapping("/saldo")
	@Operation(summary = "Consultar saldo de fidelidade por cliente", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<FidelidadeSaldoResponse> saldo(
		@PathVariable Long clienteId,
		Authentication authentication
	) {
		return ResponseEntity.ok(fidelidadeService.consultarSaldoPorClienteId(clienteId, authentication.getName()));
	}

	@GetMapping("/historico")
	@Operation(summary = "Consultar historico de fidelidade por cliente", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<List<FidelidadeHistoricoResponse>> historico(
		@PathVariable Long clienteId,
		Authentication authentication
	) {
		return ResponseEntity.ok(fidelidadeService.listarHistoricoPorClienteId(clienteId, authentication.getName()));
	}

	@PatchMapping("/consentimento")
	@Operation(summary = "Atualizar consentimento de fidelidade por cliente", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<FidelidadeSaldoResponse> atualizarConsentimento(
		@PathVariable Long clienteId,
		@Valid @RequestBody FidelidadeConsentimentoRequest request,
		Authentication authentication
	) {
		return ResponseEntity.ok(
			fidelidadeService.atualizarConsentimentoPorClienteId(
				clienteId,
				request.consentimentoFidelidade(),
				authentication.getName()
			)
		);
	}
}
