package com.beone.api.benefit.input;

import java.util.Objects;
import java.util.Optional;

import com.beone.api.benefit.money.Won;

final class InputChecks {

	private InputChecks() {
	}

	static void requirePositive(Won amount, String name) {
		Objects.requireNonNull(amount, name);
		if (!amount.isPositive()) {
			throw new IllegalArgumentException(name + " must be positive: " + amount.value());
		}
	}

	static void requireOptionalText(Optional<String> text, String name) {
		Objects.requireNonNull(text, name);
		if (text.isPresent() && text.get().isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank when present");
		}
	}

}
