package br.com.raizesdonordeste.backend.api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.raizesdonordeste.backend.api.dto.auditoria.AuditoriaResponse;
import br.com.raizesdonordeste.backend.infrastructure.audit.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/auditorias")
@Tag(name = "Auditoria", description = "Consulta de trilha de auditoria")
public class AuditoriaController {

	private final AuditoriaService auditoriaService;

	@GetMapping
	@Operation(summary = "Listar auditorias", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<Page<AuditoriaResponse>> listarAuditorias(
		@RequestParam(required = false) String entidade,
		@RequestParam(required = false) Long entidadeId,
		@RequestParam(required = false) String acao,
		@RequestParam(required = false) Long usuarioId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int limit,
		@RequestParam(defaultValue = "createdAt,desc") String sort
	) {
		PageRequest pageable = PageRequest.of(
			Math.max(page, 0),
			Math.max(1, Math.min(limit, 100)),
			resolveSort(sort)
		);

		return ResponseEntity.ok(auditoriaService.listar(entidade, entidadeId, acao, usuarioId, pageable));
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
