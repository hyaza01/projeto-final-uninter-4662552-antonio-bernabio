package br.com.raizesdonordeste.backend.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.pagamento.PagamentoMockRequest;
import br.com.raizesdonordeste.backend.api.dto.pagamento.PagamentoRequest;
import br.com.raizesdonordeste.backend.api.dto.pagamento.PagamentoResponse;
import br.com.raizesdonordeste.backend.application.services.PagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "Pagamentos", description = "Processamento de pagamento mock para pedidos")
public class PagamentoController {

	private final PagamentoService pagamentoService;

	@PostMapping("/pedidos/{pedidoId}/pagamentos/mock")
	@Operation(summary = "Processar pagamento mock do pedido", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PagamentoResponse> processarPagamentoMock(
		@PathVariable Long pedidoId,
		@Valid @RequestBody PagamentoMockRequest request,
		Authentication authentication
	) {
		return ResponseEntity.ok(pagamentoService.processarPagamentoMock(pedidoId, request, authentication.getName()));
	}

	@PostMapping("/pedidos/{pedidoId}/pagamento")
	@Operation(summary = "Processar pagamento do pedido (legado)", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PagamentoResponse> processarPagamento(
		@PathVariable Long pedidoId,
		@Valid @RequestBody PagamentoRequest request,
		Authentication authentication
	) {
		return ResponseEntity.ok(pagamentoService.processarPagamento(pedidoId, request, authentication.getName()));
	}

	@GetMapping("/pagamentos/{pagamentoId}")
	@Operation(summary = "Consultar pagamento por id", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PagamentoResponse> buscarPagamento(
		@PathVariable Long pagamentoId,
		Authentication authentication
	) {
		return ResponseEntity.ok(pagamentoService.buscarPagamento(pagamentoId, authentication.getName()));
	}
}
