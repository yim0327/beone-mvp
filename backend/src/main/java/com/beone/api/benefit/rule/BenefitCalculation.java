package com.beone.api.benefit.rule;

import java.util.Objects;

import com.beone.api.benefit.money.Won;

/**
 * Calculation method of one benefit tier: a rate in basis points or a fixed amount.
 */
public sealed interface BenefitCalculation {

	/** 100% expressed in basis points. */
	int FULL_RATE_BASIS_POINTS = 10_000;

	static Rate rate(int basisPoints) {
		return new Rate(basisPoints);
	}

	static FixedAmount fixed(long won) {
		return new FixedAmount(Won.of(won));
	}

	/**
	 * Rate in basis points, e.g. 1% = 100 and 50% = 5,000. Integer only so no floating point is
	 * involved.
	 */
	record Rate(int basisPoints) implements BenefitCalculation {

		public Rate {
			if (basisPoints <= 0 || basisPoints > FULL_RATE_BASIS_POINTS) {
				throw new IllegalArgumentException("rate must be within 1..10000 basis points: " + basisPoints);
			}
		}

	}

	/** Fixed amount per qualifying transaction, e.g. 2,500 won (D-10). */
	record FixedAmount(Won amount) implements BenefitCalculation {

		public FixedAmount {
			Objects.requireNonNull(amount, "amount");
			if (!amount.isPositive()) {
				throw new IllegalArgumentException("fixed benefit must be positive: " + amount.value());
			}
		}

	}

}
