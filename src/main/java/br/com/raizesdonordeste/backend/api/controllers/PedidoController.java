package br.com.raizesdonordeste.backend.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.pedido.PedidoCreateRequest;
import br.com.raizesdonordeste.backend.api.dto.pedido.PedidoResponse;
import br.com.raizesdonordeste.backend.api.dto.pedido.PedidoStatusUpdateRequest;
import br.com.raizesdonordeste.backend.application.services.PedidoService;
import br.com.raizesdonordeste.backend.domain.enums.CanalPedido;
import br.com.raizesdonordeste.backend.domain.enums.StatusPedido;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Gestao de pedidos de clientes e operacao interna")
public class PedidoController {

	private final PedidoService pedidoService;

	@PostMapping
	@Operation(summary = "Criar pedido", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PedidoResponse> criarPedido(
		@Valid @RequestBody PedidoCreateRequest request,
		Authentication authentication
	) {
		PedidoResponse response = pedidoService.criarPedido(request, authentication.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/me")
	@Operation(summary = "Listar meus pedidos", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Page<PedidoResponse>> listarMeusPedidos(
		Authentication authentication,
		@RequestParam(required = false) StatusPedido status,
		@RequestParam(required = false) CanalPedido canalPedido,
		@RequestParam(required = false) String dataInicio,
		@RequestParam(required = false) String dataFim,
		Pageable pageable
	) {
		return ResponseEntity.ok(
			pedidoService.listarMeusPedidos(authentication.getName(), status, canalPedido, dataInicio, dataFim, pageable)
		);
	}

	@GetMapping("/me/{pedidoId}")
	@Operation(summary = "Buscar um pedido do cliente autenticado", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PedidoResponse> buscarMeuPedido(
		@PathVariable Long pedidoId,
		Authentication authentication
	) {
		return ResponseEntity.ok(pedidoService.buscarMeuPedido(pedidoId, authentication.getName()));
	}

	@GetMapping
	@Operation(summary = "Listar pedidos com filtros operacionais", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Page<PedidoResponse>> listarPedidos(
		@RequestParam(required = false) Long unidadeId,
		@RequestParam(required = false) Long clienteId,
		@RequestParam(required = false) StatusPedido status,
		@RequestParam(required = false) CanalPedido canalPedido,
		@RequestParam(required = false) String dataInicio,
		@RequestParam(required = false) String dataFim,
		Pageable pageable
	) {
		return ResponseEntity.ok(
			pedidoService.listarPedidosComFiltros(unidadeId, clienteId, status, canalPedido, dataInicio, dataFim, pageable)
		);
	}

	@GetMapping("/unidade/{unidadeId}")
	@Operation(summary = "Listar pedidos de uma unidade", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<List<PedidoResponse>> listarPedidosDaUnidade(
		@PathVariable Long unidadeId,
		@RequestParam(required = false) StatusPedido status,
		@RequestParam(required = false) CanalPedido canalPedido,
		@RequestParam(required = false) String dataInicio,
		@RequestParam(required = false) String dataFim
	) {
		Page<PedidoResponse> page = pedidoService.listarPedidosComFiltros(
			unidadeId,
			null,
			status,
			canalPedido,
			dataInicio,
			dataFim,
			PageRequest.of(0, 100)
		);

		return ResponseEntity.ok(page.getContent());
	}

	@PatchMapping("/{pedidoId}/status")
	@Operation(summary = "Atualizar status do pedido", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PedidoResponse> atualizarStatus(
		@PathVariable Long pedidoId,
		@Valid @RequestBody PedidoStatusUpdateRequest request,
		Authentication authentication
	) {
		return ResponseEntity.ok(pedidoService.atualizarStatus(pedidoId, request.novoStatus(), authentication.getName()));
	}
}
