package com.beone.api.benefit.rule;

/**
 * Order in which transactions consume a monthly limit (D-03). Purchase order is never replaced
 * by approval order when it is unknown.
 */
public enum LimitDeductionOrder {

	/** In order of approval. */
	APPROVAL_ORDER,

	/** In order of purchase slips (01914). */
	PURCHASE_ORDER

}
