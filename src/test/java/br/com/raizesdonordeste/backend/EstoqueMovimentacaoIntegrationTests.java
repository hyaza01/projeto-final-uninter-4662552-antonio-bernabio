package br.com.raizesdonordeste.backend;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
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
class EstoqueMovimentacaoIntegrationTests {

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
	void shouldRegisterEntradaAndListMovementsAsManager() throws Exception {
		EstoqueBase estoque = findAnyStock();
		String tokenGerente = createInternalUserAndGetToken(PerfilUsuario.GERENTE, "SenhaGerente@123");

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("unidadeId", estoque.unidadeId());
		body.put("produtoId", estoque.produtoId());
		body.put("tipo", "ENTRADA");
		body.put("quantidade", 5);
		body.put("motivo", "Reposicao manual");

		mockMvc.perform(post("/api/v1/estoque/movimentacoes")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.estoqueId").value(estoque.estoqueId()))
			.andExpect(jsonPath("$.tipo").value("ENTRADA"));

		int saldoAtual = findCurrentStock(estoque.unidadeId(), estoque.produtoId());
		assertEquals(estoque.quantidadeAtual() + 5, saldoAtual);

		mockMvc.perform(get("/api/v1/estoque/{estoqueId}/movimentacoes", estoque.estoqueId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.param("page", "0")
				.param("limit", "10"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1)));

		Integer auditorias = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM auditorias WHERE acao = 'MOVIMENTACAO_ESTOQUE' AND entidade = 'Estoque' AND entidade_id = ?",
			Integer.class,
			estoque.estoqueId()
		);
		assertTrue(auditorias != null && auditorias >= 1);
	}

	@Test
	void shouldRejectSaidaGreaterThanStock() throws Exception {
		EstoqueBase estoque = findAnyStock();
		String tokenGerente = createInternalUserAndGetToken(PerfilUsuario.GERENTE, "SenhaGerente@123");

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("unidadeId", estoque.unidadeId());
		body.put("produtoId", estoque.produtoId());
		body.put("tipo", "SAIDA");
		body.put("quantidade", estoque.quantidadeAtual() + 10);
		body.put("motivo", "Ajuste indevido");

		mockMvc.perform(post("/api/v1/estoque/movimentacoes")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isConflict());
	}

	@Test
	void shouldRejectMovementWithInvalidQuantity() throws Exception {
		EstoqueBase estoque = findAnyStock();
		String tokenGerente = createInternalUserAndGetToken(PerfilUsuario.GERENTE, "SenhaGerente@123");

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("unidadeId", estoque.unidadeId());
		body.put("produtoId", estoque.produtoId());
		body.put("tipo", "ENTRADA");
		body.put("quantidade", 0);
		body.put("motivo", "Invalido");

		mockMvc.perform(post("/api/v1/estoque/movimentacoes")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldForbidClientOnManualStockMovement() throws Exception {
		EstoqueBase estoque = findAnyStock();
		String tokenCliente = registerClientAndGetToken();

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("unidadeId", estoque.unidadeId());
		body.put("produtoId", estoque.produtoId());
		body.put("tipo", "ENTRADA");
		body.put("quantidade", 1);
		body.put("motivo", "Teste cliente");

		mockMvc.perform(post("/api/v1/estoque/movimentacoes")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isForbidden());
	}

	private EstoqueBase findAnyStock() {
		Map<String, Object> row = jdbcTemplate.queryForMap(
			"SELECT id, unidade_id, produto_id, quantidade_atual FROM estoques ORDER BY id LIMIT 1"
		);

		return new EstoqueBase(
			((Number) row.get("id")).longValue(),
			((Number) row.get("unidade_id")).longValue(),
			((Number) row.get("produto_id")).longValue(),
			((Number) row.get("quantidade_atual")).intValue()
		);
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
		String email = "estoque.cliente." + System.nanoTime() + "@raizes.local";
		Map<String, String> body = Map.of(
			"nome", "Cliente Estoque",
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
		String email = perfil.name().toLowerCase(Locale.ROOT) + ".estoque." + System.nanoTime() + "@raizes.local";

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

	private record EstoqueBase(long estoqueId, long unidadeId, long produtoId, int quantidadeAtual) {
	}
}
