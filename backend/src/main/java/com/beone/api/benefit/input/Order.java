package com.beone.api.benefit.input;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;

import com.beone.api.benefit.card.MerchantClass;
import com.beone.api.benefit.card.PaymentMethod;
import com.beone.api.benefit.money.Won;

/**
 * The editable order being paid (PRD FR-04). The date is an Asia/Seoul calendar date and decides
 * benefit periods and month boundaries; the time is kept only when it was given.
 */
public record Order(Optional<String> orderId, Optional<String> merchantName, Won amount, LocalDate date,
		Optional<LocalTime> time, MerchantClass merchantClass, PaymentMethod paymentMethod) {

	public Order {
		InputChecks.requireOptionalText(orderId, "order id");
		InputChecks.requireOptionalText(merchantName, "merchant name");
		InputChecks.requirePositive(amount, "order amount");
		Objects.requireNonNull(date, "date");
		Objects.requireNonNull(time, "time");
		Objects.requireNonNull(merchantClass, "merchantClass");
		Objects.requireNonNull(paymentMethod, "paymentMethod");
	}

}
