package br.com.raizesdonordeste.backend.api.dto.pedido;

import java.math.BigDecimal;

public record PedidoItemResponse(
	Long produtoId,
	String produtoNome,
	Integer quantidade,
	BigDecimal precoUnitario,
	BigDecimal subtotal
) {
}
