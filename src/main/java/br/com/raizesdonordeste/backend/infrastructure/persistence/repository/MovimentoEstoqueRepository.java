package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.raizesdonordeste.backend.domain.model.MovimentoEstoque;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {

	Page<MovimentoEstoque> findByEstoqueIdOrderByCreatedAtDesc(Long estoqueId, Pageable pageable);
}