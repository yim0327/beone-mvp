package com.beone.api.benefit.rule;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.beone.api.benefit.card.LimitBucketId;

/**
 * A monthly benefit limit. A bucket with a parent also counts toward the parent, which models an
 * integrated limit over several area limits (limit group).
 */
public record LimitBucket(LimitBucketId id, List<LimitTier> tiers, Optional<LimitBucketId> parent) {

	public LimitBucket {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(parent, "parent");
		tiers = Tiers.requireStrictlyAscending(tiers, LimitTier::minimumPerformance, "limit bucket " + id);
		if (parent.isPresent() && parent.get().equals(id)) {
			throw new IllegalArgumentException("limit bucket " + id + " cannot be its own parent");
		}
	}

	/**
	 * True when the cap depends on prior-month performance: more than one tier, or a single tier
	 * above 0 won.
	 */
	public boolean dependsOnPerformance() {
		return tiers.size() > 1 || tiers.get(0).minimumPerformance().isPositive();
	}

}
