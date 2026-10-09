package com.beone.api.benefit.input;

/**
 * Card selection mode set before payment (PRD §4.1).
 */
public enum SelectionMode {

	/** The engine selects the card with the largest monthly benefit. */
	AUTO_RECOMMEND,

	/** The representative card is always used; the engine still shows the difference. */
	FIXED_REPRESENTATIVE

}
