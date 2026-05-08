package br.com.raizesdonordeste.backend.api.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.estoque.EstoqueConsultaResponse;
import br.com.raizesdonordeste.backend.api.dto.estoque.MovimentacaoEstoqueRequest;
import br.com.raizesdonordeste.backend.api.dto.estoque.MovimentacaoEstoqueResponse;
import br.com.raizesdonordeste.backend.application.services.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/estoque")
@Tag(name = "Estoque", description = "Consulta e movimentacao de estoque por unidade")
public class EstoqueController {

	private final EstoqueService estoqueService;

	@GetMapping("/unidades/{unidadeId}")
	@Operation(summary = "Listar estoque de uma unidade", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<List<EstoqueConsultaResponse>> listarPorUnidade(@PathVariable Long unidadeId) {
		return ResponseEntity.ok(estoqueService.listarPorUnidade(unidadeId));
	}

	@GetMapping("/unidades/{unidadeId}/produtos/{produtoId}")
	@Operation(summary = "Consultar estoque de produto na unidade", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<EstoqueConsultaResponse> buscarPorUnidadeEProduto(
		@PathVariable Long unidadeId,
		@PathVariable Long produtoId
	) {
		return ResponseEntity.ok(estoqueService.buscarPorUnidadeEProduto(unidadeId, produtoId));
	}

	@PostMapping("/movimentacoes")
	@Operation(summary = "Registrar movimentacao manual de estoque", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<MovimentacaoEstoqueResponse> registrarMovimentacao(
		@Valid @RequestBody MovimentacaoEstoqueRequest request,
		Authentication authentication
	) {
		MovimentacaoEstoqueResponse response = estoqueService.registrarMovimentacaoManual(request, authentication.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{estoqueId}/movimentacoes")
	@Operation(summary = "Listar movimentacoes de um estoque", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Page<MovimentacaoEstoqueResponse>> listarMovimentacoes(
		@PathVariable Long estoqueId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int limit,
		@RequestParam(defaultValue = "createdAt,desc") String sort
	) {
		PageRequest pageable = PageRequest.of(
			Math.max(page, 0),
			Math.max(1, Math.min(limit, 100)),
			resolveSort(sort)
		);

		return ResponseEntity.ok(estoqueService.listarMovimentacoesPorEstoque(estoqueId, pageable));
	}

	private Sort resolveSort(String sort) {
		if (sort == null || sort.isBlank()) {
			return Sort.by(Sort.Direction.DESC, "createdAt");
		}

		String[] chunks = sort.split(",");
		String field = chunks[0].trim().isBlank() ? "createdAt" : chunks[0].trim();
		Sort.Direction direction = chunks.length > 1 && "asc".equalsIgnoreCase(chunks[1].trim())
			? Sort.Direction.ASC
			: Sort.Direction.DESC;
		return Sort.by(direction, field);
	}
}
