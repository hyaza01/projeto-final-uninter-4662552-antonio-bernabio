package br.com.raizesdonordeste.backend.api.dto.pagamento;

import java.math.BigDecimal;
import java.time.Instant;

import br.com.raizesdonordeste.backend.domain.enums.FormaPagamento;
import br.com.raizesdonordeste.backend.domain.enums.StatusPagamento;
import br.com.raizesdonordeste.backend.domain.enums.StatusPedido;

public record PagamentoResponse(
	Long pagamentoId,
	Long pedidoId,
	StatusPagamento statusPagamento,
	StatusPedido statusPedido,
	FormaPagamento metodo,
	BigDecimal valor,
	String codigoTransacaoMock,
	Instant processadoEm,
	String mensagem
) {
}
