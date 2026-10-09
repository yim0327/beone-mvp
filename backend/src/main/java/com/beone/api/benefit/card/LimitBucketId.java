package com.beone.api.benefit.card;

/**
 * Identifier of a monthly benefit limit within a card rule, such as {@code Great} or
 * {@code CU_AREA}. Compared exactly as written; case is not normalized.
 */
public record LimitBucketId(String value) {

	public LimitBucketId {
		Identifiers.requireToken(value, "limit bucket id");
	}

	public static LimitBucketId of(String value) {
		return new LimitBucketId(value);
	}

	@Override
	public String toString() {
		return value;
	}

}
