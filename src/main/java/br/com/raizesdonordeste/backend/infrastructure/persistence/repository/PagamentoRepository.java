package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.raizesdonordeste.backend.domain.model.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

	Optional<Pagamento> findByPedidoId(Long pedidoId);
}