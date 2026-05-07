package br.com.raizesdonordeste.backend.api.dto.pedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import br.com.raizesdonordeste.backend.domain.enums.CanalPedido;
import br.com.raizesdonordeste.backend.domain.enums.FormaPagamento;
import br.com.raizesdonordeste.backend.domain.enums.StatusPedido;

public record PedidoResponse(
	Long id,
	Long clienteId,
	Long unidadeId,
	String unidadeNome,
	CanalPedido canalPedido,
	FormaPagamento formaPagamento,
	StatusPedido status,
	BigDecimal subtotal,
	BigDecimal total,
	Instant criadoEm,
	List<PedidoItemResponse> itens
) {
}
