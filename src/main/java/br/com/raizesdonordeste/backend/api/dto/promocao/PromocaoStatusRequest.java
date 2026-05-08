package br.com.raizesdonordeste.backend.api.dto.promocao;

import jakarta.validation.constraints.NotNull;

public record PromocaoStatusRequest(
	@NotNull(message = "Status da promocao e obrigatorio")
	Boolean ativa
) {
}
