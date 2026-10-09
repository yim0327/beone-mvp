package com.beone.api.benefit.rule;

import java.util.List;
import java.util.function.Function;

import com.beone.api.benefit.money.Won;

final class Tiers {

	private Tiers() {
	}

	static <T> List<T> requireStrictlyAscending(List<T> tiers, Function<T, Won> minimum, String name) {
		if (tiers == null || tiers.isEmpty()) {
			throw new IllegalArgumentException(name + " needs at least one tier");
		}
		List<T> copy = List.copyOf(tiers);
		for (int i = 1; i < copy.size(); i++) {
			if (minimum.apply(copy.get(i)).compareTo(minimum.apply(copy.get(i - 1))) <= 0) {
				throw new IllegalArgumentException(name + " tiers must have strictly ascending minimum performance");
			}
		}
		return copy;
	}

}
