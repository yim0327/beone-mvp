package com.beone.api.benefit.rule;

/**
 * Window used to compute prior-month performance. Only the window the four candidate cards
 * state is supported; any other window stays an unresolved term (PRD FR-02, D-03).
 */
public enum PerformanceWindow {

	/** The previous calendar month, 1st to last day. */
	PREVIOUS_CALENDAR_MONTH

}
