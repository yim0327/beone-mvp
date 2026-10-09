package com.beone.api.benefit.rule;

/**
 * Verification state of a rule version. Only {@link #VERIFIED} may be used in production
 * calculation (PRD FR-01).
 */
public enum VerificationStatus {

	/** Human-approved, versioned rule based on the official terms (PRD FR-01). */
	VERIFIED,

	/** Rule draft extracted by AI and not yet approved by a person (PRD FR-01, FR-08). */
	AI_DRAFT,

	/** Rule read from official material but not verified (evaluation fixture defaults). */
	CANDIDATE,

	/** Synthetic rule snapshot used only to test rule selection (evaluation fixture C02). */
	SYNTHETIC_TEST_SNAPSHOT

}
