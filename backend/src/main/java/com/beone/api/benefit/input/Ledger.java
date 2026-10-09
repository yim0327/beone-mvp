package com.beone.api.benefit.input;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.rule.TriggerEvidence;
import com.beone.api.benefit.state.BasisPeriod;

/**
 * Transactions of one ledger source with the periods known to be complete. Transactions of cards
 * that are not held are allowed and ignored by comparison (evaluation casebook M10).
 */
public record Ledger(LedgerSource source, List<LedgerTransaction> transactions, List<LedgerCoverage> completePeriods) {

	public Ledger {
		Objects.requireNonNull(source, "source");
		transactions = List.copyOf(transactions);
		completePeriods = List.copyOf(completePeriods);
	}

	/**
	 * True when every day of {@code period} is inside some declared complete period of the card.
	 * Adjacent declarations combine; undeclared days make the period incomplete.
	 */
	public boolean isComplete(CardId cardId, BasisPeriod period) {
		for (LocalDate day = period.from(); !day.isAfter(period.to()); day = day.plusDays(1)) {
			LocalDate current = day;
			boolean covered = completePeriods.stream()
				.anyMatch(coverage -> coverage.cardId().equals(cardId) && coverage.period().contains(current));
			if (!covered) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Evidence for {@link com.beone.api.benefit.rule.TermTrigger#CANCELLATION_AFFECTING_PERFORMANCE_WINDOW}
	 * when the cancellation attribution is unresolved.
	 *
	 * <ol>
	 * <li>{@code PRESENT} if a cancellation of the card was received in the window or cancels a
	 * transaction dated in it.</li>
	 * <li>{@code UNDETERMINED} unless the ledger is complete from the window start to
	 * {@code asOf}, because a cancellation received later could still belong to the window.</li>
	 * <li>{@code UNDETERMINED} if a cancellation received after the window has no original date.</li>
	 * <li>{@code CONFIRMED_ABSENT} otherwise.</li>
	 * </ol>
	 *
	 * @param asOf last date whose cancellations can affect the result, not before the window end
	 */
	public TriggerEvidence cancellationEvidence(CardId cardId, BasisPeriod window, LocalDate asOf) {
		Objects.requireNonNull(cardId, "cardId");
		Objects.requireNonNull(window, "window");
		Objects.requireNonNull(asOf, "asOf");
		if (asOf.isBefore(window.to())) {
			throw new IllegalArgumentException("asOf " + asOf + " is before the window end " + window.to());
		}
		List<LedgerTransaction> cancellations = transactions.stream()
			.filter(tx -> tx.cardId().equals(cardId) && tx.type() == LedgerEntryType.CANCELLATION)
			.toList();
		boolean touchesWindow = cancellations.stream()
			.anyMatch(tx -> window.contains(tx.date()) || tx.originalDate().map(window::contains).orElse(false));
		if (touchesWindow) {
			return TriggerEvidence.PRESENT;
		}
		if (!isComplete(cardId, BasisPeriod.of(window.from(), asOf))) {
			return TriggerEvidence.UNDETERMINED;
		}
		boolean unknownOriginAfterWindow = cancellations.stream()
			.anyMatch(tx -> tx.date().isAfter(window.to()) && !tx.date().isAfter(asOf) && tx.originalDate().isEmpty());
		return unknownOriginAfterWindow ? TriggerEvidence.UNDETERMINED : TriggerEvidence.CONFIRMED_ABSENT;
	}

}
