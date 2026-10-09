package com.beone.api.benefit.rule;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Inclusive period in which a rule version applies. An empty end means no end date is known
 * to have been announced.
 */
public record ValidityPeriod(LocalDate from, Optional<LocalDate> to) {

	public ValidityPeriod {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");
		if (to.isPresent() && to.get().isBefore(from)) {
			throw new IllegalArgumentException("validity period ends before it starts: " + from + " ~ " + to.get());
		}
	}

	public static ValidityPeriod between(LocalDate from, LocalDate to) {
		return new ValidityPeriod(from, Optional.of(to));
	}

	public static ValidityPeriod startingAt(LocalDate from) {
		return new ValidityPeriod(from, Optional.empty());
	}

	public boolean contains(LocalDate date) {
		return !date.isBefore(from) && to.map(end -> !date.isAfter(end)).orElse(true);
	}

	public boolean overlaps(ValidityPeriod other) {
		boolean thisStartsBeforeOtherEnds = other.to.map(end -> !from.isAfter(end)).orElse(true);
		boolean otherStartsBeforeThisEnds = to.map(end -> !other.from.isAfter(end)).orElse(true);
		return thisStartsBeforeOtherEnds && otherStartsBeforeThisEnds;
	}

}
