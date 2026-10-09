package com.beone.api.benefit.rule;

/**
 * Which date of a transaction a rule counts by. Different bases are never merged just because
 * they fall in the same month (D-03).
 */
public enum TransactionDateBasis {

	/** Approval date. */
	APPROVAL,

	/** Usage date, such as the actual ride date. */
	USAGE,

	/** Date entered on the card statement. */
	STATEMENT_ENTRY,

	/** Purchase (acquiring) date. */
	PURCHASE

}
