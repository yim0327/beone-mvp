package com.beone.api.benefit.rule;

import java.util.Objects;
import java.util.Set;

import com.beone.api.benefit.card.MerchantClass;

/**
 * Merchants a benefit service targets before exclusions are applied.
 */
public sealed interface MerchantTarget {

	static AllMerchants allMerchants() {
		return new AllMerchants();
	}

	static SpecificClasses only(Set<MerchantClass> classes) {
		return new SpecificClasses(classes);
	}

	/** Every merchant, such as the 09271 base accrual. */
	record AllMerchants() implements MerchantTarget {
	}

	/** Only the listed merchant classes. */
	record SpecificClasses(Set<MerchantClass> classes) implements MerchantTarget {

		public SpecificClasses {
			Objects.requireNonNull(classes, "classes");
			if (classes.isEmpty()) {
				throw new IllegalArgumentException("a specific merchant target needs at least one class");
			}
			classes = Set.copyOf(classes);
		}

	}

}
