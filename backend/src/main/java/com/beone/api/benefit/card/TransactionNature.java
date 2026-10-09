package com.beone.api.benefit.card;

/**
 * Nature of a ledger transaction for prior-month performance, kept separate from its approval or
 * cancellation state (evaluation casebook: {@code ORDINARY}, {@code TAX}).
 */
public enum TransactionNature {

	/** Recognized purchase that did not receive a discount. */
	ORDINARY,

	/** Tax or public charge, excluded from performance by the candidate rules. */
	TAX

}
