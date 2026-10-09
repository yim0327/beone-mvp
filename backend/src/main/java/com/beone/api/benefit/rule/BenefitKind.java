package com.beone.api.benefit.rule;

/**
 * How a monetary benefit is delivered. Shown separately to the user (D-11).
 */
public enum BenefitKind {

	/** Point accrual valued at 1 point = 1 won (D-11). */
	POINT_ACCRUAL,

	/** Billing or refund discount. */
	BILLING_DISCOUNT

}
