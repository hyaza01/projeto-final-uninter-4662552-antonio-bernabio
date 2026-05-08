package br.com.raizesdonordeste.backend.infrastructure.audit;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import br.com.raizesdonordeste.backend.api.dto.auditoria.AuditoriaResponse;
import br.com.raizesdonordeste.backend.domain.model.Auditoria;
import br.com.raizesdonordeste.backend.domain.model.Usuario;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.AuditoriaRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
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
		auditoria.setIpOrigem(resolveIpOrigem());

		if (emailUsuario != null && !emailUsuario.isBlank()) {
			String emailNormalizado = emailUsuario.trim().toLowerCase(Locale.ROOT);
			Usuario usuario = usuarioRepository.findByEmail(emailNormalizado).orElse(null);
			auditoria.setUsuario(usuario);
		}

		auditoriaRepository.save(auditoria);
	}

	@Transactional(readOnly = true)
	public Page<AuditoriaResponse> listar(
		String entidade,
		Long entidadeId,
		String acao,
		Long usuarioId,
		Pageable pageable
	) {
		return auditoriaRepository.findByFiltros(
			normalizeText(entidade),
			entidadeId,
			normalizeText(acao),
			usuarioId,
			pageable
		).map(this::toResponse);
	}

	private AuditoriaResponse toResponse(Auditoria auditoria) {
		Usuario usuario = auditoria.getUsuario();

		return new AuditoriaResponse(
			auditoria.getId(),
			usuario != null ? usuario.getId() : null,
			usuario != null ? usuario.getEmail() : null,
			auditoria.getAcao(),
			auditoria.getEntidade(),
			auditoria.getEntidadeId(),
			auditoria.getDetalhes(),
			auditoria.getIpOrigem(),
			auditoria.getCreatedAt()
		);
	}

	private String resolveIpOrigem() {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		if (attrs == null) {
			return null;
		}

		HttpServletRequest request = attrs.getRequest();
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			int commaIndex = forwardedFor.indexOf(',');
			return (commaIndex > 0 ? forwardedFor.substring(0, commaIndex) : forwardedFor).trim();
		}

		return request.getRemoteAddr();
	}

	private String normalizeText(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}
}
