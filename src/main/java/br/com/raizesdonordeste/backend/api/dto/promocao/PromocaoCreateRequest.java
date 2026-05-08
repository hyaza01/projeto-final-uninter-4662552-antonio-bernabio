package br.com.raizesdonordeste.backend.api.dto.promocao;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PromocaoCreateRequest(
	@NotBlank(message = "Nome e obrigatorio")
	String nome,

	@NotBlank(message = "Descricao e obrigatoria")
	String descricao,

	@NotNull(message = "Percentual de desconto e obrigatorio")
	@DecimalMin(value = "0.01", message = "Percentual deve ser maior que zero")
	@DecimalMax(value = "100.00", message = "Percentual deve ser menor ou igual a 100")
	BigDecimal percentualDesconto,

	@NotNull(message = "Data de inicio e obrigatoria")
	LocalDate dataInicio,

	@NotNull(message = "Data de fim e obrigatoria")
	LocalDate dataFim,

	@NotNull(message = "Flag de status da promocao e obrigatoria")
	Boolean ativa,

	Long produtoId,
	Long unidadeId
) {
	@AssertTrue(message = "Data de fim deve ser igual ou posterior a data de inicio")
	public boolean isPeriodoValido() {
		if (dataInicio == null || dataFim == null) {
			return true;
		}
		return !dataFim.isBefore(dataInicio);
	}
}
