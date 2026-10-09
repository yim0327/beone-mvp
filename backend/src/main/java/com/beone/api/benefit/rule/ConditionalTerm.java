package com.beone.api.benefit.rule;

import java.util.Objects;

/**
 * An unresolved rule term that only changes the result when its trigger occurs.
 *
 * <p>
 * Contract: the term may be disregarded only when the evidence is
 * {@link TriggerEvidence#CONFIRMED_ABSENT}. With {@code PRESENT} or {@code UNDETERMINED} the
 * affected benefit stays {@code 확인 필요} (D-03, D-05). Missing evidence is
 * {@code UNDETERMINED}, never absence.
 *
 * @param name term name, prefixed with the service id for service terms (e.g.
 * {@code BASE.fractionalWon})
 * @param reason why the term is unresolved
 * @param trigger situation in which the term matters
 */
public record ConditionalTerm(String name, String reason, TermTrigger trigger) {

	public ConditionalTerm {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("term name is required");
		}
		if (reason == null || reason.isBlank()) {
			throw new IllegalArgumentException("reason is required");
		}
		Objects.requireNonNull(trigger, "trigger");
	}

	public boolean mayBeDisregarded(TriggerEvidence evidence) {
		Objects.requireNonNull(evidence, "evidence");
		return evidence == TriggerEvidence.CONFIRMED_ABSENT;
	}

}
