package com.beone.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.beone.api.support.MySqlImage;
import org.junit.jupiter.api.Test;

class MySqlVersionAlignmentTest {

	private static final Path COMPOSE_FILE = Path.of("..", "docker-compose.yml");

	private static final Pattern MYSQL_IMAGE = Pattern.compile("(?m)^\\s*image:\\s*(mysql:\\S+)\\s*$");

	@Test
	void testcontainersImageMatchesDockerCompose() throws IOException {
		Matcher matcher = MYSQL_IMAGE.matcher(Files.readString(COMPOSE_FILE));

		assertThat(matcher.find()).as("mysql image in %s", COMPOSE_FILE).isTrue();
		assertThat(matcher.group(1)).isEqualTo(MySqlImage.TAG);
		assertThat(matcher.find()).as("only one mysql image in %s", COMPOSE_FILE).isFalse();
	}

}
