package br.com.raizesdonordeste.backend.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.raizesdonordeste.backend.domain.enums.CanalPedido;
import br.com.raizesdonordeste.backend.domain.enums.FormaPagamento;
import br.com.raizesdonordeste.backend.domain.enums.StatusPedido;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pedidos")
public class Pedido extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false)
	private Cliente cliente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unidade_id", nullable = false)
	private Unidade unidade;

	@Enumerated(EnumType.STRING)
	@Column(name = "canal_pedido", nullable = false, length = 30)
	private CanalPedido canalPedido;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 40)
	private StatusPedido status;

	@Enumerated(EnumType.STRING)
	@Column(name = "forma_pagamento", nullable = false, length = 40)
	private FormaPagamento formaPagamento;

	@Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal;

	@Column(name = "total", nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PedidoItem> itens = new ArrayList<>();

	@OneToOne(mappedBy = "pedido", fetch = FetchType.LAZY)
	private Pagamento pagamento;

	@OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY)
	private List<FidelidadeHistorico> fidelidadeHistoricos = new ArrayList<>();

	@OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY)
	private List<MovimentoEstoque> movimentosEstoque = new ArrayList<>();
}