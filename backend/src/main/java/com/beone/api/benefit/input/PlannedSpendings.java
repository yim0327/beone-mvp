package com.beone.api.benefit.input;

import java.util.List;

/**
 * Planned spendings used in one optimization, at most {@value #MAX_COUNT} (PRD FR-04).
 */
public record PlannedSpendings(List<PlannedSpending> items) {

	public static final int MAX_COUNT = 5;

	public PlannedSpendings {
		items = List.copyOf(items);
		if (items.size() > MAX_COUNT) {
			throw new IllegalArgumentException(
					"at most " + MAX_COUNT + " planned spendings are allowed, got " + items.size());
		}
	}

	public static PlannedSpendings none() {
		return new PlannedSpendings(List.of());
	}

}
