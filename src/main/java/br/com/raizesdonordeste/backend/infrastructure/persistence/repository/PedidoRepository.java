package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.raizesdonordeste.backend.domain.enums.CanalPedido;
import br.com.raizesdonordeste.backend.domain.enums.StatusPedido;
import br.com.raizesdonordeste.backend.domain.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	@Override
	@EntityGraph(attributePaths = {"cliente", "unidade", "itens", "itens.produto"})
	Optional<Pedido> findById(Long id);

	@EntityGraph(attributePaths = {"cliente", "unidade", "itens", "itens.produto"})
	List<Pedido> findByClienteIdOrderByCreatedAtDesc(Long clienteId);

	@EntityGraph(attributePaths = {"cliente", "unidade", "itens", "itens.produto"})
	Optional<Pedido> findByIdAndClienteId(Long id, Long clienteId);

	@EntityGraph(attributePaths = {"cliente", "unidade", "itens", "itens.produto"})
	List<Pedido> findByUnidadeIdOrderByCreatedAtDesc(Long unidadeId);

	@Query("""
		SELECT p
		FROM Pedido p
		WHERE (:clienteId IS NULL OR p.cliente.id = :clienteId)
		  AND (:unidadeId IS NULL OR p.unidade.id = :unidadeId)
		  AND (:status IS NULL OR p.status = :status)
		  AND (:canalPedido IS NULL OR p.canalPedido = :canalPedido)
		  AND p.createdAt >= COALESCE(:dataInicio, p.createdAt)
		  AND p.createdAt <= COALESCE(:dataFim, p.createdAt)
	""")
	Page<Pedido> findByFiltros(
		@Param("clienteId") Long clienteId,
		@Param("unidadeId") Long unidadeId,
		@Param("status") StatusPedido status,
		@Param("canalPedido") CanalPedido canalPedido,
		@Param("dataInicio") Instant dataInicio,
		@Param("dataFim") Instant dataFim,
		Pageable pageable
	);
}