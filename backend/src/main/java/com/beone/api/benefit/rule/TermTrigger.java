package com.beone.api.benefit.rule;

/**
 * The input situation in which an unresolved conditional term changes the result. While that
 * situation cannot be ruled out, the term must be treated as needed.
 */
public enum TermTrigger {

	/**
	 * A cancellation that may be attributed to the performance window: received in it, cancelling
	 * a transaction in it, or with an unknown original date (cancellation attribution, D-03).
	 */
	CANCELLATION_AFFECTING_PERFORMANCE_WINDOW,

	/** A sale that received a discount within the performance window (performance exclusion). */
	BENEFITED_SALE_IN_PERFORMANCE_WINDOW,

	/** Two or more transactions consuming the same limit in one period (deduction order, D-03). */
	SHARED_LIMIT_CONSUMPTION,

	/** The card is within a new-card grace span (D-03). */
	WITHIN_NEW_CARD_GRACE,

	/** Two or more services qualify on the same transaction (stacking, PRD FR-05). */
	MULTIPLE_SERVICES_QUALIFY,

	/** A rate calculation yields an amount below 1 won (sub-won handling, D-03). */
	FRACTIONAL_WON_RESULT

}
