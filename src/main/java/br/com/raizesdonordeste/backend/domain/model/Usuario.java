package br.com.raizesdonordeste.backend.domain.model;

import br.com.raizesdonordeste.backend.domain.enums.PerfilUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario extends AuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome", nullable = false, length = 120)
	private String nome;

	@Column(name = "email", nullable = false, unique = true, length = 180)
	private String email;

	@Column(name = "senha_hash", nullable = false, length = 255)
	private String senhaHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "perfil", nullable = false, length = 30)
	private PerfilUsuario perfil;

	@Column(name = "ativo", nullable = false)
	private boolean ativo = true;

	@OneToOne(mappedBy = "usuario", fetch = FetchType.LAZY)
	private Cliente cliente;
}