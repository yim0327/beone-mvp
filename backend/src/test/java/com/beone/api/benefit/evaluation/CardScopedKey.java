package com.beone.api.benefit.evaluation;

final class CardScopedKey {

	private CardScopedKey() {
	}

	/**
	 * Splits {@code card:name} into exactly two non-empty parts.
	 */
	static String[] split(String key) {
		String[] parts = key.split(":", -1);
		if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
			throw new IllegalArgumentException("expected '<card>:<name>' but got '" + key + "'");
		}
		return parts;
	}

}
