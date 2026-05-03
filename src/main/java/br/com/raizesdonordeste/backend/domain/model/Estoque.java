package br.com.raizesdonordeste.backend.domain.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
	name = "estoques",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_estoque_unidade_produto", columnNames = {"unidade_id", "produto_id"})
	}
)
public class Estoque extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unidade_id", nullable = false)
	private Unidade unidade;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "produto_id", nullable = false)
	private Produto produto;

	@Column(name = "quantidade_atual", nullable = false)
	private Integer quantidadeAtual;

	@Column(name = "estoque_minimo", nullable = false)
	private Integer estoqueMinimo;

	@OneToMany(mappedBy = "estoque", fetch = FetchType.LAZY)
	private List<MovimentoEstoque> movimentacoes = new ArrayList<>();
}