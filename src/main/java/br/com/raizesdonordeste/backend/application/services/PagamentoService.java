package br.com.raizesdonordeste.backend.application.services;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.raizesdonordeste.backend.api.dto.pagamento.PagamentoMockRequest;
import br.com.raizesdonordeste.backend.api.dto.pagamento.PagamentoRequest;
import br.com.raizesdonordeste.backend.api.dto.pagamento.PagamentoResponse;
import br.com.raizesdonordeste.backend.domain.enums.FormaPagamento;
import br.com.raizesdonordeste.backend.domain.enums.PerfilUsuario;
import br.com.raizesdonordeste.backend.domain.enums.StatusPagamento;
import br.com.raizesdonordeste.backend.domain.enums.StatusPedido;
import br.com.raizesdonordeste.backend.domain.enums.TipoMovimentoEstoque;
import br.com.raizesdonordeste.backend.domain.model.Cliente;
import br.com.raizesdonordeste.backend.domain.model.Estoque;
import br.com.raizesdonordeste.backend.domain.model.MovimentoEstoque;
import br.com.raizesdonordeste.backend.domain.model.Pagamento;
import br.com.raizesdonordeste.backend.domain.model.Pedido;
import br.com.raizesdonordeste.backend.domain.model.PedidoItem;
import br.com.raizesdonordeste.backend.domain.model.Usuario;
import br.com.raizesdonordeste.backend.infrastructure.payment.PaymentMockResult;
import br.com.raizesdonordeste.backend.infrastructure.payment.PaymentMockService;
import br.com.raizesdonordeste.backend.infrastructure.audit.AuditoriaService;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.ClienteRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.EstoqueRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.MovimentoEstoqueRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.PagamentoRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.PedidoRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PagamentoService {

	private final PedidoRepository pedidoRepository;
	private final PagamentoRepository pagamentoRepository;
	private final UsuarioRepository usuarioRepository;
	private final ClienteRepository clienteRepository;
	private final EstoqueRepository estoqueRepository;
	private final MovimentoEstoqueRepository movimentoEstoqueRepository;
	private final PaymentMockService paymentMockService;
	private final AuditoriaService auditoriaService;
	private final FidelidadeService fidelidadeService;

	private static final String MSG_PAGAMENTO_APROVADO = "Pagamento mock aprovado com sucesso.";
	private static final String MSG_PAGAMENTO_RECUSADO = "Pagamento mock recusado.";

	@Transactional
	public PagamentoResponse processarPagamento(Long pedidoId, PagamentoRequest request, String emailAutenticado) {
		return processarPagamentoInterno(
			pedidoId,
			emailAutenticado,
			request.formaPagamento(),
			request.forcarAprovacao()
		);
	}

	@Transactional
	public PagamentoResponse processarPagamentoMock(Long pedidoId, PagamentoMockRequest request, String emailAutenticado) {
		Boolean forcarAprovacao = request.resultadoMock() == StatusPagamento.APROVADO;
		return processarPagamentoInterno(pedidoId, emailAutenticado, null, forcarAprovacao);
	}

	@Transactional(readOnly = true)
	public PagamentoResponse buscarPagamento(Long pagamentoId, String emailAutenticado) {
		Usuario usuario = findUsuarioByEmail(emailAutenticado);

		Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento nao encontrado"));

		validarPermissaoConsulta(pagamento, usuario);
		String mensagem = pagamento.getStatus() == StatusPagamento.APROVADO ? MSG_PAGAMENTO_APROVADO : MSG_PAGAMENTO_RECUSADO;
		return toResponse(pagamento, mensagem);
	}

	private PagamentoResponse processarPagamentoInterno(
		Long pedidoId,
		String emailAutenticado,
		FormaPagamento formaPagamento,
		Boolean forcarAprovacao
	) {
		Cliente cliente = findClienteByEmail(emailAutenticado);

		Pedido pedido = pedidoRepository.findByIdAndClienteId(pedidoId, cliente.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

		validarConflitosDePagamento(pedido);

		if (formaPagamento != null) {
			pedido.setFormaPagamento(formaPagamento);
		}

		PaymentMockResult mockResult = paymentMockService.processar(pedido, forcarAprovacao);

		Pagamento pagamento = new Pagamento();
		pagamento.setPedido(pedido);
		pagamento.setStatus(mockResult.status());
		pagamento.setMetodo(pedido.getFormaPagamento());
		pagamento.setValor(pedido.getTotal());
		pagamento.setPayloadEnvio(mockResult.payloadEnvio());
		pagamento.setPayloadRetorno(mockResult.payloadRetorno());
		pagamento.setCodigoTransacaoMock(mockResult.codigoTransacao());
		String mensagem;

		if (mockResult.status() == StatusPagamento.APROVADO) {
			baixarEstoquePorPagamento(pedido);
			pedido.setStatus(StatusPedido.PAGAMENTO_APROVADO);
			fidelidadeService.gerarPontosSeElegivel(pedido);
			mensagem = MSG_PAGAMENTO_APROVADO;
		} else {
			pedido.setStatus(StatusPedido.PAGAMENTO_RECUSADO);
			mensagem = MSG_PAGAMENTO_RECUSADO;
		}

		Pagamento salvo = pagamentoRepository.save(pagamento);
		pedidoRepository.save(pedido);

		auditoriaService.registrar(
			emailAutenticado,
			"PAGAMENTO_PROCESSADO",
			"Pedido",
			pedido.getId(),
			"statusPagamento=" + mockResult.status().name() + "; metodo=MOCK; codigoTransacao=" + mockResult.codigoTransacao()
		);

		return toResponse(salvo, mensagem);
	}

	private void validarConflitosDePagamento(Pedido pedido) {
		if (pedido.getStatus() == StatusPedido.CANCELADO) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Nao e permitido pagar pedido cancelado");
		}

		if (pedido.getStatus() == StatusPedido.ENTREGUE) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Nao e permitido pagar pedido entregue");
		}

		if (pedido.getStatus() == StatusPedido.PAGAMENTO_APROVADO) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Pedido ja possui pagamento aprovado");
		}

		if (pagamentoRepository.findByPedidoId(pedido.getId()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Pedido ja possui pagamento processado");
		}

		if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Pedido nao esta aguardando pagamento");
		}
	}

	private void validarPermissaoConsulta(Pagamento pagamento, Usuario usuario) {
		if (usuario.getPerfil() != PerfilUsuario.CLIENTE) {
			return;
		}

		Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas clientes podem consultar seus pagamentos"));

		if (!pagamento.getPedido().getCliente().getId().equals(cliente.getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pagamento nao encontrado");
		}
	}

	private Cliente findClienteByEmail(String email) {
		Usuario usuario = findUsuarioByEmail(email);

		return clienteRepository.findByUsuarioId(usuario.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas clientes podem processar pagamentos"));
	}

	private Usuario findUsuarioByEmail(String email) {
		return usuarioRepository.findByEmail(normalizeEmail(email))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado"));
	}

	private String normalizeEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}

	private PagamentoResponse toResponse(Pagamento pagamento, String mensagem) {
		return new PagamentoResponse(
			pagamento.getId(),
			pagamento.getPedido().getId(),
			pagamento.getStatus(),
			pagamento.getPedido().getStatus(),
			pagamento.getMetodo(),
			pagamento.getValor(),
			pagamento.getCodigoTransacaoMock(),
			pagamento.getCreatedAt(),
			mensagem
		);
	}

	private void baixarEstoquePorPagamento(Pedido pedido) {
		for (PedidoItem item : pedido.getItens()) {
			Estoque estoque = estoqueRepository.findByUnidadeIdAndProdutoId(
				pedido.getUnidade().getId(),
				item.getProduto().getId()
			).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Estoque nao encontrado para o produto"));

			if (estoque.getQuantidadeAtual() < item.getQuantidade()) {
				throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"Estoque insuficiente para concluir pagamento do produto " + item.getProduto().getNome()
				);
			}

			estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() - item.getQuantidade());

			MovimentoEstoque movimento = new MovimentoEstoque();
			movimento.setEstoque(estoque);
			movimento.setPedido(pedido);
			movimento.setTipo(TipoMovimentoEstoque.SAIDA_PEDIDO);
			movimento.setQuantidade(item.getQuantidade());
			movimento.setMotivo("Baixa apos pagamento aprovado");
			movimentoEstoqueRepository.save(movimento);
		}
	}
}
