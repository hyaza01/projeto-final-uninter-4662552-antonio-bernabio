package br.com.raizesdonordeste.backend.api.dto.fidelidade;

import java.time.Instant;

import br.com.raizesdonordeste.backend.domain.enums.TipoMovimentacaoFidelidade;

public record FidelidadeHistoricoResponse(
	Long id,
	Long pedidoId,
	TipoMovimentacaoFidelidade tipo,
	Integer pontos,
	String descricao,
	Instant createdAt
) {
}
