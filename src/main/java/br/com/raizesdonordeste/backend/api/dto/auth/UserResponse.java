package br.com.raizesdonordeste.backend.api.dto.auth;

import br.com.raizesdonordeste.backend.domain.enums.PerfilUsuario;

public record UserResponse(
	Long id,
	String nome,
	String email,
	PerfilUsuario perfil,
	boolean ativo
) {
}
