package com.beone.api.benefit.rule;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.beone.api.benefit.card.ServiceId;
import com.beone.api.benefit.money.Won;

/**
 * New-card grace: during the span, the listed services treat prior-month performance as at least
 * {@code deemedPerformance} (D-03).
 */
public record NewCardGrace(GraceSpan span, Optional<Integer> days, Won deemedPerformance, Set<ServiceId> services) {

	public NewCardGrace {
		Objects.requireNonNull(span, "span");
		Objects.requireNonNull(days, "days");
		Objects.requireNonNull(deemedPerformance, "deemedPerformance");
		Objects.requireNonNull(services, "services");
		if (span == GraceSpan.DAYS_FROM_REGISTRATION) {
			if (days.isEmpty() || days.get() <= 0) {
				throw new IllegalArgumentException("a day-based grace needs a positive number of days");
			}
		}
		else if (days.isPresent()) {
			throw new IllegalArgumentException("days apply only to a day-based grace");
		}
		if (!deemedPerformance.isPositive()) {
			throw new IllegalArgumentException("deemed performance must be positive");
		}
		if (services.isEmpty()) {
			throw new IllegalArgumentException("grace must list the services it applies to");
		}
		services = Set.copyOf(services);
	}

}
