package br.com.raizesdonordeste.backend;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
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
class CatalogoEstoqueIntegrationTests {

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
	void shouldListCatalogProductsWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/catalogo/produtos"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(greaterThan(0)))
			.andExpect(jsonPath("$[0].id").isNumber())
			.andExpect(jsonPath("$[0].nome").isNotEmpty());
	}

	@Test
	void shouldFilterCatalogProductsByCategory() throws Exception {
		mockMvc.perform(get("/api/v1/catalogo/produtos").param("categoria", "LANCHE"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(greaterThan(0)))
			.andExpect(jsonPath("$[0].categoria").value("LANCHE"));
	}

	@Test
	void shouldGetCatalogProductById() throws Exception {
		MvcResult listResult = mockMvc.perform(get("/api/v1/catalogo/produtos"))
			.andExpect(status().isOk())
			.andReturn();

		JsonNode produtos = objectMapper.readTree(listResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
		long produtoId = produtos.get(0).get("id").asLong();

		mockMvc.perform(get("/api/v1/catalogo/produtos/{produtoId}", produtoId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(produtoId));
	}

	@Test
	void shouldListCardapioByUnidadeWithoutAuthentication() throws Exception {
		EstoqueIds ids = findAnyEstoqueIds();

		mockMvc.perform(get("/api/v1/unidades/{unidadeId}/cardapio", ids.unidadeId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(greaterThan(0)))
			.andExpect(jsonPath("$[0].produtoId").isNumber())
			.andExpect(jsonPath("$[0].disponivel").isBoolean())
			.andExpect(jsonPath("$[0].quantidadeDisponivel").isNumber());
	}

	@Test
	void shouldReturnNotFoundForCardapioWhenUnitDoesNotExist() throws Exception {
		mockMvc.perform(get("/api/v1/unidades/{unidadeId}/cardapio", 999999L))
			.andExpect(status().isNotFound());
	}

	@Test
	void shouldRequireAuthenticationForStockEndpoint() throws Exception {
		EstoqueIds ids = findAnyEstoqueIds();

		mockMvc.perform(get("/api/v1/estoque/unidades/{unidadeId}/produtos/{produtoId}", ids.unidadeId(), ids.produtoId()))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldForbidClientFromStockEndpoint() throws Exception {
		EstoqueIds ids = findAnyEstoqueIds();
		String tokenCliente = registerClientAndGetToken();

		mockMvc.perform(get("/api/v1/estoque/unidades/{unidadeId}/produtos/{produtoId}", ids.unidadeId(), ids.produtoId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
			.andExpect(status().isForbidden());
	}

	@Test
	void shouldAllowManagerToAccessStockEndpoint() throws Exception {
		EstoqueIds ids = findAnyEstoqueIds();
		String tokenGerente = createInternalUserAndGetToken(PerfilUsuario.GERENTE, "SenhaGerente@123");

		mockMvc.perform(get("/api/v1/estoque/unidades/{unidadeId}/produtos/{produtoId}", ids.unidadeId(), ids.produtoId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.unidadeId").value(ids.unidadeId()))
			.andExpect(jsonPath("$.produtoId").value(ids.produtoId()))
			.andExpect(jsonPath("$.quantidadeAtual").isNumber())
			.andExpect(jsonPath("$.estoqueMinimo").isNumber());
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

	private EstoqueIds findAnyEstoqueIds() {
		Map<String, Object> row = jdbcTemplate.queryForMap(
			"SELECT unidade_id, produto_id FROM estoques ORDER BY id LIMIT 1"
		);

		long unidadeId = ((Number) row.get("unidade_id")).longValue();
		long produtoId = ((Number) row.get("produto_id")).longValue();
		assertTrue(unidadeId > 0 && produtoId > 0);
		return new EstoqueIds(unidadeId, produtoId);
	}

	private String registerClientAndGetToken() throws Exception {
		String email = "cliente.catalogo." + System.nanoTime() + "@raizes.local";
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
		String email = perfil.name().toLowerCase(Locale.ROOT) + ".catalogo." + System.nanoTime() + "@raizes.local";

		Usuario usuario = new Usuario();
		usuario.setNome("Usuario " + perfil.name());
		usuario.setEmail(email);
		usuario.setSenhaHash(passwordEncoder.encode(senha));
		usuario.setPerfil(perfil);
		usuario.setAtivo(true);
		usuarioRepository.save(usuario);

		return loginAndGetToken(email, senha);
	}

	private record EstoqueIds(long unidadeId, long produtoId) {
	}
}
