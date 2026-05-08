package br.com.raizesdonordeste.backend.application.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.raizesdonordeste.backend.api.dto.estoque.EstoqueConsultaResponse;
import br.com.raizesdonordeste.backend.api.dto.estoque.MovimentacaoEstoqueRequest;
import br.com.raizesdonordeste.backend.api.dto.estoque.MovimentacaoEstoqueResponse;
import br.com.raizesdonordeste.backend.domain.model.MovimentoEstoque;
import br.com.raizesdonordeste.backend.domain.model.Estoque;
import br.com.raizesdonordeste.backend.domain.model.Produto;
import br.com.raizesdonordeste.backend.domain.model.Unidade;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.EstoqueRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.MovimentoEstoqueRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.ProdutoRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UnidadeRepository;
import br.com.raizesdonordeste.backend.infrastructure.audit.AuditoriaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstoqueService {

	private final EstoqueRepository estoqueRepository;
	private final UnidadeRepository unidadeRepository;
	private final ProdutoRepository produtoRepository;
	private final MovimentoEstoqueRepository movimentoEstoqueRepository;
	private final AuditoriaService auditoriaService;

	@Transactional(readOnly = true)
	public List<EstoqueConsultaResponse> listarPorUnidade(Long unidadeId) {
		unidadeRepository.findByIdAndAtivaTrue(unidadeId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade nao encontrada"));

		return estoqueRepository.findByUnidadeIdOrderByProdutoNomeAsc(unidadeId).stream()
			.filter(estoque -> estoque.getProduto().isAtivo())
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public EstoqueConsultaResponse buscarPorUnidadeEProduto(Long unidadeId, Long produtoId) {
		Unidade unidade = unidadeRepository.findByIdAndAtivaTrue(unidadeId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade nao encontrada"));

		Produto produto = produtoRepository.findByIdAndAtivoTrue(produtoId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));

		Estoque estoque = estoqueRepository.findByUnidadeIdAndProdutoId(unidade.getId(), produto.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque nao encontrado"));

		return toResponse(estoque);
	}

	@Transactional
	public MovimentacaoEstoqueResponse registrarMovimentacaoManual(
		MovimentacaoEstoqueRequest request,
		String emailAutenticado
	) {
		Unidade unidade = unidadeRepository.findByIdAndAtivaTrue(request.unidadeId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade nao encontrada"));

		Produto produto = produtoRepository.findByIdAndAtivoTrue(request.produtoId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));

		Estoque estoque = estoqueRepository.findByUnidadeIdAndProdutoId(unidade.getId(), produto.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque nao encontrado"));

		int quantidadeAnterior = estoque.getQuantidadeAtual();
		int quantidadeAtual;

		switch (request.tipo()) {
			case ENTRADA -> quantidadeAtual = quantidadeAnterior + request.quantidade();
			case SAIDA -> {
				if (request.quantidade() > quantidadeAnterior) {
					throw new ResponseStatusException(HttpStatus.CONFLICT, "Saida maior que saldo atual em estoque");
				}
				quantidadeAtual = quantidadeAnterior - request.quantidade();
			}
			default -> throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Tipo de movimentacao invalido");
		}

		estoque.setQuantidadeAtual(quantidadeAtual);

		MovimentoEstoque movimento = new MovimentoEstoque();
		movimento.setEstoque(estoque);
		movimento.setTipo(request.tipo());
		movimento.setQuantidade(request.quantidade());
		movimento.setMotivo(request.motivo().trim());

		MovimentoEstoque salvo = movimentoEstoqueRepository.save(movimento);

		auditoriaService.registrar(
			emailAutenticado,
			"MOVIMENTACAO_ESTOQUE",
			"Estoque",
			estoque.getId(),
			"tipo=" + request.tipo().name()
				+ "; quantidade=" + request.quantidade()
				+ "; saldoAnterior=" + quantidadeAnterior
				+ "; saldoAtual=" + quantidadeAtual
				+ "; motivo=" + request.motivo().trim()
		);

		return toMovimentacaoResponse(salvo, quantidadeAnterior, quantidadeAtual);
	}

	@Transactional(readOnly = true)
	public Page<MovimentacaoEstoqueResponse> listarMovimentacoesPorEstoque(Long estoqueId, Pageable pageable) {
		estoqueRepository.findById(estoqueId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque nao encontrado"));

		return movimentoEstoqueRepository.findByEstoqueIdOrderByCreatedAtDesc(estoqueId, pageable)
			.map(movimento -> toMovimentacaoResponse(movimento, null, null));
	}

	private EstoqueConsultaResponse toResponse(Estoque estoque) {
		boolean abaixoDoMinimo = estoque.getQuantidadeAtual() <= estoque.getEstoqueMinimo();

		return new EstoqueConsultaResponse(
			estoque.getId(),
			estoque.getUnidade().getId(),
			estoque.getUnidade().getNome(),
			estoque.getProduto().getId(),
			estoque.getProduto().getNome(),
			estoque.getQuantidadeAtual(),
			estoque.getEstoqueMinimo(),
			abaixoDoMinimo
		);
	}

	private MovimentacaoEstoqueResponse toMovimentacaoResponse(
		MovimentoEstoque movimento,
		Integer quantidadeAnterior,
		Integer quantidadeAtual
	) {
		return new MovimentacaoEstoqueResponse(
			movimento.getId(),
			movimento.getEstoque().getId(),
			movimento.getEstoque().getUnidade().getId(),
			movimento.getEstoque().getProduto().getId(),
			movimento.getTipo(),
			movimento.getQuantidade(),
			quantidadeAnterior,
			quantidadeAtual,
			movimento.getMotivo(),
			movimento.getCreatedAt()
		);
	}
}
