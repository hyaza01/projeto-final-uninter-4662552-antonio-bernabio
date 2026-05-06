package br.com.raizesdonordeste.backend;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

@TestConfiguration
public class EmbeddedPostgresTestConfig {

	@Bean(destroyMethod = "close")
	EmbeddedPostgres embeddedPostgres() throws IOException {
		return EmbeddedPostgres.builder().setPort(0).start();
	}

	@Bean
	@Primary
	DataSource dataSource(EmbeddedPostgres embeddedPostgres) {
		return embeddedPostgres.getPostgresDatabase();
	}
}
