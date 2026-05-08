package br.com.raizesdonordeste.backend.application.services;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.raizesdonordeste.backend.api.dto.auth.LoginRequest;
import br.com.raizesdonordeste.backend.api.dto.auth.LoginResponse;
import br.com.raizesdonordeste.backend.api.dto.auth.RegisterRequest;
import br.com.raizesdonordeste.backend.api.dto.auth.UserResponse;
import br.com.raizesdonordeste.backend.domain.enums.PerfilUsuario;
import br.com.raizesdonordeste.backend.domain.model.Cliente;
import br.com.raizesdonordeste.backend.domain.model.Usuario;
import br.com.raizesdonordeste.backend.infrastructure.audit.AuditoriaService;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.ClienteRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UsuarioRepository;
import br.com.raizesdonordeste.backend.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UsuarioRepository usuarioRepository;
	private final ClienteRepository clienteRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final AuditoriaService auditoriaService;

	@Transactional
	public UserResponse register(RegisterRequest request) {
		String emailNormalizado = normalizeEmail(request.email());

		if (usuarioRepository.existsByEmail(emailNormalizado)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ja cadastrado");
		}

		Usuario usuario = new Usuario();
		usuario.setNome(request.nome().trim());
		usuario.setEmail(emailNormalizado);
		usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
		usuario.setPerfil(PerfilUsuario.CLIENTE);
		usuario.setAtivo(true);

		Usuario salvo = usuarioRepository.save(usuario);
		Cliente cliente = new Cliente();
		cliente.setUsuario(salvo);
		cliente.setConsentimentoFidelidade(false);
		cliente.setPontosSaldo(0);
		clienteRepository.save(cliente);

		return toUserResponse(salvo);
	}

	@Transactional
	public LoginResponse login(LoginRequest request) {
		String emailNormalizado = normalizeEmail(request.email());

		try {
			authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(emailNormalizado, request.senha())
			);
		} catch (AuthenticationException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha invalidos");
		}

		Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario invalido"));

		if (!usuario.isAtivo()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inativo");
		}

		String token = jwtService.generateToken(usuario.getEmail(), usuario.getPerfil().name());

		auditoriaService.registrar(
			usuario.getEmail(),
			"LOGIN_REALIZADO",
			"Usuario",
			usuario.getId(),
			"perfil=" + usuario.getPerfil().name()
		);

		return new LoginResponse(token, "Bearer", toUserResponse(usuario));
	}

	@Transactional(readOnly = true)
	public UserResponse me(String email) {
		Usuario usuario = usuarioRepository.findByEmail(normalizeEmail(email))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

		return toUserResponse(usuario);
	}

	private String normalizeEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}

	private UserResponse toUserResponse(Usuario usuario) {
		return new UserResponse(
			usuario.getId(),
			usuario.getNome(),
			usuario.getEmail(),
			usuario.getPerfil(),
			usuario.isAtivo()
		);
	}
}
