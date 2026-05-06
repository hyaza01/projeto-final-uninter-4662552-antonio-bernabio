package br.com.raizesdonordeste.backend.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.estoque.EstoqueConsultaResponse;
import br.com.raizesdonordeste.backend.application.services.EstoqueService;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/estoque")
public class EstoqueController {

	private final EstoqueService estoqueService;

	@GetMapping("/unidades/{unidadeId}")
	public ResponseEntity<List<EstoqueConsultaResponse>> listarPorUnidade(@PathVariable Long unidadeId) {
		return ResponseEntity.ok(estoqueService.listarPorUnidade(unidadeId));
	}

	@GetMapping("/unidades/{unidadeId}/produtos/{produtoId}")
	public ResponseEntity<EstoqueConsultaResponse> buscarPorUnidadeEProduto(
		@PathVariable Long unidadeId,
		@PathVariable Long produtoId
	) {
		return ResponseEntity.ok(estoqueService.buscarPorUnidadeEProduto(unidadeId, produtoId));
	}
}
