package br.com.raizesdonordeste.backend.api.dto.estoque;

import java.time.Instant;

import br.com.raizesdonordeste.backend.domain.enums.TipoMovimentoEstoque;

public record MovimentacaoEstoqueResponse(
	Long movimentacaoId,
	Long estoqueId,
	Long unidadeId,
	Long produtoId,
	TipoMovimentoEstoque tipo,
	Integer quantidade,
	Integer quantidadeAnterior,
	Integer quantidadeAtual,
	String motivo,
	Instant createdAt
) {
}
