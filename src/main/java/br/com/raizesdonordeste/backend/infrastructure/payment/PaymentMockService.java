package br.com.raizesdonordeste.backend.infrastructure.payment;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.raizesdonordeste.backend.domain.enums.StatusPagamento;
import br.com.raizesdonordeste.backend.domain.model.Pedido;

@Service
public class PaymentMockService {

	public PaymentMockResult processar(Pedido pedido, Boolean forcarAprovacao) {
		boolean aprovado = forcarAprovacao != null
			? forcarAprovacao
			: (pedido.getId() % 2 == 0);

		StatusPagamento status = aprovado ? StatusPagamento.APROVADO : StatusPagamento.RECUSADO;
		String codigo = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);

		String payloadEnvio = String.format(
			"{\"pedidoId\":%d,\"valor\":%s,\"metodo\":\"%s\",\"timestamp\":\"%s\"}",
			pedido.getId(),
			pedido.getTotal(),
			pedido.getFormaPagamento().name(),
			Instant.now()
		);

		String payloadRetorno = String.format(
			"{\"status\":\"%s\",\"codigoTransacao\":\"%s\",\"provider\":\"PAYMENT_MOCK\"}",
			status.name(),
			codigo
		);

		return new PaymentMockResult(status, payloadEnvio, payloadRetorno, codigo);
	}
}
