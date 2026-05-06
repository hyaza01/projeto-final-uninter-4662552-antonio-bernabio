package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.raizesdonordeste.backend.domain.enums.CategoriaProduto;
import br.com.raizesdonordeste.backend.domain.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

	List<Produto> findByAtivoTrueOrderByNomeAsc();

	List<Produto> findByAtivoTrueAndCategoriaOrderByNomeAsc(CategoriaProduto categoria);

	Optional<Produto> findByIdAndAtivoTrue(Long id);
}