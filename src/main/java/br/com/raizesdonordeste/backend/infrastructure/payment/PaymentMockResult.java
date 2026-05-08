package br.com.raizesdonordeste.backend.infrastructure.payment;

import br.com.raizesdonordeste.backend.domain.enums.StatusPagamento;

public record PaymentMockResult(
	StatusPagamento status,
	String payloadEnvio,
	String payloadRetorno,
	String codigoTransacao
) {
}
