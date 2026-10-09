package com.beone.api.benefit.state;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Inclusive date range a value refers to. Dates are Asia/Seoul calendar dates.
 */
public record BasisPeriod(LocalDate from, LocalDate to) {

	public BasisPeriod {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");
		if (to.isBefore(from)) {
			throw new IllegalArgumentException("basis period ends before it starts: " + from + " ~ " + to);
		}
	}

	public static BasisPeriod of(LocalDate from, LocalDate to) {
		return new BasisPeriod(from, to);
	}

	public static BasisPeriod day(LocalDate date) {
		return new BasisPeriod(date, date);
	}

	public static BasisPeriod month(YearMonth month) {
		return new BasisPeriod(month.atDay(1), month.atEndOfMonth());
	}

	public boolean contains(LocalDate date) {
		return !date.isBefore(from) && !date.isAfter(to);
	}

}
