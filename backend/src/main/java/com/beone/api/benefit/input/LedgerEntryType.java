package com.beone.api.benefit.input;

/**
 * Approval or cancellation state of a ledger transaction.
 */
public enum LedgerEntryType {

	/** Approved transaction; amount is positive. */
	APPROVAL,

	/** Cancellation dated on the day it was received; amount is negative. */
	CANCELLATION

}
