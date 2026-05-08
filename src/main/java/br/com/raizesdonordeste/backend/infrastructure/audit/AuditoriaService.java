package br.com.raizesdonordeste.backend.infrastructure.audit;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.raizesdonordeste.backend.domain.model.Auditoria;
import br.com.raizesdonordeste.backend.domain.model.Usuario;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.AuditoriaRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

	private final AuditoriaRepository auditoriaRepository;
	private final UsuarioRepository usuarioRepository;

	@Transactional
	public void registrar(String emailUsuario, String acao, String entidade, Long entidadeId, String detalhes) {
		Auditoria auditoria = new Auditoria();
		auditoria.setAcao(acao);
		auditoria.setEntidade(entidade);
		auditoria.setEntidadeId(entidadeId == null ? 0L : entidadeId);
		auditoria.setDetalhes(detalhes == null ? "-" : detalhes);

		if (emailUsuario != null && !emailUsuario.isBlank()) {
			String emailNormalizado = emailUsuario.trim().toLowerCase(Locale.ROOT);
			Usuario usuario = usuarioRepository.findByEmail(emailNormalizado).orElse(null);
			auditoria.setUsuario(usuario);
		}

		auditoriaRepository.save(auditoria);
	}
}
