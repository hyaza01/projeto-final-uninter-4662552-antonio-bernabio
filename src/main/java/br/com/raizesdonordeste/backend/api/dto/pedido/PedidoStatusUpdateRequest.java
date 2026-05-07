package br.com.raizesdonordeste.backend.api.dto.pedido;

import br.com.raizesdonordeste.backend.domain.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record PedidoStatusUpdateRequest(
	@NotNull(message = "Novo status e obrigatorio")
	StatusPedido novoStatus
) {
}
