package br.com.raizesdonordeste.backend.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.catalogo.ProdutoCatalogoResponse;
import br.com.raizesdonordeste.backend.application.services.CatalogoService;
import br.com.raizesdonordeste.backend.domain.enums.CategoriaProduto;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/catalogo")
public class CatalogoController {

	private final CatalogoService catalogoService;

	@GetMapping("/produtos")
	public ResponseEntity<List<ProdutoCatalogoResponse>> listarProdutos(
		@RequestParam(required = false) CategoriaProduto categoria
	) {
		return ResponseEntity.ok(catalogoService.listarProdutosAtivos(categoria));
	}

	@GetMapping("/produtos/{produtoId}")
	public ResponseEntity<ProdutoCatalogoResponse> buscarProduto(@PathVariable Long produtoId) {
		return ResponseEntity.ok(catalogoService.buscarProdutoAtivoPorId(produtoId));
	}
}
