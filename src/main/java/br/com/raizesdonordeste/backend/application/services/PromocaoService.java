package br.com.raizesdonordeste.backend.application.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.raizesdonordeste.backend.domain.model.Promocao;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.PromocaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromocaoService {

	private final PromocaoRepository promocaoRepository;

	@Transactional(readOnly = true)
	public BigDecimal aplicarPromocaoSeExistir(Long produtoId, Long unidadeId, BigDecimal precoBase) {
		List<Promocao> promocoes = promocaoRepository.findPromocoesAplicaveis(produtoId, unidadeId, LocalDate.now());
		if (promocoes.isEmpty()) {
			return precoBase;
		}

		BigDecimal percentual = promocoes.get(0).getPercentualDesconto();
		if (percentual == null || percentual.compareTo(BigDecimal.ZERO) <= 0) {
			return precoBase;
		}

		BigDecimal fator = BigDecimal.ONE.subtract(percentual.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
		BigDecimal precoFinal = precoBase.multiply(fator).setScale(2, RoundingMode.HALF_UP);
		return precoFinal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : precoFinal;
	}
}
