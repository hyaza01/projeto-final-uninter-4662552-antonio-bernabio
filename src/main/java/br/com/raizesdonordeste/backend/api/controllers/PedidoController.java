package br.com.raizesdonordeste.backend.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.pedido.PedidoCreateRequest;
import br.com.raizesdonordeste.backend.api.dto.pedido.PedidoResponse;
import br.com.raizesdonordeste.backend.api.dto.pedido.PedidoStatusUpdateRequest;
import br.com.raizesdonordeste.backend.application.services.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;

	@PostMapping
	public ResponseEntity<PedidoResponse> criarPedido(
		@Valid @RequestBody PedidoCreateRequest request,
		Authentication authentication
	) {
		PedidoResponse response = pedidoService.criarPedido(request, authentication.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/me")
	public ResponseEntity<List<PedidoResponse>> listarMeusPedidos(Authentication authentication) {
		return ResponseEntity.ok(pedidoService.listarMeusPedidos(authentication.getName()));
	}

	@GetMapping("/me/{pedidoId}")
	public ResponseEntity<PedidoResponse> buscarMeuPedido(
		@PathVariable Long pedidoId,
		Authentication authentication
	) {
		return ResponseEntity.ok(pedidoService.buscarMeuPedido(pedidoId, authentication.getName()));
	}

	@GetMapping("/unidade/{unidadeId}")
	public ResponseEntity<List<PedidoResponse>> listarPedidosPorUnidade(@PathVariable Long unidadeId) {
		return ResponseEntity.ok(pedidoService.listarPedidosPorUnidade(unidadeId));
	}

	@PatchMapping("/{pedidoId}/status")
	public ResponseEntity<PedidoResponse> atualizarStatus(
		@PathVariable Long pedidoId,
		@Valid @RequestBody PedidoStatusUpdateRequest request
	) {
		return ResponseEntity.ok(pedidoService.atualizarStatus(pedidoId, request.novoStatus()));
	}
}
