package com.beone.api.benefit.money;

import java.util.Objects;

/**
 * A benefit amount that is either a confirmed won value or {@code 확인 필요}.
 *
 * <p>
 * {@link NeedsConfirmation} is never equal to {@code Confirmed(0)} and there is deliberately
 * no accessor that falls back to zero (D-05).
 */
public sealed interface BenefitAmount {

	static Confirmed confirmed(long won) {
		return new Confirmed(Won.of(won));
	}

	static NeedsConfirmation needsConfirmation(String reason) {
		return new NeedsConfirmation(reason);
	}

	boolean isConfirmed();

	/**
	 * Returns the confirmed amount.
	 * @throws IllegalStateException if the amount still needs confirmation
	 */
	Won confirmedWon();

	record Confirmed(Won won) implements BenefitAmount {

		public Confirmed {
			Objects.requireNonNull(won, "won");
			if (won.isNegative()) {
				throw new IllegalArgumentException("benefit amount must not be negative: " + won.value());
			}
		}

		@Override
		public boolean isConfirmed() {
			return true;
		}

		@Override
		public Won confirmedWon() {
			return won;
		}

	}

	record NeedsConfirmation(String reason) implements BenefitAmount {

		public NeedsConfirmation {
			if (reason == null || reason.isBlank()) {
				throw new IllegalArgumentException("reason is required for an unconfirmed amount");
			}
		}

		@Override
		public boolean isConfirmed() {
			return false;
		}

		@Override
		public Won confirmedWon() {
			throw new IllegalStateException("amount needs confirmation: " + reason);
		}

	}

}
