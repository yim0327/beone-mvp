package com.beone.api.benefit.input;

/**
 * Which ledger a transaction belongs to. CODEF approvals and the service's own simulated ledger
 * are kept apart (PRD FR-02, FR-07).
 */
public enum LedgerSource {

	/** The service's own virtual or mock-approval ledger. */
	SIMULATION,

	/** Approvals actually returned by CODEF. */
	CODEF

}
