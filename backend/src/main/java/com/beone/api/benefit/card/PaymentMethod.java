package com.beone.api.benefit.card;

/**
 * Confirmed payment method of an order, planned spending or ledger transaction.
 */
public enum PaymentMethod {

	/** Paid directly with the card. */
	DIRECT,

	/** Paid through a simple-pay service the card terms list as eligible (09174 Check). */
	ELIGIBLE_SIMPLE_PAY,

	/**
	 * Automatic transfer registered to this card. The evaluation fixture uses it as a simplified
	 * input for the autopay registration that D-10 treats as a user-confirmed state.
	 */
	AUTOPAY

}
