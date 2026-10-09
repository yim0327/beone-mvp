package com.beone.api.benefit.card;

final class Identifiers {

	private Identifiers() {
	}

	static void requireToken(String value, String name) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " is required");
		}
		if (!value.equals(value.strip()) || value.chars().anyMatch(Character::isWhitespace)) {
			throw new IllegalArgumentException(name + " must not contain whitespace: '" + value + "'");
		}
	}

}
