package com.beone.api.benefit.input;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.card.MerchantClass;
import com.beone.api.benefit.card.PaymentMethod;
import com.beone.api.benefit.card.TransactionNature;
import com.beone.api.benefit.money.Won;

/**
 * One ledger transaction (PRD FR-02). Values the source did not provide stay empty and are never
 * filled with defaults.
 *
 * @param date approval date, or the receipt date of a cancellation
 * @param originalDate date of the cancelled original, only for a cancellation
 */
public record LedgerTransaction(CardId cardId, LocalDate date, Optional<LocalTime> time, Won amount,
		LedgerEntryType type, Optional<TransactionNature> nature, Optional<MerchantClass> merchantClass,
		Optional<PaymentMethod> paymentMethod, Optional<LocalDate> originalDate) {

	public LedgerTransaction {
		Objects.requireNonNull(cardId, "cardId");
		Objects.requireNonNull(date, "date");
		Objects.requireNonNull(time, "time");
		Objects.requireNonNull(amount, "amount");
		Objects.requireNonNull(type, "type");
		Objects.requireNonNull(nature, "nature");
		Objects.requireNonNull(merchantClass, "merchantClass");
		Objects.requireNonNull(paymentMethod, "paymentMethod");
		Objects.requireNonNull(originalDate, "originalDate");
		switch (type) {
			case APPROVAL -> {
				if (!amount.isPositive()) {
					throw new IllegalArgumentException("approval amount must be positive: " + amount.value());
				}
				if (originalDate.isPresent()) {
					throw new IllegalArgumentException("an approval has no original date");
				}
			}
			case CANCELLATION -> {
				if (!amount.isNegative()) {
					throw new IllegalArgumentException("cancellation amount must be negative: " + amount.value());
				}
				if (originalDate.isPresent() && originalDate.get().isAfter(date)) {
					throw new IllegalArgumentException("cancellation is received before its original transaction");
				}
			}
		}
	}

}
