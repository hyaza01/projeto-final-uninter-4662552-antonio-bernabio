package br.com.raizesdonordeste.backend.api.dto.pedido;

import java.util.List;

import br.com.raizesdonordeste.backend.domain.enums.CanalPedido;
import br.com.raizesdonordeste.backend.domain.enums.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PedidoCreateRequest(
	@NotNull(message = "Unidade e obrigatoria")
	Long unidadeId,

	@NotNull(message = "Canal do pedido e obrigatorio")
	CanalPedido canalPedido,

	@NotNull(message = "Forma de pagamento e obrigatoria")
	FormaPagamento formaPagamento,

	@NotEmpty(message = "Pedido deve conter ao menos um item")
	List<@Valid PedidoItemCreateRequest> itens
) {
}
