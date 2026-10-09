package com.beone.api.benefit.rule;

import java.util.Objects;

/**
 * A rule term that is either resolved from verified sources or still {@code UNRESOLVED}.
 *
 * <p>
 * An unresolved term means {@code 확인 필요}. It has no default and no fallback to 0 won; code
 * that needs the value must treat the affected benefit as needing confirmation (D-03, D-05).
 */
public sealed interface RuleTerm<T> {

	static <T> Resolved<T> resolved(T value) {
		return new Resolved<>(value);
	}

	static <T> Unresolved<T> unresolved(String reason) {
		return new Unresolved<>(reason);
	}

	boolean isResolved();

	/**
	 * Returns the resolved value.
	 * @throws IllegalStateException if the term is unresolved
	 */
	T resolvedValue();

	record Resolved<T>(T value) implements RuleTerm<T> {

		public Resolved {
			Objects.requireNonNull(value, "value");
		}

		@Override
		public boolean isResolved() {
			return true;
		}

		@Override
		public T resolvedValue() {
			return value;
		}

	}

	record Unresolved<T>(String reason) implements RuleTerm<T> {

		public Unresolved {
			if (reason == null || reason.isBlank()) {
				throw new IllegalArgumentException("reason is required for an unresolved term");
			}
		}

		@Override
		public boolean isResolved() {
			return false;
		}

		@Override
		public T resolvedValue() {
			throw new IllegalStateException("rule term is unresolved: " + reason);
		}

	}

}
