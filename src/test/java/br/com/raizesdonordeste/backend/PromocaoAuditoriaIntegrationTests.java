package br.com.raizesdonordeste.backend;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class PromocaoAuditoriaIntegrationTests {

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
	void shouldCreateAndUpdatePromotionAndAuditAsManager() throws Exception {
		String tokenGerente = createInternalUserAndGetToken(PerfilUsuario.GERENTE, "SenhaGerente@123");
		IdsBase ids = findAnyProdutoUnidade();

		Map<String, Object> createBody = new LinkedHashMap<>();
		createBody.put("nome", "Promocao Sao Joao " + System.nanoTime());
		createBody.put("descricao", "Desconto especial para cardapio regional");
		createBody.put("percentualDesconto", 10.0);
		createBody.put("dataInicio", "2026-06-01");
		createBody.put("dataFim", "2026-06-30");
		createBody.put("ativa", true);
		createBody.put("produtoId", ids.produtoId());
		createBody.put("unidadeId", ids.unidadeId());

		MvcResult createResult = mockMvc.perform(post("/api/v1/promocoes")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createBody)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.ativa").value(true))
			.andReturn();

		JsonNode promocao = objectMapper.readTree(createResult.getResponse().getContentAsString(StandardCharsets.UTF_8));
		long promocaoId = promocao.get("id").asLong();

		Map<String, Object> statusBody = Map.of("ativa", false);
		mockMvc.perform(patch("/api/v1/promocoes/{promocaoId}/status", promocaoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(statusBody)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(promocaoId))
			.andExpect(jsonPath("$.ativa").value(false));

		MvcResult listResult = mockMvc.perform(get("/api/v1/promocoes")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.param("ativa", "false")
				.param("page", "0")
				.param("limit", "20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(1)))
			.andReturn();

		JsonNode listContent = objectMapper.readTree(listResult.getResponse().getContentAsString(StandardCharsets.UTF_8)).get("content");
		boolean encontrouPromocao = false;
		for (JsonNode item : listContent) {
			if (item.get("id").asLong() == promocaoId) {
				encontrouPromocao = true;
				break;
			}
		}
		assertTrue(encontrouPromocao);

		mockMvc.perform(get("/api/v1/auditorias")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.param("entidade", "Promocao")
				.param("entidadeId", String.valueOf(promocaoId))
				.param("page", "0")
				.param("limit", "20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(2)));
	}

	@Test
	void shouldRejectPromotionWithInvalidDiscount() throws Exception {
		String tokenGerente = createInternalUserAndGetToken(PerfilUsuario.GERENTE, "SenhaGerente@123");

		Map<String, Object> createBody = new LinkedHashMap<>();
		createBody.put("nome", "Promocao invalida");
		createBody.put("descricao", "Percentual invalido");
		createBody.put("percentualDesconto", 0.0);
		createBody.put("dataInicio", "2026-06-01");
		createBody.put("dataFim", "2026-06-30");
		createBody.put("ativa", true);
		createBody.put("produtoId", null);
		createBody.put("unidadeId", null);

		mockMvc.perform(post("/api/v1/promocoes")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createBody)))
			.andExpect(status().isUnprocessableEntity());
	}

	@Test
	void shouldForbidClientAccessingPromotionManagementAndAuditoria() throws Exception {
		String tokenCliente = registerClientAndGetToken();

		Map<String, Object> createBody = new LinkedHashMap<>();
		createBody.put("nome", "Promocao cliente");
		createBody.put("descricao", "Nao permitido");
		createBody.put("percentualDesconto", 5.0);
		createBody.put("dataInicio", "2026-06-01");
		createBody.put("dataFim", "2026-06-30");
		createBody.put("ativa", true);
		createBody.put("produtoId", null);
		createBody.put("unidadeId", null);

		mockMvc.perform(post("/api/v1/promocoes")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createBody)))
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/v1/auditorias")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
			.andExpect(status().isForbidden());
	}

	private IdsBase findAnyProdutoUnidade() {
		Map<String, Object> row = jdbcTemplate.queryForMap(
			"SELECT p.id AS produto_id, u.id AS unidade_id FROM produtos p CROSS JOIN unidades u WHERE p.ativo = true AND u.ativa = true LIMIT 1"
		);

		return new IdsBase(
			((Number) row.get("produto_id")).longValue(),
			((Number) row.get("unidade_id")).longValue()
		);
	}

	private String registerClientAndGetToken() throws Exception {
		String email = "promocao.cliente." + System.nanoTime() + "@raizes.local";
		Map<String, String> body = Map.of(
			"nome", "Cliente Promocao",
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
		String email = perfil.name().toLowerCase(Locale.ROOT) + ".promocao." + System.nanoTime() + "@raizes.local";

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

	private record IdsBase(long produtoId, long unidadeId) {
	}
}
