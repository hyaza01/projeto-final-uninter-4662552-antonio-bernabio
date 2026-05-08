package br.com.raizesdonordeste.backend.api.dto.fidelidade;

public record FidelidadeSaldoResponse(
	Long clienteId,
	boolean consentimentoFidelidade,
	Integer pontosSaldo
) {
}
