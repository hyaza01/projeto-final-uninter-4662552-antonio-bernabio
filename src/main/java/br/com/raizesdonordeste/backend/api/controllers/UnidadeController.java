package br.com.raizesdonordeste.backend.api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.catalogo.CardapioUnidadeItemResponse;
import br.com.raizesdonordeste.backend.application.services.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/unidades")
@Tag(name = "Unidades", description = "Consultas publicas de unidade e cardapio")
public class UnidadeController {

	private final CatalogoService catalogoService;

	@GetMapping("/{unidadeId}/cardapio")
	@Operation(summary = "Listar cardapio por unidade")
	public ResponseEntity<List<CardapioUnidadeItemResponse>> listarCardapioPorUnidade(@PathVariable Long unidadeId) {
		return ResponseEntity.ok(catalogoService.listarCardapioPorUnidade(unidadeId));
	}
}
