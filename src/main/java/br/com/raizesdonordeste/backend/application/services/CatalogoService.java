package br.com.raizesdonordeste.backend.application.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.raizesdonordeste.backend.api.dto.catalogo.ProdutoCatalogoResponse;
import br.com.raizesdonordeste.backend.domain.enums.CategoriaProduto;
import br.com.raizesdonordeste.backend.domain.model.Produto;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogoService {

	private final ProdutoRepository produtoRepository;

	@Transactional(readOnly = true)
	public List<ProdutoCatalogoResponse> listarProdutosAtivos(CategoriaProduto categoria) {
		List<Produto> produtos = categoria == null
			? produtoRepository.findByAtivoTrueOrderByNomeAsc()
			: produtoRepository.findByAtivoTrueAndCategoriaOrderByNomeAsc(categoria);

		return produtos.stream()
			.map(this::toResponse)
			.toList();
	}

	@Transactional(readOnly = true)
	public ProdutoCatalogoResponse buscarProdutoAtivoPorId(Long id) {
		Produto produto = produtoRepository.findByIdAndAtivoTrue(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));

		return toResponse(produto);
	}

	private ProdutoCatalogoResponse toResponse(Produto produto) {
		return new ProdutoCatalogoResponse(
			produto.getId(),
			produto.getNome(),
			produto.getDescricao(),
			produto.getPreco(),
			produto.getCategoria()
		);
	}
}
