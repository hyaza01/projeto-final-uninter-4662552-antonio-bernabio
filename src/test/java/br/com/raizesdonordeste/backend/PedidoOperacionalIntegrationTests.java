package br.com.raizesdonordeste.backend;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.raizesdonordeste.backend.domain.enums.PerfilUsuario;
import br.com.raizesdonordeste.backend.domain.model.Usuario;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(EmbeddedPostgresTestConfig.class)
class PedidoOperacionalIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void shouldListOrdersByUnitForInternalRole() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		String tokenGerente = createInternalUserAndGetToken(PerfilUsuario.GERENTE, "SenhaGerente@123");
		EstoqueBase estoque = findAnyStockWithMinimum(3);

		Long pedidoId = createOrderAndReturnId(tokenCliente, estoque.unidadeId(), estoque.produtoId(), 1);

		MvcResult result = mockMvc.perform(get("/api/v1/pedidos/unidade/{unidadeId}", estoque.unidadeId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
			.andReturn();

		JsonNode pedidos = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
		boolean contemPedido = false;
		for (JsonNode pedido : pedidos) {
			if (pedido.get("id").asLong() == pedidoId) {
				contemPedido = true;
				break;
			}
		}

		assertTrue(contemPedido);
	}

	@Test
	void shouldUpdateOrderStatusToPreparing() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		String tokenAtendente = createInternalUserAndGetToken(PerfilUsuario.ATENDENTE, "SenhaAtendente@123");
		EstoqueBase estoque = findAnyStockWithMinimum(3);

		Long pedidoId = createOrderAndReturnId(tokenCliente, estoque.unidadeId(), estoque.produtoId(), 1);

		Map<String, String> body = Map.of("novoStatus", "EM_PREPARO");

		mockMvc.perform(patch("/api/v1/pedidos/{pedidoId}/status", pedidoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAtendente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(pedidoId))
			.andExpect(jsonPath("$.status").value("EM_PREPARO"));
	}

	@Test
	void shouldRejectInvalidStatusTransition() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		String tokenGerente = createInternalUserAndGetToken(PerfilUsuario.GERENTE, "SenhaGerente@123");
		EstoqueBase estoque = findAnyStockWithMinimum(3);

		Long pedidoId = createOrderAndReturnId(tokenCliente, estoque.unidadeId(), estoque.produtoId(), 1);

		Map<String, String> body = Map.of("novoStatus", "PRONTO");

		mockMvc.perform(patch("/api/v1/pedidos/{pedidoId}/status", pedidoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isConflict());
	}

	@Test
	void shouldForbidClientOnOperationalEndpoints() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		EstoqueBase estoque = findAnyStockWithMinimum(3);
		Long pedidoId = createOrderAndReturnId(tokenCliente, estoque.unidadeId(), estoque.produtoId(), 1);

		Map<String, String> body = Map.of("novoStatus", "EM_PREPARO");

		mockMvc.perform(get("/api/v1/pedidos/unidade/{unidadeId}", estoque.unidadeId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
			.andExpect(status().isForbidden());

		mockMvc.perform(patch("/api/v1/pedidos/{pedidoId}/status", pedidoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isForbidden());
	}

	@Test
	void shouldRestockWhenOrderIsCancelled() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		String tokenGerente = createInternalUserAndGetToken(PerfilUsuario.GERENTE, "SenhaGerente@123");
		EstoqueBase estoque = findAnyStockWithMinimum(5);
		int quantidadePedido = 2;

		int estoqueAntes = findCurrentStock(estoque.unidadeId(), estoque.produtoId());
		Long pedidoId = createOrderAndReturnId(tokenCliente, estoque.unidadeId(), estoque.produtoId(), quantidadePedido);
		int estoqueDepoisCriacao = findCurrentStock(estoque.unidadeId(), estoque.produtoId());
		assertEquals(estoqueAntes - quantidadePedido, estoqueDepoisCriacao);

		Map<String, String> body = Map.of("novoStatus", "CANCELADO");

		mockMvc.perform(patch("/api/v1/pedidos/{pedidoId}/status", pedidoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CANCELADO"));

		int estoqueDepoisCancelamento = findCurrentStock(estoque.unidadeId(), estoque.produtoId());
		assertEquals(estoqueAntes, estoqueDepoisCancelamento);

		Integer movimentosEntrada = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM movimentos_estoque WHERE pedido_id = ? AND tipo = 'ENTRADA'",
			Integer.class,
			pedidoId
		);
		assertEquals(1, movimentosEntrada);
	}

	private Long createOrderAndReturnId(String token, Long unidadeId, Long produtoId, int quantidade) throws Exception {
		Map<String, Object> body = createOrderBody(unidadeId, produtoId, quantidade);

		MvcResult result = mockMvc.perform(post("/api/v1/pedidos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isCreated())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
		return json.get("id").asLong();
	}

	private Map<String, Object> createOrderBody(Long unidadeId, Long produtoId, int quantidade) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("unidadeId", unidadeId);
		body.put("canalPedido", "APP");
		body.put("formaPagamento", "PIX");
		body.put("itens", List.of(Map.of(
			"produtoId", produtoId,
			"quantidade", quantidade
		)));
		return body;
	}

	private EstoqueBase findAnyStockWithMinimum(int minimo) {
		Map<String, Object> row = jdbcTemplate.queryForMap(
			"SELECT unidade_id, produto_id, quantidade_atual FROM estoques WHERE quantidade_atual >= ? ORDER BY id LIMIT 1",
			minimo
		);

		long unidadeId = ((Number) row.get("unidade_id")).longValue();
		long produtoId = ((Number) row.get("produto_id")).longValue();
		int quantidadeAtual = ((Number) row.get("quantidade_atual")).intValue();
		return new EstoqueBase(unidadeId, produtoId, quantidadeAtual);
	}

	private int findCurrentStock(Long unidadeId, Long produtoId) {
		Integer quantidadeAtual = jdbcTemplate.queryForObject(
			"SELECT quantidade_atual FROM estoques WHERE unidade_id = ? AND produto_id = ?",
			Integer.class,
			unidadeId,
			produtoId
		);
		return quantidadeAtual == null ? 0 : quantidadeAtual;
	}

	private String registerClientAndGetToken() throws Exception {
		String email = "pedido.op.cliente." + System.nanoTime() + "@raizes.local";
		Map<String, String> body = Map.of(
			"nome", "Cliente Operacao",
			"email", email,
			"senha", "Senha@123"
		);

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isCreated());

		return loginAndGetToken(email, "Senha@123");
	}

	private String createInternalUserAndGetToken(PerfilUsuario perfil, String senha) throws Exception {
		String email = perfil.name().toLowerCase(Locale.ROOT) + ".pedido.op." + System.nanoTime() + "@raizes.local";

		Usuario usuario = new Usuario();
		usuario.setNome("Usuario " + perfil.name());
		usuario.setEmail(email);
		usuario.setSenhaHash(passwordEncoder.encode(senha));
		usuario.setPerfil(perfil);
		usuario.setAtivo(true);
		usuarioRepository.save(usuario);

		return loginAndGetToken(email, senha);
	}

	private String loginAndGetToken(String email, String senha) throws Exception {
		Map<String, String> body = Map.of(
			"email", email,
			"senha", senha
		);

		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isOk())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
		return json.get("accessToken").asText();
	}

	private record EstoqueBase(long unidadeId, long produtoId, int quantidadeAtual) {
	}
}
