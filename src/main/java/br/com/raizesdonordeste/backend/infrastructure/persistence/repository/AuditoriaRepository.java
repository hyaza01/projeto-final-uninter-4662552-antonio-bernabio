package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.raizesdonordeste.backend.domain.model.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

	@Query("""
		SELECT a
		FROM Auditoria a
		WHERE (:entidade IS NULL OR a.entidade = :entidade)
		  AND (:entidadeId IS NULL OR a.entidadeId = :entidadeId)
		  AND (:acao IS NULL OR a.acao = :acao)
		  AND (:usuarioId IS NULL OR (a.usuario IS NOT NULL AND a.usuario.id = :usuarioId))
	""")
	Page<Auditoria> findByFiltros(
		@Param("entidade") String entidade,
		@Param("entidadeId") Long entidadeId,
		@Param("acao") String acao,
		@Param("usuarioId") Long usuarioId,
		Pageable pageable
	);
}