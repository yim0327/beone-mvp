package com.beone.api.benefit.card;

/**
 * Internal card product identifier such as {@code 09271}. Kept as a string so leading zeros
 * survive and ordering is by string, never by number (D-12).
 */
public record CardId(String value) implements Comparable<CardId> {

	public CardId {
		Identifiers.requireToken(value, "card id");
	}

	public static CardId of(String value) {
		return new CardId(value);
	}

	@Override
	public int compareTo(CardId other) {
		return value.compareTo(other.value);
	}

	@Override
	public String toString() {
		return value;
	}

}
