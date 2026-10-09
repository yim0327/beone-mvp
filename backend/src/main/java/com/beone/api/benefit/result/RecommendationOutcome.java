package com.beone.api.benefit.result;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.money.Won;

/**
 * Whether a card was selected and on what basis.
 */
public sealed interface RecommendationOutcome {

	/**
	 * A card was selected.
	 *
	 * @param basis D-12 step that decided; empty when only one card was compared
	 * @param tiedCards cards tied on every benefit step; listed only when an arbitrary step
	 * decided (D-12)
	 * @param marginOverRunnerUp monthly benefit difference to the next card, when confirmed
	 * @param conditional true when unconfirmed state could change the selection, so it must not be
	 * shown as a confirmed optimum (PRD FR-05, D-03). Deciding whether it could change is the
	 * calculation's job; the model only carries the flag.
	 */
	record Selected(CardId card, Optional<SelectionBasis> basis, List<CardId> tiedCards,
			Optional<Won> marginOverRunnerUp, boolean conditional) implements RecommendationOutcome {

		public Selected {
			Objects.requireNonNull(card, "card");
			Objects.requireNonNull(basis, "basis");
			tiedCards = List.copyOf(tiedCards);
			Objects.requireNonNull(marginOverRunnerUp, "marginOverRunnerUp");
			boolean arbitrary = basis.map(SelectionBasis::isArbitrary).orElse(false);
			if (arbitrary != !tiedCards.isEmpty()) {
				throw new IllegalArgumentException("tied cards are listed exactly when an arbitrary basis decided");
			}
			if (!tiedCards.isEmpty()) {
				if (tiedCards.size() < 2 || Set.copyOf(tiedCards).size() != tiedCards.size()) {
					throw new IllegalArgumentException("a tie needs at least two distinct cards");
				}
				if (!tiedCards.contains(card)) {
					throw new IllegalArgumentException("the selected card must be one of the tied cards");
				}
			}
			marginOverRunnerUp.ifPresent(margin -> {
				if (margin.isNegative()) {
					throw new IllegalArgumentException("margin over the runner-up must not be negative");
				}
			});
		}

	}

	/**
	 * No card can be recommended, e.g. because no usable rule exists (evaluation C02).
	 */
	record NotDetermined(String reason) implements RecommendationOutcome {

		public NotDetermined {
			if (reason == null || reason.isBlank()) {
				throw new IllegalArgumentException("reason is required");
			}
		}

	}

}
