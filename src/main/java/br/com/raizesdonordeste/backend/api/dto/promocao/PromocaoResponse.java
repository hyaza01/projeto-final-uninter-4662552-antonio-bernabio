package br.com.raizesdonordeste.backend.api.dto.promocao;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PromocaoResponse(
	Long id,
	String nome,
	String descricao,
	BigDecimal percentualDesconto,
	LocalDate dataInicio,
	LocalDate dataFim,
	boolean ativa,
	Long produtoId,
	Long unidadeId,
	Instant createdAt,
	Instant updatedAt
) {
}
