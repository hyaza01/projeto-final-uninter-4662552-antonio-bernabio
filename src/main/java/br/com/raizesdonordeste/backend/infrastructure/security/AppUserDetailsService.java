package br.com.raizesdonordeste.backend.infrastructure.security;

import java.util.List;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.com.raizesdonordeste.backend.domain.model.Usuario;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

		if (!usuario.isAtivo()) {
			throw new DisabledException("Usuario inativo");
		}

		String role = "ROLE_" + usuario.getPerfil().name();

		return new User(
			usuario.getEmail(),
			usuario.getSenhaHash(),
			List.of(new SimpleGrantedAuthority(role))
		);
	}
}
