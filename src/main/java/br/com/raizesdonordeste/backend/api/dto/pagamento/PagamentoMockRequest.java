package br.com.raizesdonordeste.backend.api.dto.pagamento;

import br.com.raizesdonordeste.backend.domain.enums.StatusPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PagamentoMockRequest(
	@NotNull(message = "Resultado do mock e obrigatorio")
	StatusPagamento resultadoMock,

	@NotBlank(message = "Metodo e obrigatorio")
	@Pattern(regexp = "(?i)MOCK", message = "Metodo deve ser MOCK")
	String metodo
) {
}