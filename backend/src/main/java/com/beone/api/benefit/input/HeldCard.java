package com.beone.api.benefit.input;

import java.time.LocalDate;
import java.util.Objects;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.state.StateSource;
import com.beone.api.benefit.state.StateValue;

/**
 * A card in the portfolio. {@code holdingSource} keeps virtual cards apart from real ones
 * (PRD §3, D-04).
 *
 * @param registeredAt card registration date, used for new-card grace
 */
public record HeldCard(CardId cardId, StateSource holdingSource, StateValue<LocalDate> registeredAt) {

	public HeldCard {
		Objects.requireNonNull(cardId, "cardId");
		Objects.requireNonNull(holdingSource, "holdingSource");
		if (holdingSource == StateSource.INFERRED || holdingSource == StateSource.UNKNOWN) {
			throw new IllegalArgumentException("card holding must come from CODEF, MANUAL or SYNTHETIC, not "
					+ holdingSource);
		}
		Objects.requireNonNull(registeredAt, "registeredAt");
	}

}
