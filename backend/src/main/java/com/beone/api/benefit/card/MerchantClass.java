package com.beone.api.benefit.card;

/**
 * Confirmed merchant or business-category classification such as {@code STARBUCKS_STORE}. It is
 * an input the user or a scenario confirmed; it is never inferred from a similar merchant name.
 */
public record MerchantClass(String code) {

	public MerchantClass {
		Identifiers.requireToken(code, "merchant class");
	}

	public static MerchantClass of(String code) {
		return new MerchantClass(code);
	}

	@Override
	public String toString() {
		return code;
	}

}
