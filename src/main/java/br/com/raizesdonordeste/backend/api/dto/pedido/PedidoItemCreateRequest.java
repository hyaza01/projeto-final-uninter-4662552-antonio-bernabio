package br.com.raizesdonordeste.backend.api.dto.pedido;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PedidoItemCreateRequest(
	@NotNull(message = "Produto e obrigatorio")
	Long produtoId,

	@NotNull(message = "Quantidade e obrigatoria")
	@Min(value = 1, message = "Quantidade deve ser maior que zero")
	Integer quantidade
) {
}