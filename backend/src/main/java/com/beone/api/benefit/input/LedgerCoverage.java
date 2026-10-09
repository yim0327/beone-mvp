package com.beone.api.benefit.input;

import java.util.Objects;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.state.BasisPeriod;

/**
 * Declares that the ledger holds every transaction of a card within a period. Periods not
 * declared are not known to be complete, so values derived from them stay {@code UNKNOWN}
 * (D-04).
 */
public record LedgerCoverage(CardId cardId, BasisPeriod period) {

	public LedgerCoverage {
		Objects.requireNonNull(cardId, "cardId");
		Objects.requireNonNull(period, "period");
	}

}
