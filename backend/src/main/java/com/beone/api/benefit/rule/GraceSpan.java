package com.beone.api.benefit.rule;

/**
 * How long a new-card performance grace lasts (D-03).
 */
public enum GraceSpan {

	/** From registration until the last day of the following month (09271, 09174, 01914). */
	UNTIL_END_OF_NEXT_MONTH,

	/** A number of days from the first registration (01664: 60 days). */
	DAYS_FROM_REGISTRATION

}
