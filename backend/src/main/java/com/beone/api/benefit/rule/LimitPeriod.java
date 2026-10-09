package com.beone.api.benefit.rule;

/**
 * Period a monthly benefit limit applies to. Other periods stay unresolved (PRD FR-02, D-03).
 */
public enum LimitPeriod {

	/** The 1st to the last day of the month, without carry-over. */
	CALENDAR_MONTH

}
