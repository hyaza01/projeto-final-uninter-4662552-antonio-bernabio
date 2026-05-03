package br.com.raizesdonordeste.backend.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.raizesdonordeste.backend.domain.enums.CategoriaProduto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "produtos")
public class Produto extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false, length = 120)
	private String nome;

	@Column(name = "descricao", nullable = false, length = 600)
	private String descricao;

	@Column(name = "preco", nullable = false, precision = 12, scale = 2)
	private BigDecimal preco;

	@Enumerated(EnumType.STRING)
	@Column(name = "categoria", nullable = false, length = 40)
	private CategoriaProduto categoria;

	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;

	@OneToMany(mappedBy = "produto", fetch = FetchType.LAZY)
	private List<Estoque> estoques = new ArrayList<>();

	@OneToMany(mappedBy = "produto", fetch = FetchType.LAZY)
	private List<PedidoItem> itens = new ArrayList<>();

	@OneToMany(mappedBy = "produto", fetch = FetchType.LAZY)
	private List<Promocao> promocoes = new ArrayList<>();
}