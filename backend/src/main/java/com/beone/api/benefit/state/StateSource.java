package com.beone.api.benefit.state;

/**
 * Where a state value came from (PRD FR-03, D-04).
 */
public enum StateSource {

	/** A value CODEF actually returned, normalized without reinterpretation. */
	CODEF,

	/** A value the user entered or confirmed. */
	MANUAL,

	/** Recomputed from a VERIFIED rule and a complete, ordered ledger. */
	INFERRED,

	/** Explicit virtual-portfolio or evaluation data, never shown as a real observation. */
	SYNTHETIC,

	/** Missing, conflicting, out of period or stale. Never replaced by 0 won. */
	UNKNOWN

}
