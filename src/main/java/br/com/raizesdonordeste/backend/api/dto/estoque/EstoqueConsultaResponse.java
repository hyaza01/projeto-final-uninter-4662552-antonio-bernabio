package br.com.raizesdonordeste.backend.api.dto.estoque;

public record EstoqueConsultaResponse(
	Long estoqueId,
	Long unidadeId,
	String unidadeNome,
	Long produtoId,
	String produtoNome,
	Integer quantidadeAtual,
	Integer estoqueMinimo,
	boolean abaixoDoMinimo
) {
}
