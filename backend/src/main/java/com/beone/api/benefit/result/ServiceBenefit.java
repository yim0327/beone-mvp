package com.beone.api.benefit.result;

import java.util.Objects;

import com.beone.api.benefit.card.ServiceId;
import com.beone.api.benefit.money.BenefitAmount;

/**
 * Benefit of one service of a card on the current order.
 */
public record ServiceBenefit(ServiceId serviceId, ApplicationStatus status, BenefitAmount amount) {

	public ServiceBenefit {
		Objects.requireNonNull(serviceId, "serviceId");
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(amount, "amount");
		if (!status.isConsistentWith(amount)) {
			throw new IllegalArgumentException("service " + serviceId + " status " + status + " does not match amount " + amount);
		}
	}

}
