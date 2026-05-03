package br.com.raizesdonordeste.backend.domain.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "unidades")
public class Unidade extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false, length = 120)
	private String nome;

	@Column(name = "cidade", nullable = false, length = 80)
	private String cidade;

	@Column(name = "bairro", nullable = false, length = 80)
	private String bairro;

	@Column(name = "endereco", nullable = false, length = 255)
	private String endereco;

	@Column(name = "ativa", nullable = false)
	private boolean ativa = true;

	@OneToMany(mappedBy = "unidade", fetch = FetchType.LAZY)
	private List<Estoque> estoques = new ArrayList<>();

	@OneToMany(mappedBy = "unidade", fetch = FetchType.LAZY)
	private List<Pedido> pedidos = new ArrayList<>();

	@OneToMany(mappedBy = "unidade", fetch = FetchType.LAZY)
	private List<Promocao> promocoes = new ArrayList<>();
}