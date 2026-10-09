package com.beone.api.benefit.rule;

import java.util.Objects;

import com.beone.api.benefit.money.Won;

/**
 * Calculation that applies when prior-month performance is at least
 * {@code minimumPerformance}. A service without a performance requirement has a single tier
 * starting at 0 won.
 */
public record ServiceTier(Won minimumPerformance, RuleTerm<BenefitCalculation> calculation) {

	public ServiceTier {
		Objects.requireNonNull(minimumPerformance, "minimumPerformance");
		Objects.requireNonNull(calculation, "calculation");
		if (minimumPerformance.isNegative()) {
			throw new IllegalArgumentException("minimum performance must not be negative");
		}
	}

}
