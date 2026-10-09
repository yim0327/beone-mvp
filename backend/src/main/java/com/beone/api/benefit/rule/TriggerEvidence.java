package com.beone.api.benefit.rule;

/**
 * What the input and ledger show about a {@link TermTrigger}.
 */
public enum TriggerEvidence {

	/** The trigger occurs, so the term is needed. */
	PRESENT,

	/**
	 * Complete inputs show the trigger does not occur. This is the only evidence that lets an
	 * unresolved conditional term be disregarded.
	 */
	CONFIRMED_ABSENT,

	/**
	 * Inputs are incomplete or unknown, so the trigger cannot be ruled out. Treated like
	 * {@link #PRESENT}; this is also the evidence to use when nothing was checked.
	 */
	UNDETERMINED

}
