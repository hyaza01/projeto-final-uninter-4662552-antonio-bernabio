package br.com.raizesdonordeste.backend.api.dto.auditoria;

import java.time.Instant;

public record AuditoriaResponse(
	Long id,
	Long usuarioId,
	String usuarioEmail,
	String acao,
	String entidade,
	Long entidadeId,
	String detalhes,
	String ipOrigem,
	Instant createdAt
) {
}
