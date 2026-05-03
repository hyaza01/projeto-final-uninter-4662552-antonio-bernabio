package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.raizesdonordeste.backend.domain.model.Estoque;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

	Optional<Estoque> findByUnidadeIdAndProdutoId(Long unidadeId, Long produtoId);
}