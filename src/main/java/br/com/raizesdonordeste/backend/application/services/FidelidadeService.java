package br.com.raizesdonordeste.backend.application.services;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.raizesdonordeste.backend.api.dto.fidelidade.FidelidadeHistoricoResponse;
import br.com.raizesdonordeste.backend.api.dto.fidelidade.FidelidadeSaldoResponse;
import br.com.raizesdonordeste.backend.domain.enums.TipoMovimentacaoFidelidade;
import br.com.raizesdonordeste.backend.domain.model.Cliente;
import br.com.raizesdonordeste.backend.domain.model.FidelidadeHistorico;
import br.com.raizesdonordeste.backend.domain.model.Pedido;
import br.com.raizesdonordeste.backend.domain.model.Usuario;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.ClienteRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.FidelidadeHistoricoRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FidelidadeService {

	private final UsuarioRepository usuarioRepository;
	private final ClienteRepository clienteRepository;
	private final FidelidadeHistoricoRepository fidelidadeHistoricoRepository;

	@Transactional
	public void gerarPontosSeElegivel(Pedido pedido) {
		Cliente cliente = pedido.getCliente();
		if (cliente == null || !cliente.isConsentimentoFidelidade()) {
			return;
		}

		int pontos = pedido.getTotal().intValue();
		if (pontos <= 0) {
			return;
		}

		cliente.setPontosSaldo((cliente.getPontosSaldo() == null ? 0 : cliente.getPontosSaldo()) + pontos);

		FidelidadeHistorico historico = new FidelidadeHistorico();
		historico.setCliente(cliente);
		historico.setPedido(pedido);
		historico.setTipo(TipoMovimentacaoFidelidade.CREDITO);
		historico.setPontos(pontos);
		historico.setDescricao("Pontos gerados por pagamento aprovado do pedido " + pedido.getId());
		fidelidadeHistoricoRepository.save(historico);
	}

	@Transactional(readOnly = true)
	public FidelidadeSaldoResponse consultarSaldo(String emailAutenticado) {
		Cliente cliente = findClienteByEmail(emailAutenticado);
		return new FidelidadeSaldoResponse(
			cliente.getId(),
			cliente.isConsentimentoFidelidade(),
			cliente.getPontosSaldo()
		);
	}

	@Transactional(readOnly = true)
	public List<FidelidadeHistoricoResponse> listarHistorico(String emailAutenticado) {
		Cliente cliente = findClienteByEmail(emailAutenticado);

		return fidelidadeHistoricoRepository.findByClienteIdOrderByCreatedAtDesc(cliente.getId()).stream()
			.map(h -> new FidelidadeHistoricoResponse(
				h.getId(),
				h.getPedido() != null ? h.getPedido().getId() : null,
				h.getTipo(),
				h.getPontos(),
				h.getDescricao(),
				h.getCreatedAt()
			))
			.toList();
	}

	@Transactional
	public FidelidadeSaldoResponse atualizarConsentimento(String emailAutenticado, boolean consentimentoFidelidade) {
		Cliente cliente = findClienteByEmail(emailAutenticado);
		cliente.setConsentimentoFidelidade(consentimentoFidelidade);

		return new FidelidadeSaldoResponse(
			cliente.getId(),
			cliente.isConsentimentoFidelidade(),
			cliente.getPontosSaldo()
		);
	}

	private Cliente findClienteByEmail(String email) {
		Usuario usuario = usuarioRepository.findByEmail(normalizeEmail(email))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado"));

		return clienteRepository.findByUsuarioId(usuario.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
	}

	private String normalizeEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}
}
