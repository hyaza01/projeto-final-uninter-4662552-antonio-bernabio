package br.com.raizesdonordeste.backend;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class FidelidadeClienteIntegrationTests {

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
	void shouldAllowClientToReadOwnFidelityByClienteId() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		long clienteId = findClienteIdFromMe(tokenCliente);

		mockMvc.perform(get("/api/v1/clientes/{clienteId}/fidelidade/saldo", clienteId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.clienteId").value(clienteId));

		mockMvc.perform(get("/api/v1/clientes/{clienteId}/fidelidade/historico", clienteId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray());

		mockMvc.perform(patch("/api/v1/clientes/{clienteId}/fidelidade/consentimento", clienteId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"consentimentoFidelidade\":true}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.consentimentoFidelidade").value(true));

		Integer auditorias = jdbcTemplate.queryForObject(
			"SELECT COUNT(*) FROM auditorias WHERE acao = 'ALTERACAO_CONSENTIMENTO_FIDELIDADE' AND entidade = 'Cliente' AND entidade_id = ?",
			Integer.class,
			clienteId
		);
		assertTrue(auditorias != null && auditorias >= 1);
	}

	@Test
	void shouldForbidClientReadingAnotherClientFidelity() throws Exception {
		String tokenClienteA = registerClientAndGetToken();
		String tokenClienteB = registerClientAndGetToken();
		long clienteIdB = findClienteIdFromMe(tokenClienteB);

		mockMvc.perform(get("/api/v1/clientes/{clienteId}/fidelidade/saldo", clienteIdB)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenClienteA))
			.andExpect(status().isForbidden());
	}

	@Test
	void shouldAllowManagerToConsultClientFidelity() throws Exception {
		String tokenCliente = registerClientAndGetToken();
		long clienteId = findClienteIdFromMe(tokenCliente);
		String tokenGerente = createInternalUserAndGetToken(PerfilUsuario.GERENTE, "SenhaGerente@123");

		mockMvc.perform(get("/api/v1/clientes/{clienteId}/fidelidade/saldo", clienteId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.clienteId").value(clienteId));

		mockMvc.perform(get("/api/v1/clientes/{clienteId}/fidelidade/historico", clienteId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenGerente))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(0)));
	}

	private long findClienteIdFromMe(String token) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/v1/fidelidade/me")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
			.andExpect(status().isOk())
			.andReturn();

		JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
		return json.get("clienteId").asLong();
	}

	private String registerClientAndGetToken() throws Exception {
		String email = "fidelidade.cliente." + System.nanoTime() + "@raizes.local";
		Map<String, String> body = Map.of(
			"nome", "Cliente Fidelidade",
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
		String email = perfil.name().toLowerCase(Locale.ROOT) + ".fidelidade." + System.nanoTime() + "@raizes.local";

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
}
