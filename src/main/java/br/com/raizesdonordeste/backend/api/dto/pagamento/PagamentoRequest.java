package br.com.raizesdonordeste.backend.api.dto.pagamento;

import br.com.raizesdonordeste.backend.domain.enums.FormaPagamento;
import jakarta.validation.constraints.NotNull;

public record PagamentoRequest(
	@NotNull(message = "Forma de pagamento e obrigatoria")
	FormaPagamento formaPagamento,

	Boolean forcarAprovacao
) {
}
