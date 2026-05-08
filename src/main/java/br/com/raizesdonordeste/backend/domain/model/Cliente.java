package br.com.raizesdonordeste.backend.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "clientes")
public class Cliente extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false, unique = true)
	private Usuario usuario;

	@Column(name = "consentimento_fidelidade", nullable = false)
	private boolean consentimentoFidelidade;

	@Column(name = "pontos_saldo", nullable = false)
	private Integer pontosSaldo = 0;

	@Column(name = "consentimento_fidelidade_atualizado_em")
	private Instant consentimentoFidelidadeAtualizadoEm;

	@OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
	private List<Pedido> pedidos = new ArrayList<>();

	@OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
	private List<FidelidadeHistorico> fidelidadeHistoricos = new ArrayList<>();
}