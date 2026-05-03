package br.com.raizesdonordeste.backend.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.raizesdonordeste.backend.domain.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}