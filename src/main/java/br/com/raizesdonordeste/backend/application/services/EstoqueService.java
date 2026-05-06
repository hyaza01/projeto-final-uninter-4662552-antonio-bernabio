package br.com.raizesdonordeste.backend.application.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.raizesdonordeste.backend.api.dto.estoque.EstoqueConsultaResponse;
import br.com.raizesdonordeste.backend.domain.model.Estoque;
import br.com.raizesdonordeste.backend.domain.model.Produto;
import br.com.raizesdonordeste.backend.domain.model.Unidade;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.EstoqueRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.ProdutoRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UnidadeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstoqueService {

	private final EstoqueRepository estoqueRepository;
	private final UnidadeRepository unidadeRepository;
	private final ProdutoRepository produtoRepository;

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
}
