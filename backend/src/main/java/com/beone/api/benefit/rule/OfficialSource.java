package com.beone.api.benefit.rule;

import java.util.Objects;
import java.util.Optional;

/**
 * Official document a rule is based on, with an optional page or section locator.
 */
public record OfficialSource(String uri, Optional<String> locator) {

	public OfficialSource {
		if (uri == null || uri.isBlank()) {
			throw new IllegalArgumentException("source uri is required");
		}
		Objects.requireNonNull(locator, "locator");
		if (locator.isPresent() && locator.get().isBlank()) {
			throw new IllegalArgumentException("locator must not be blank when present");
		}
	}

	public static OfficialSource of(String uri, String locator) {
		return new OfficialSource(uri, Optional.of(locator));
	}

}
