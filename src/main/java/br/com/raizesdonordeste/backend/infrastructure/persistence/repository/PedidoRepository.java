package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.raizesdonordeste.backend.domain.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	@EntityGraph(attributePaths = {"cliente", "unidade", "itens", "itens.produto"})
	List<Pedido> findByClienteIdOrderByCreatedAtDesc(Long clienteId);

	@EntityGraph(attributePaths = {"cliente", "unidade", "itens", "itens.produto"})
	Optional<Pedido> findByIdAndClienteId(Long id, Long clienteId);
}