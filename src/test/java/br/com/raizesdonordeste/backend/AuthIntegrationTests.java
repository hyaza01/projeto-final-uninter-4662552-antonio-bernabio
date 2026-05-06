package br.com.raizesdonordeste.backend;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.raizesdonordeste.backend.domain.model.Usuario;
import br.com.raizesdonordeste.backend.infrastructure.persistence.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Import(EmbeddedPostgresTestConfig.class)
class AuthIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void shouldRegisterUserWithHashedPassword() throws Exception {
		String email = "novo." + System.currentTimeMillis() + "@raizes.local";
		Map<String, String> body = Map.of(
			"nome", "Cliente Novo",
			"email", email,
			"senha", "Senha@123"
		);

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.email").value(email))
			.andExpect(jsonPath("$.perfil").value("CLIENTE"))
			.andExpect(jsonPath("$.senhaHash").doesNotExist());

		Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
		assertNotEquals("Senha@123", usuario.getSenhaHash());
		assertTrue(passwordEncoder.matches("Senha@123", usuario.getSenhaHash()));
	}

	@Test
	void shouldLoginWithValidCredentials() throws Exception {
		String email = registerClientAndReturnEmail();
		Map<String, String> body = Map.of(
			"email", email,
			"senha", "Senha@123"
		);

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.usuario.email").value(email))
			.andExpect(jsonPath("$.usuario.senhaHash").doesNotExist());
	}

	@Test
	void shouldReturnUnauthorizedForInvalidLogin() throws Exception {
		String email = registerClientAndReturnEmail();
		Map<String, String> body = Map.of(
			"email", email,
			"senha", "errada"
		);

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldReturnUnauthorizedWithoutTokenOnProtectedRoute() throws Exception {
		mockMvc.perform(get("/api/v1/private/ping"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldReturnForbiddenForClientAccessingAdminRoute() throws Exception {
		String email = registerClientAndReturnEmail();
		String tokenCliente = loginAndGetToken(email, "Senha@123");

		mockMvc.perform(get("/api/v1/admin/ping")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente))
			.andExpect(status().isForbidden());
	}

	private String registerClientAndReturnEmail() throws Exception {
		String email = "auth." + System.nanoTime() + "@raizes.local";
		Map<String, String> body = Map.of(
			"nome", "Cliente Login",
			"email", email,
			"senha", "Senha@123"
		);

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(body)))
			.andExpect(status().isCreated());

		return email;
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
