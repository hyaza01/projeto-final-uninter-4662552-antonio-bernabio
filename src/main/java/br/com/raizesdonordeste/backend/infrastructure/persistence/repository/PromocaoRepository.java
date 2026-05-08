package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.raizesdonordeste.backend.domain.model.Promocao;

public interface PromocaoRepository extends JpaRepository<Promocao, Long> {

	@Query("""
		SELECT p
		FROM Promocao p
		WHERE p.ativa = true
		  AND :dataReferencia BETWEEN p.dataInicio AND p.dataFim
		  AND (p.produto IS NULL OR p.produto.id = :produtoId)
		  AND (p.unidade IS NULL OR p.unidade.id = :unidadeId)
		ORDER BY p.percentualDesconto DESC
	""")
	List<Promocao> findPromocoesAplicaveis(
		@Param("produtoId") Long produtoId,
		@Param("unidadeId") Long unidadeId,
		@Param("dataReferencia") LocalDate dataReferencia
	);

	@Query("""
		SELECT p
		FROM Promocao p
		WHERE (:ativa IS NULL OR p.ativa = :ativa)
		  AND (:produtoId IS NULL OR (p.produto IS NOT NULL AND p.produto.id = :produtoId))
		  AND (:unidadeId IS NULL OR (p.unidade IS NOT NULL AND p.unidade.id = :unidadeId))
		  AND (:dataReferencia IS NULL OR :dataReferencia BETWEEN p.dataInicio AND p.dataFim)
	""")
	Page<Promocao> findByFiltros(
		@Param("ativa") Boolean ativa,
		@Param("produtoId") Long produtoId,
		@Param("unidadeId") Long unidadeId,
		@Param("dataReferencia") LocalDate dataReferencia,
		Pageable pageable
	);
}