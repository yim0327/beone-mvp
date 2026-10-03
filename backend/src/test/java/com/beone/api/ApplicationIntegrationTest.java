package com.beone.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.beone.api.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

@IntegrationTest
class ApplicationIntegrationTest {

	@LocalServerPort
	int port;

	@Test
	void healthEndpointReportsUp() {
		String body = RestClient.create("http://localhost:" + port)
			.get()
			.uri("/actuator/health")
			.retrieve()
			.body(String.class);

		assertThat(body).contains("\"status\":\"UP\"");
	}

}
