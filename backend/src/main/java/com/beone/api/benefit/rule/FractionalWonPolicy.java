package com.beone.api.benefit.rule;

/**
 * Handling of a calculated benefit below 1 won. Not confirmed for any card yet, so rules keep it
 * as an unresolved term until an official basis is found (card research, D-03).
 */
public enum FractionalWonPolicy {

	/** Drop the fraction (절사). */
	TRUNCATE,

	/** Round half up (반올림). */
	ROUND_HALF_UP

}
