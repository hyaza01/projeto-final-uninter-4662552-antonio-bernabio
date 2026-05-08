package br.com.raizesdonordeste.backend.application.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.raizesdonordeste.backend.api.dto.promocao.PromocaoCreateRequest;
import br.com.raizesdonordeste.backend.api.dto.promocao.PromocaoResponse;
import br.com.raizesdonordeste.backend.api.dto.promocao.PromocaoStatusRequest;
import br.com.raizesdonordeste.backend.domain.model.Produto;
import br.com.raizesdonordeste.backend.domain.model.Promocao;
import br.com.raizesdonordeste.backend.domain.model.Unidade;
import br.com.raizesdonordeste.backend.infrastructure.audit.AuditoriaService;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.ProdutoRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.PromocaoRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PromocaoService {

	private final PromocaoRepository promocaoRepository;
	private final ProdutoRepository produtoRepository;
	private final UnidadeRepository unidadeRepository;
	private final AuditoriaService auditoriaService;

	@Transactional
	public PromocaoResponse criarPromocao(PromocaoCreateRequest request, String emailAutenticado) {
		Produto produto = null;
		if (request.produtoId() != null) {
			produto = produtoRepository.findByIdAndAtivoTrue(request.produtoId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));
		}

		Unidade unidade = null;
		if (request.unidadeId() != null) {
			unidade = unidadeRepository.findByIdAndAtivaTrue(request.unidadeId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade nao encontrada"));
		}

		Promocao promocao = new Promocao();
		promocao.setNome(request.nome().trim());
		promocao.setDescricao(request.descricao().trim());
		promocao.setPercentualDesconto(request.percentualDesconto().setScale(2, RoundingMode.HALF_UP));
		promocao.setDataInicio(request.dataInicio());
		promocao.setDataFim(request.dataFim());
		promocao.setAtiva(Boolean.TRUE.equals(request.ativa()));
		promocao.setProduto(produto);
		promocao.setUnidade(unidade);

		Promocao salva = promocaoRepository.save(promocao);

		auditoriaService.registrar(
			emailAutenticado,
			"CRIACAO_PROMOCAO",
			"Promocao",
			salva.getId(),
			"nome=" + salva.getNome()
				+ "; percentualDesconto=" + salva.getPercentualDesconto()
				+ "; ativa=" + salva.isAtiva()
		);

		return toResponse(salva);
	}

	@Transactional(readOnly = true)
	public Page<PromocaoResponse> listarPromocoes(
		Boolean ativa,
		Long produtoId,
		Long unidadeId,
		String dataReferencia,
		Pageable pageable
	) {
		LocalDate data = parseDataReferencia(dataReferencia);

		return promocaoRepository.findByFiltros(ativa, produtoId, unidadeId, data, pageable)
			.map(this::toResponse);
	}

	@Transactional
	public PromocaoResponse atualizarStatus(Long promocaoId, PromocaoStatusRequest request, String emailAutenticado) {
		Promocao promocao = promocaoRepository.findById(promocaoId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promocao nao encontrada"));

		promocao.setAtiva(Boolean.TRUE.equals(request.ativa()));

		Promocao salva = promocaoRepository.save(promocao);

		auditoriaService.registrar(
			emailAutenticado,
			"ALTERACAO_STATUS_PROMOCAO",
			"Promocao",
			salva.getId(),
			"ativa=" + salva.isAtiva()
		);

		return toResponse(salva);
	}

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

	private LocalDate parseDataReferencia(String dataReferencia) {
		if (dataReferencia == null || dataReferencia.isBlank()) {
			return null;
		}

		try {
			return LocalDate.parse(dataReferencia);
		} catch (DateTimeParseException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataReferencia invalida");
		}
	}

	private PromocaoResponse toResponse(Promocao promocao) {
		return new PromocaoResponse(
			promocao.getId(),
			promocao.getNome(),
			promocao.getDescricao(),
			promocao.getPercentualDesconto(),
			promocao.getDataInicio(),
			promocao.getDataFim(),
			promocao.isAtiva(),
			promocao.getProduto() != null ? promocao.getProduto().getId() : null,
			promocao.getUnidade() != null ? promocao.getUnidade().getId() : null,
			promocao.getCreatedAt(),
			promocao.getUpdatedAt()
		);
	}
}
