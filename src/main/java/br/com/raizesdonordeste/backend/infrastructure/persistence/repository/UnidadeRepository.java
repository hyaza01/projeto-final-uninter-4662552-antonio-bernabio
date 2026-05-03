package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.raizesdonordeste.backend.domain.model.Unidade;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
}