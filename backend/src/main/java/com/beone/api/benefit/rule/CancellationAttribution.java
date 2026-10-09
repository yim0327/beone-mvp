package com.beone.api.benefit.rule;

/**
 * Month a cancellation is deducted from performance. Only the attribution stated in the
 * candidate terms is listed; anything else stays an unresolved term (D-03).
 */
public enum CancellationAttribution {

	/** Deducted in the month the cancellation slip is received (09271, 01664). */
	RECEIPT_MONTH

}
