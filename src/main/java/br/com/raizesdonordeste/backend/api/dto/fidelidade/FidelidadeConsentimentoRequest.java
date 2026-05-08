package br.com.raizesdonordeste.backend.api.dto.fidelidade;

import jakarta.validation.constraints.NotNull;

public record FidelidadeConsentimentoRequest(
	@NotNull(message = "Consentimento de fidelidade e obrigatorio")
	Boolean consentimentoFidelidade
) {
}
