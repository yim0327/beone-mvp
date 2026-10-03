package com.beone.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class MigrationNamingTest {

	private static final Path MIGRATIONS = Path.of("src", "main", "resources", "db", "migration");

	private static final Pattern VERSIONED = Pattern.compile("V(\\d+)__[a-z0-9_]+\\.sql");

	@Test
	void migrationsAreVersionedWithUniqueVersions() throws IOException {
		List<String> names;
		try (Stream<Path> files = Files.list(MIGRATIONS)) {
			names = files.map(path -> path.getFileName().toString()).sorted().toList();
		}

		assertThat(names).isNotEmpty().allMatch(name -> VERSIONED.matcher(name).matches());

		List<Integer> versions = names.stream().map(name -> {
			Matcher matcher = VERSIONED.matcher(name);
			matcher.matches();
			return Integer.parseInt(matcher.group(1));
		}).toList();
		assertThat(versions).doesNotHaveDuplicates();
	}

}
