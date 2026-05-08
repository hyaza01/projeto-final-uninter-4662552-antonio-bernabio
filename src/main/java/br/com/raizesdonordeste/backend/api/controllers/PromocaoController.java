package br.com.raizesdonordeste.backend.api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.promocao.PromocaoCreateRequest;
import br.com.raizesdonordeste.backend.api.dto.promocao.PromocaoResponse;
import br.com.raizesdonordeste.backend.api.dto.promocao.PromocaoStatusRequest;
import br.com.raizesdonordeste.backend.application.services.PromocaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/promocoes")
@Tag(name = "Promocoes", description = "Gestao de promocoes e campanhas")
public class PromocaoController {

	private final PromocaoService promocaoService;

	@PostMapping
	@Operation(summary = "Criar promocao", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PromocaoResponse> criarPromocao(
		@Valid @RequestBody PromocaoCreateRequest request,
		Authentication authentication
	) {
		PromocaoResponse response = promocaoService.criarPromocao(request, authentication.getName());
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping
	@Operation(summary = "Listar promocoes", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Page<PromocaoResponse>> listarPromocoes(
		@RequestParam(required = false) Boolean ativa,
		@RequestParam(required = false) Long produtoId,
		@RequestParam(required = false) Long unidadeId,
		@RequestParam(required = false) String dataReferencia,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int limit,
		@RequestParam(defaultValue = "createdAt,desc") String sort
	) {
		PageRequest pageable = PageRequest.of(
			Math.max(page, 0),
			Math.max(1, Math.min(limit, 100)),
			resolveSort(sort)
		);

		return ResponseEntity.ok(promocaoService.listarPromocoes(ativa, produtoId, unidadeId, dataReferencia, pageable));
	}

	@PatchMapping("/{promocaoId}/status")
	@Operation(summary = "Alterar status de promocao", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<PromocaoResponse> atualizarStatus(
		@PathVariable Long promocaoId,
		@Valid @RequestBody PromocaoStatusRequest request,
		Authentication authentication
	) {
		return ResponseEntity.ok(promocaoService.atualizarStatus(promocaoId, request, authentication.getName()));
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
