package com.beone.api.benefit.card;

/**
 * Identifier of one benefit service within a card rule, such as {@code BASE}, {@code FUEL} or
 * {@code Great}. Compared exactly as written; case is not normalized.
 */
public record ServiceId(String value) {

	public ServiceId {
		Identifiers.requireToken(value, "service id");
	}

	public static ServiceId of(String value) {
		return new ServiceId(value);
	}

	@Override
	public String toString() {
		return value;
	}

}
