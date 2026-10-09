package com.beone.api.benefit.rule;

import java.util.Objects;

import com.beone.api.benefit.card.CardId;

/**
 * Points to one rule version of one card, for results and inferred state.
 */
public record RuleVersionRef(CardId cardId, String ruleVersion) {

	public RuleVersionRef {
		Objects.requireNonNull(cardId, "cardId");
		if (ruleVersion == null || ruleVersion.isBlank()) {
			throw new IllegalArgumentException("rule version is required");
		}
	}

}
