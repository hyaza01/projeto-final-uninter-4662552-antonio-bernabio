package br.com.raizesdonordeste.backend.application.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.raizesdonordeste.backend.api.dto.pedido.PedidoCreateRequest;
import br.com.raizesdonordeste.backend.api.dto.pedido.PedidoItemCreateRequest;
import br.com.raizesdonordeste.backend.api.dto.pedido.PedidoItemResponse;
import br.com.raizesdonordeste.backend.api.dto.pedido.PedidoResponse;
import br.com.raizesdonordeste.backend.domain.enums.CanalPedido;
import br.com.raizesdonordeste.backend.domain.enums.PerfilUsuario;
import br.com.raizesdonordeste.backend.domain.enums.StatusPedido;
import br.com.raizesdonordeste.backend.domain.enums.TipoMovimentoEstoque;
import br.com.raizesdonordeste.backend.domain.model.Cliente;
import br.com.raizesdonordeste.backend.domain.model.Estoque;
import br.com.raizesdonordeste.backend.domain.model.MovimentoEstoque;
import br.com.raizesdonordeste.backend.domain.model.Pedido;
import br.com.raizesdonordeste.backend.domain.model.PedidoItem;
import br.com.raizesdonordeste.backend.domain.model.Produto;
import br.com.raizesdonordeste.backend.domain.model.Unidade;
import br.com.raizesdonordeste.backend.domain.model.Usuario;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.ClienteRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.EstoqueRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.MovimentoEstoqueRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.PedidoRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.ProdutoRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UnidadeRepository;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UsuarioRepository;
import br.com.raizesdonordeste.backend.infrastructure.audit.AuditoriaService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoService {

	private final PedidoRepository pedidoRepository;
	private final ProdutoRepository produtoRepository;
	private final UnidadeRepository unidadeRepository;
	private final ClienteRepository clienteRepository;
	private final UsuarioRepository usuarioRepository;
	private final EstoqueRepository estoqueRepository;
	private final MovimentoEstoqueRepository movimentoEstoqueRepository;
	private final PromocaoService promocaoService;
	private final AuditoriaService auditoriaService;

	@Transactional
	public PedidoResponse criarPedido(PedidoCreateRequest request, String emailAutenticado) {
		Cliente cliente = findClienteByEmail(emailAutenticado);

		Unidade unidade = unidadeRepository.findByIdAndAtivaTrue(request.unidadeId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade nao encontrada"));

		Pedido pedido = new Pedido();
		pedido.setCliente(cliente);
		pedido.setUnidade(unidade);
		pedido.setCanalPedido(request.canalPedido());
		pedido.setFormaPagamento(request.formaPagamento());
		pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

		BigDecimal subtotal = BigDecimal.ZERO;

		for (PedidoItemCreateRequest itemRequest : request.itens()) {
			Produto produto = produtoRepository.findByIdAndAtivoTrue(itemRequest.produtoId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));

			Estoque estoque = estoqueRepository.findByUnidadeIdAndProdutoId(unidade.getId(), produto.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Produto indisponivel na unidade"));

			if (estoque.getQuantidadeAtual() < itemRequest.quantidade()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Estoque insuficiente para o produto " + produto.getNome());
			}

			BigDecimal precoUnitarioAplicado = promocaoService.aplicarPromocaoSeExistir(
				produto.getId(),
				unidade.getId(),
				produto.getPreco()
			);

			BigDecimal itemSubtotal = precoUnitarioAplicado.multiply(BigDecimal.valueOf(itemRequest.quantidade()));
			itemSubtotal = itemSubtotal.setScale(2, RoundingMode.HALF_UP);

			PedidoItem item = new PedidoItem();
			item.setPedido(pedido);
			item.setProduto(produto);
			item.setQuantidade(itemRequest.quantidade());
			item.setPrecoUnitario(precoUnitarioAplicado);
			item.setSubtotal(itemSubtotal);
			pedido.getItens().add(item);

			subtotal = subtotal.add(itemSubtotal);
		}

		pedido.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
		pedido.setTotal(subtotal.setScale(2, RoundingMode.HALF_UP));

		Pedido salvo = pedidoRepository.save(pedido);

		auditoriaService.registrar(
			emailAutenticado,
			"PEDIDO_CRIADO",
			"Pedido",
			salvo.getId(),
			"canalPedido=" + salvo.getCanalPedido() + "; status=" + salvo.getStatus()
		);

		return toResponse(salvo);
	}

	@Transactional(readOnly = true)
	public Page<PedidoResponse> listarMeusPedidos(
		String emailAutenticado,
		StatusPedido status,
		CanalPedido canalPedido,
		String dataInicio,
		String dataFim,
		Pageable pageable
	) {
		Cliente cliente = findClienteByEmail(emailAutenticado);

		Page<Pedido> pedidos = pedidoRepository.findByFiltros(
			cliente.getId(),
			null,
			status,
			canalPedido,
			parseDateTimeStart(dataInicio),
			parseDateTimeEnd(dataFim),
			pageable
		);

		return pedidos.map(this::toResponse);
	}

	@Transactional(readOnly = true)
	public PedidoResponse buscarMeuPedido(Long pedidoId, String emailAutenticado) {
		Cliente cliente = findClienteByEmail(emailAutenticado);

		Pedido pedido = pedidoRepository.findByIdAndClienteId(pedidoId, cliente.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

		return toResponse(pedido);
	}

	@Transactional(readOnly = true)
	public Page<PedidoResponse> listarPedidosComFiltros(
		Long unidadeId,
		Long clienteId,
		StatusPedido status,
		CanalPedido canalPedido,
		String dataInicio,
		String dataFim,
		Pageable pageable
	) {
		Page<Pedido> pedidos = pedidoRepository.findByFiltros(
			clienteId,
			unidadeId,
			status,
			canalPedido,
			parseDateTimeStart(dataInicio),
			parseDateTimeEnd(dataFim),
			pageable
		);

		return pedidos.map(this::toResponse);
	}

	@Transactional
	public PedidoResponse atualizarStatus(Long pedidoId, StatusPedido novoStatus, String emailAutenticado) {
		Pedido pedido = pedidoRepository.findById(pedidoId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

		StatusPedido statusAtual = pedido.getStatus();
		if (statusAtual == novoStatus) {
			return toResponse(pedido);
		}

		if (!isTransitionAllowed(statusAtual, novoStatus)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Transicao de status invalida");
		}

		if (novoStatus == StatusPedido.CANCELADO && statusComEstoqueBaixado(statusAtual)) {
			estornarEstoquePorCancelamento(pedido);
		}

		pedido.setStatus(novoStatus);
		Pedido salvo = pedidoRepository.save(pedido);

		auditoriaService.registrar(
			emailAutenticado,
			"STATUS_PEDIDO_ALTERADO",
			"Pedido",
			salvo.getId(),
			"novoStatus=" + novoStatus.name()
		);

		return toResponse(salvo);
	}

	private Cliente findClienteByEmail(String email) {
		Usuario usuario = usuarioRepository.findByEmail(normalizeEmail(email))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado"));

		if (usuario.getPerfil() != PerfilUsuario.CLIENTE) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas clientes podem operar pedidos");
		}

		return clienteRepository.findByUsuarioId(usuario.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
	}

	private String normalizeEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}

	private Instant parseDateTimeStart(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			return Instant.parse(value);
		} catch (DateTimeParseException ex) {
			try {
				return LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC);
			} catch (DateTimeParseException ignored) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataInicio invalida");
			}
		}
	}

	private Instant parseDateTimeEnd(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		try {
			return Instant.parse(value);
		} catch (DateTimeParseException ex) {
			try {
				return LocalDate.parse(value).atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
			} catch (DateTimeParseException ignored) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataFim invalida");
			}
		}
	}

	private boolean isTransitionAllowed(StatusPedido statusAtual, StatusPedido novoStatus) {
		return switch (statusAtual) {
			case AGUARDANDO_PAGAMENTO -> EnumSet.of(
				StatusPedido.PAGAMENTO_APROVADO,
				StatusPedido.PAGAMENTO_RECUSADO,
				StatusPedido.CANCELADO
			).contains(novoStatus);
			case PAGAMENTO_APROVADO -> EnumSet.of(
				StatusPedido.RECEBIDO,
				StatusPedido.EM_PREPARO,
				StatusPedido.CANCELADO
			).contains(novoStatus);
			case PAGAMENTO_RECUSADO -> novoStatus == StatusPedido.CANCELADO;
			case RECEBIDO -> EnumSet.of(StatusPedido.EM_PREPARO, StatusPedido.CANCELADO).contains(novoStatus);
			case EM_PREPARO -> EnumSet.of(StatusPedido.PRONTO, StatusPedido.CANCELADO).contains(novoStatus);
			case PRONTO -> EnumSet.of(StatusPedido.ENTREGUE, StatusPedido.CANCELADO).contains(novoStatus);
			case ENTREGUE, CANCELADO -> false;
		};
	}

	private boolean statusComEstoqueBaixado(StatusPedido status) {
		return EnumSet.of(
			StatusPedido.PAGAMENTO_APROVADO,
			StatusPedido.RECEBIDO,
			StatusPedido.EM_PREPARO,
			StatusPedido.PRONTO
		).contains(status);
	}

	private void estornarEstoquePorCancelamento(Pedido pedido) {
		List<MovimentoEstoque> movimentos = new ArrayList<>();

		for (PedidoItem item : pedido.getItens()) {
			Estoque estoque = estoqueRepository.findByUnidadeIdAndProdutoId(
				pedido.getUnidade().getId(),
				item.getProduto().getId()
			).orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Estoque nao encontrado para estorno"));

			estoque.setQuantidadeAtual(estoque.getQuantidadeAtual() + item.getQuantidade());

			MovimentoEstoque movimento = new MovimentoEstoque();
			movimento.setEstoque(estoque);
			movimento.setPedido(pedido);
			movimento.setTipo(TipoMovimentoEstoque.ENTRADA);
			movimento.setQuantidade(item.getQuantidade());
			movimento.setMotivo("Estorno por cancelamento do pedido " + pedido.getId());
			movimentos.add(movimento);
		}

		if (!movimentos.isEmpty()) {
			movimentoEstoqueRepository.saveAll(movimentos);
		}
	}

	private PedidoResponse toResponse(Pedido pedido) {
		List<PedidoItemResponse> itens = pedido.getItens().stream()
			.map(item -> new PedidoItemResponse(
				item.getProduto().getId(),
				item.getProduto().getNome(),
				item.getQuantidade(),
				item.getPrecoUnitario(),
				item.getSubtotal()
			))
			.toList();

		return new PedidoResponse(
			pedido.getId(),
			pedido.getCliente().getId(),
			pedido.getUnidade().getId(),
			pedido.getUnidade().getNome(),
			pedido.getCanalPedido(),
			pedido.getFormaPagamento(),
			pedido.getStatus(),
			pedido.getSubtotal(),
			pedido.getTotal(),
			pedido.getCreatedAt(),
			itens
		);
	}
}
