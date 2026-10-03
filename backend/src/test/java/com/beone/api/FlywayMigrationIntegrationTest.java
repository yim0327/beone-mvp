package com.beone.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.beone.api.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class FlywayMigrationIntegrationTest {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	void baselineMigrationIsApplied() {
		Boolean success = jdbcTemplate.queryForObject(
				"SELECT success FROM flyway_schema_history WHERE version = '1'", Boolean.class);

		assertThat(success).isTrue();
	}

	@Test
	void runsAgainstPinnedMySqlVersion() {
		String version = jdbcTemplate.queryForObject("SELECT VERSION()", String.class);

		assertThat(version).startsWith("8.4.11");
	}

}
