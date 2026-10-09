package com.beone.api.benefit.input;

import java.time.LocalDate;
import java.util.Objects;

import com.beone.api.benefit.card.ServiceId;

/**
 * An opt-in service the user selected for a card, effective from a date (09271 FUEL/STORE).
 */
public record ServiceSelection(ServiceId service, LocalDate effectiveFrom) {

	public ServiceSelection {
		Objects.requireNonNull(service, "service");
		Objects.requireNonNull(effectiveFrom, "effectiveFrom");
	}

}
