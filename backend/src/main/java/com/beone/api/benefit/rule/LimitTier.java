package com.beone.api.benefit.rule;

import java.util.Objects;

import com.beone.api.benefit.money.Won;

/**
 * Monthly cap that applies when prior-month performance is at least {@code minimumPerformance}.
 */
public record LimitTier(Won minimumPerformance, Won monthlyCap) {

	public LimitTier {
		Objects.requireNonNull(minimumPerformance, "minimumPerformance");
		Objects.requireNonNull(monthlyCap, "monthlyCap");
		if (minimumPerformance.isNegative()) {
			throw new IllegalArgumentException("minimum performance must not be negative");
		}
		if (!monthlyCap.isPositive()) {
			throw new IllegalArgumentException("monthly cap must be positive: " + monthlyCap.value());
		}
	}

}
