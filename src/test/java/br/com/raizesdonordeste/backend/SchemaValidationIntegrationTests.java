package br.com.raizesdonordeste.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import br.com.raizesdonordeste.backend.domain.enums.CanalPedido;
import br.com.raizesdonordeste.backend.domain.enums.PerfilUsuario;
import br.com.raizesdonordeste.backend.domain.enums.StatusPagamento;
import br.com.raizesdonordeste.backend.domain.enums.StatusPedido;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.flyway.enabled=true",
	"spring.jpa.hibernate.ddl-auto=validate",
	"spring.jpa.open-in-view=false"
})
@Import(EmbeddedPostgresTestConfig.class)
class SchemaValidationIntegrationTests {

	private static final Set<String> TABELAS_OBRIGATORIAS = Set.of(
		"usuarios",
		"clientes",
		"unidades",
		"produtos",
		"estoques",
		"pedidos",
		"pedido_itens",
		"pagamentos",
		"auditorias"
	);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private DataSource dataSource;

	@Test
	void shouldBootApplicationWithLocalDatabase() {
		Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		assertEquals(1, one);
	}

	@Test
	void shouldCreateRequiredTablesFromScratch() {
		List<String> tabelas = jdbcTemplate.queryForList(
			"SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
			String.class
		);

		Set<String> tabelasEncontradas = tabelas.stream().collect(Collectors.toSet());
		assertTrue(tabelasEncontradas.containsAll(TABELAS_OBRIGATORIAS));
	}

	@Test
	void shouldCreatePrimaryKeysAndForeignKeys() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();

			for (String tabela : TABELAS_OBRIGATORIAS) {
				try (ResultSet pk = metaData.getPrimaryKeys(null, "public", tabela)) {
					assertTrue(pk.next(), "Tabela sem PK: " + tabela);
				}
			}
		}

		Set<String> fksCriticas = jdbcTemplate.queryForList(
			"""
			SELECT tc.table_name || '.' || kcu.column_name || '->' || ccu.table_name || '.' || ccu.column_name AS fk
			FROM information_schema.table_constraints tc
			JOIN information_schema.key_column_usage kcu
			  ON tc.constraint_name = kcu.constraint_name
			JOIN information_schema.constraint_column_usage ccu
			  ON ccu.constraint_name = tc.constraint_name
			WHERE tc.constraint_type = 'FOREIGN KEY'
			ORDER BY fk
			""",
			String.class
		).stream().collect(Collectors.toSet());

		assertTrue(fksCriticas.contains("clientes.usuario_id->usuarios.id"));
		assertTrue(fksCriticas.contains("pedidos.cliente_id->clientes.id"));
		assertTrue(fksCriticas.contains("pedidos.unidade_id->unidades.id"));
		assertTrue(fksCriticas.contains("pedido_itens.pedido_id->pedidos.id"));
		assertTrue(fksCriticas.contains("pedido_itens.produto_id->produtos.id"));
		assertTrue(fksCriticas.contains("pagamentos.pedido_id->pedidos.id"));
		assertTrue(fksCriticas.contains("auditorias.usuario_id->usuarios.id"));
	}

	@Test
	void shouldHaveMainEnumsImplemented() {
		assertTrue(Set.of(PerfilUsuario.values()).contains(PerfilUsuario.ADMIN));
		assertTrue(Set.of(StatusPedido.values()).contains(StatusPedido.AGUARDANDO_PAGAMENTO));
		assertTrue(Set.of(CanalPedido.values()).contains(CanalPedido.TOTEM));
		assertTrue(Set.of(StatusPagamento.values()).contains(StatusPagamento.APROVADO));
		assertFalse(Set.of(StatusPagamento.values()).isEmpty());
	}

}
