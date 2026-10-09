package com.beone.api.benefit.input;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.beone.api.benefit.card.CardId;

/**
 * Everything one recommendation is computed from.
 *
 * @param referenceDate the clock date the state is judged at; the order may be later
 */
public record RecommendationInput(LocalDate referenceDate, Order order, List<HeldCard> cards, Ledger ledger,
		List<CardMonthlyState> states, PlannedSpendings plannedSpendings, SelectionPreference preference) {

	public RecommendationInput {
		Objects.requireNonNull(referenceDate, "referenceDate");
		Objects.requireNonNull(order, "order");
		cards = List.copyOf(cards);
		Objects.requireNonNull(ledger, "ledger");
		states = List.copyOf(states);
		Objects.requireNonNull(plannedSpendings, "plannedSpendings");
		Objects.requireNonNull(preference, "preference");
		if (cards.isEmpty()) {
			throw new IllegalArgumentException("at least one held card is required");
		}
		Set<CardId> held = new HashSet<>();
		for (HeldCard card : cards) {
			if (!held.add(card.cardId())) {
				throw new IllegalArgumentException("card " + card.cardId() + " is held twice");
			}
		}
		Set<CardId> withState = new HashSet<>();
		for (CardMonthlyState state : states) {
			if (!held.contains(state.cardId())) {
				throw new IllegalArgumentException("monthly state given for card " + state.cardId() + " that is not held");
			}
			if (!withState.add(state.cardId())) {
				throw new IllegalArgumentException("monthly state given twice for card " + state.cardId());
			}
		}
		preference.representativeCard().ifPresent(card -> {
			if (!held.contains(card)) {
				throw new IllegalArgumentException("representative card " + card + " is not held");
			}
		});
	}

}
