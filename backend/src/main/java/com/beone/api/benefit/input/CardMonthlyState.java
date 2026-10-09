package com.beone.api.benefit.input;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.card.LimitBucketId;
import com.beone.api.benefit.money.Won;
import com.beone.api.benefit.state.StateValue;

/**
 * Monthly state of one held card. Every value carries its source (D-04).
 *
 * @param priorPerformance prior-month performance when it was supplied directly; empty means it
 * must be derived from the ledger
 * @param usedBenefits benefit already used this month, by limit bucket
 * @param otherBucketsUsed usage of buckets not listed in {@code usedBenefits}; {@code UNKNOWN}
 * when not supplied, never 0 won
 * @param serviceSelections opt-in service selections with their effective dates
 */
public record CardMonthlyState(CardId cardId, Optional<StateValue<Won>> priorPerformance,
		Map<LimitBucketId, StateValue<Won>> usedBenefits, StateValue<Won> otherBucketsUsed,
		List<StateValue<ServiceSelection>> serviceSelections) {

	public CardMonthlyState {
		Objects.requireNonNull(cardId, "cardId");
		Objects.requireNonNull(priorPerformance, "priorPerformance");
		usedBenefits = Map.copyOf(usedBenefits);
		usedBenefits.forEach((bucket, used) -> requireNotNegative(used, "used benefit of " + bucket));
		Objects.requireNonNull(otherBucketsUsed, "otherBucketsUsed");
		requireNotNegative(otherBucketsUsed, "used benefit of other buckets");
		serviceSelections = List.copyOf(serviceSelections);
	}

	private static void requireNotNegative(StateValue<Won> value, String name) {
		if (value.isKnown() && value.knownValue().isNegative()) {
			throw new IllegalArgumentException(name + " must not be negative");
		}
	}

}
