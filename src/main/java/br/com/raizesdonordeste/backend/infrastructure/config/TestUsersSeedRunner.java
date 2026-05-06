package br.com.raizesdonordeste.backend.infrastructure.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.raizesdonordeste.backend.domain.enums.PerfilUsuario;
import br.com.raizesdonordeste.backend.domain.model.Cliente;
import br.com.raizesdonordeste.backend.domain.model.Usuario;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.ClienteRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(UsuarioRepository.class)
@ConditionalOnProperty(name = "app.seed.test-users.enabled", havingValue = "true", matchIfMissing = true)
public class TestUsersSeedRunner implements ApplicationRunner {

	private final UsuarioRepository usuarioRepository;
	private final ClienteRepository clienteRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		upsertUser("Administrador", "admin@raizes.local", "Admin@123", PerfilUsuario.ADMIN, false);
		upsertUser("Gerente", "gerente@raizes.local", "Gerente@123", PerfilUsuario.GERENTE, false);
		upsertUser("Atendente", "atendente@raizes.local", "Atendente@123", PerfilUsuario.ATENDENTE, false);
		upsertUser("Cozinha", "cozinha@raizes.local", "Cozinha@123", PerfilUsuario.COZINHA, false);
		upsertUser("Cliente", "cliente@raizes.local", "Cliente@123", PerfilUsuario.CLIENTE, true);
	}

	private void upsertUser(String nome, String email, String senha, PerfilUsuario perfil, boolean criarCliente) {
		String emailNormalizado = email.trim().toLowerCase();
		Usuario usuario = usuarioRepository.findByEmail(emailNormalizado).orElseGet(Usuario::new);
		usuario.setNome(nome);
		usuario.setEmail(emailNormalizado);
		usuario.setPerfil(perfil);
		usuario.setAtivo(true);

		if (usuario.getSenhaHash() == null || !passwordEncoder.matches(senha, usuario.getSenhaHash())) {
			usuario.setSenhaHash(passwordEncoder.encode(senha));
		}

		Usuario salvo = usuarioRepository.save(usuario);

		if (criarCliente) {
			clienteRepository.findByUsuarioId(salvo.getId()).orElseGet(() -> {
				Cliente cliente = new Cliente();
				cliente.setUsuario(salvo);
				cliente.setConsentimentoFidelidade(true);
				cliente.setPontosSaldo(0);
				return clienteRepository.save(cliente);
			});
		}
	}
}
