package com.beone.api.benefit.result;

/**
 * The D-12 step that decided the selected card.
 */
public enum SelectionBasis {

	/** Step 1: largest total expected monthly benefit. */
	MONTHLY_TOTAL(false),

	/** Step 2: largest benefit on the current order. */
	CURRENT_ORDER_BENEFIT(false),

	/** Step 3: the user's representative card among the tied cards. Arbitrary rule. */
	REPRESENTATIVE_CARD(true),

	/** Step 4: card ID string ascending. Arbitrary rule. */
	CARD_ID_ASC(true);

	private final boolean arbitrary;

	SelectionBasis(boolean arbitrary) {
		this.arbitrary = arbitrary;
	}

	/**
	 * True for steps that do not mean the selected card is better (D-12).
	 */
	public boolean isArbitrary() {
		return arbitrary;
	}

}
