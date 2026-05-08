package br.com.raizesdonordeste.backend;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(EmbeddedPostgresTestConfig.class)
class PagamentoFidelidadeIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void shouldApprovePaymentDecreaseStockAndAudit() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		EstoqueBase estoque = findAnyStockWithMinimum(4);
		int quantidadePedido = 2;

		int estoqueAntes = findCurrentStock(estoque.unidadeId(), estoque.produtoId());
		JsonNode pedido = createOrder(tokenCliente, estoque.unidadeId(), estoque.produtoId(), quantidadePedido);
		long pedidoId = pedido.get("id").asLong();

		int estoqueDepoisCriacao = findCurrentStock(estoque.unidadeId(), estoque.produtoId());
		assertEquals(estoqueAntes, estoqueDepoisCriacao);

		MvcResult pagamentoResult = processarPagamento(tokenCliente, pedidoId, true)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.statusPagamento").value("APROVADO"))
			.andExpect(jsonPath("$.statusPedido").value("PAGAMENTO_APROVADO"))
			.andReturn();

		JsonNode pagamentoJson = objectMapper.readTree(pagamentoResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
		long pagamentoId = pagamentoJson.get("pagamentoId").asLong();
		assertTrue(pagamentoId > 0);

		mockMvc.perform(get("/api/v1/pagamentos/{pagamentoId}", pagamentoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.pagamentoId").value(pagamentoId))
			.andExpect(jsonPath("$.statusPagamento").value("APROVADO"));

		int estoqueDepoisPagamento = findCurrentStock(estoque.unidadeId(), estoque.produtoId());
		assertEquals(estoqueAntes - quantidadePedido, estoqueDepoisPagamento);

		Integer movimentosSaida = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM movimentos_estoque WHERE pedido_id = ? AND tipo = 'SAIDA_PEDIDO'",
			Integer.class,
			pedidoId
		);
		assertEquals(1, movimentosSaida);

		Integer auditoriasPagamento = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM auditorias WHERE entidade = 'Pedido' AND entidade_id = ? AND acao = 'PAGAMENTO_APROVADO'",
			Integer.class,
			pedidoId
		);
		assertEquals(1, auditoriasPagamento);

		processarPagamento(tokenCliente, pedidoId, true)
			.andExpect(status().isConflict());
	}

	@Test
	void shouldReturnConflictWhenOrderIsCancelled() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		EstoqueBase estoque = findAnyStockWithMinimum(2);

		JsonNode pedido = createOrder(tokenCliente, estoque.unidadeId(), estoque.produtoId(), 1);
		long pedidoId = pedido.get("id").asLong();

		jdbcTemplate.update("UPDATE pedidos SET status = 'CANCELADO' WHERE id = ?", pedidoId);

		processarPagamento(tokenCliente, pedidoId, true)
			.andExpect(status().isConflict());
	}

	@Test
	void shouldReturnConflictWhenOrderIsDelivered() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		EstoqueBase estoque = findAnyStockWithMinimum(2);

		JsonNode pedido = createOrder(tokenCliente, estoque.unidadeId(), estoque.produtoId(), 1);
		long pedidoId = pedido.get("id").asLong();

		jdbcTemplate.update("UPDATE pedidos SET status = 'ENTREGUE' WHERE id = ?", pedidoId);

		processarPagamento(tokenCliente, pedidoId, true)
			.andExpect(status().isConflict());
	}

	@Test
	void shouldRejectPaymentAndKeepStock() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		EstoqueBase estoque = findAnyStockWithMinimum(3);

		int estoqueAntes = findCurrentStock(estoque.unidadeId(), estoque.produtoId());
		JsonNode pedido = createOrder(tokenCliente, estoque.unidadeId(), estoque.produtoId(), 1);
		long pedidoId = pedido.get("id").asLong();

		processarPagamento(tokenCliente, pedidoId, false)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.statusPagamento").value("RECUSADO"))
			.andExpect(jsonPath("$.statusPedido").value("PAGAMENTO_RECUSADO"));

		int estoqueDepois = findCurrentStock(estoque.unidadeId(), estoque.produtoId());
		assertEquals(estoqueAntes, estoqueDepois);

		Integer movimentosSaida = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM movimentos_estoque WHERE pedido_id = ? AND tipo = 'SAIDA_PEDIDO'",
			Integer.class,
			pedidoId
		);
		assertEquals(0, movimentosSaida);
	}

	@Test
	void shouldGenerateFidelityWhenConsentEnabled() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		EstoqueBase estoque = findAnyStockWithMinimum(3);

		Map<String, Object> consentBody = Map.of("consentimentoFidelidade", true);
		mockMvc.perform(patch("/api/v1/fidelidade/me/consentimento")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(consentBody)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.consentimentoFidelidade").value(true));

		JsonNode pedido = createOrder(tokenCliente, estoque.unidadeId(), estoque.produtoId(), 1);
		long pedidoId = pedido.get("id").asLong();
		int pontosEsperados = new BigDecimal(pedido.get("total").asText()).intValue();

		processarPagamento(tokenCliente, pedidoId, true)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.statusPagamento").value("APROVADO"));

		mockMvc.perform(get("/api/v1/fidelidade/me")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.consentimentoFidelidade").value(true))
			.andExpect(jsonPath("$.pontosSaldo").value(greaterThanOrEqualTo(pontosEsperados)));

		mockMvc.perform(get("/api/v1/fidelidade/me/historico")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
			.andExpect(jsonPath("$[0].tipo").value("CREDITO"));

		Integer fidelidadeMovimentos = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM fidelidade_historico WHERE pedido_id = ?",
			Integer.class,
			pedidoId
		);
		assertEquals(1, fidelidadeMovimentos);
	}

	private org.springframework.test.web.servlet.ResultActions processarPagamento(
		String token,
		Long pedidoId,
		boolean forcarAprovacao
	) throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("resultadoMock", forcarAprovacao ? "APROVADO" : "RECUSADO");
		body.put("metodo", "MOCK");

		return mockMvc.perform(post("/api/v1/pedidos/{pedidoId}/pagamentos/mock", pedidoId)
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(body)));
	}

	private JsonNode createOrder(String token, Long unidadeId, Long produtoId, int quantidade) throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("unidadeId", unidadeId);
		body.put("canalPedido", "APP");
		body.put("formaPagamento", "PIX");
		body.put("itens", List.of(Map.of(
			"produtoId", produtoId,
			"quantidade", quantidade
		)));

		MvcResult result = mockMvc.perform(post("/api/v1/pedidos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isCreated())
			.andReturn();

		return objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
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
		String email = "pagamento.cliente." + System.nanoTime() + "@raizes.local";
		Map<String, String> body = Map.of(
			"nome", "Cliente Pagamento",
			"email", email,
			"senha", "Senha@123"
		);

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isCreated());

		Map<String, String> loginBody = Map.of(
			"email", email,
			"senha", "Senha@123"
		);

		MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginBody)))
			.andExpect(status().isOk())
			.andReturn();

		JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
		return loginJson.get("accessToken").asText();
	}

	private record EstoqueBase(long unidadeId, long produtoId, int quantidadeAtual) {
	}
}
