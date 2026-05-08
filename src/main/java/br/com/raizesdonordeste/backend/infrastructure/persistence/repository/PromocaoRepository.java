package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;

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
}