package br.com.raizesdonordeste.backend.api.dto.estoque;

import br.com.raizesdonordeste.backend.domain.enums.TipoMovimentoEstoque;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MovimentacaoEstoqueRequest(
	@NotNull(message = "Unidade e obrigatoria")
	Long unidadeId,

	@NotNull(message = "Produto e obrigatorio")
	Long produtoId,

	@NotNull(message = "Tipo de movimentacao e obrigatorio")
	TipoMovimentoEstoque tipo,

	@NotNull(message = "Quantidade e obrigatoria")
	@Positive(message = "Quantidade deve ser maior que zero")
	Integer quantidade,

	@NotBlank(message = "Motivo e obrigatorio")
	String motivo
) {
	@AssertTrue(message = "Tipo deve ser ENTRADA ou SAIDA")
	public boolean isTipoValido() {
		return tipo == TipoMovimentoEstoque.ENTRADA || tipo == TipoMovimentoEstoque.SAIDA;
	}
}
