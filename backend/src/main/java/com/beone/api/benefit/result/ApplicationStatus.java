package com.beone.api.benefit.result;

import com.beone.api.benefit.money.BenefitAmount;

/**
 * Why a benefit applied or did not apply, for a card or one of its services. Codes match the
 * evaluation fixture; {@code NO_VERIFIED_RULE} is named in the casebook and
 * {@code NEEDS_CONFIRMATION} is the {@code 확인 필요} state of D-05.
 */
public enum ApplicationStatus {

	/** A benefit above 0 won applies. */
	APPLIED(AmountShape.POSITIVE),

	/** A benefit above 0 won applies but the formula amount was cut by the remaining limit. */
	APPLIED_CAP_REMAINING(AmountShape.POSITIVE),

	/** The applicable limit is already used up. */
	LIMIT_EXHAUSTED(AmountShape.ZERO),

	/** The transaction is excluded by the terms. */
	NOT_APPLICABLE_EXCLUDED(AmountShape.ZERO),

	/** The merchant class is not a target. */
	NOT_APPLICABLE_CATEGORY(AmountShape.ZERO),

	/** Prior-month performance is below the threshold. */
	NOT_APPLICABLE_PERFORMANCE(AmountShape.ZERO),

	/** The amount is below the per-transaction minimum. */
	NOT_APPLICABLE_MIN_AMOUNT(AmountShape.ZERO),

	/** The payment method does not qualify. */
	NOT_APPLICABLE_PAYMENT_METHOD(AmountShape.ZERO),

	/** No rule version covers the decision date. */
	RULE_EXPIRED(AmountShape.UNCONFIRMED),

	/** A covering rule exists but none is selectable in the context, or the card has no rule. */
	NO_VERIFIED_RULE(AmountShape.UNCONFIRMED),

	/** A required state or rule term is unresolved (D-05). */
	NEEDS_CONFIRMATION(AmountShape.UNCONFIRMED);

	private final AmountShape amountShape;

	ApplicationStatus(AmountShape amountShape) {
		this.amountShape = amountShape;
	}

	/**
	 * Whether the amount agrees with this status: applied statuses need a confirmed amount above
	 * 0, not-applicable statuses a confirmed 0, and rule or state gaps an unconfirmed amount.
	 */
	public boolean isConsistentWith(BenefitAmount amount) {
		return switch (amountShape) {
			case POSITIVE -> amount.isConfirmed() && amount.confirmedWon().isPositive();
			case ZERO -> amount.isConfirmed() && amount.confirmedWon().isZero();
			case UNCONFIRMED -> !amount.isConfirmed();
		};
	}

	public boolean isApplied() {
		return amountShape == AmountShape.POSITIVE;
	}

	private enum AmountShape {

		POSITIVE, ZERO, UNCONFIRMED

	}

}
