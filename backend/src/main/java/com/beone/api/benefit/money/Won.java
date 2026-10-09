package com.beone.api.benefit.money;

/**
 * Korean won as an integer amount. The sign is allowed so cancellations can be negative;
 * each model decides which signs it accepts. Never use {@code float} or {@code double} for money.
 */
public record Won(long value) implements Comparable<Won> {

	public static final Won ZERO = new Won(0);

	public static Won of(long value) {
		return new Won(value);
	}

	public boolean isPositive() {
		return value > 0;
	}

	public boolean isNegative() {
		return value < 0;
	}

	public boolean isZero() {
		return value == 0;
	}

	@Override
	public int compareTo(Won other) {
		return Long.compare(value, other.value);
	}

}
