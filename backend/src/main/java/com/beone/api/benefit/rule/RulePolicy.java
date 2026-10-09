package com.beone.api.benefit.rule;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.beone.api.benefit.card.MerchantClass;
import com.beone.api.benefit.card.TransactionNature;

/**
 * Card-wide performance and limit terms (D-03). Every term without an official basis stays
 * {@link RuleTerm.Unresolved}.
 *
 * @param performanceWindow window for prior-month performance
 * @param performanceDateBasis transaction date that decides which window a transaction falls in
 * @param cancellationAttribution month a cancellation is deducted from performance
 * @param performanceExcludedNatures ledger natures excluded from performance
 * @param performanceExcludedClasses merchant classes excluded from performance
 * @param excludesBenefitedSales whether sales that received a discount are excluded from
 * performance
 * @param limitPeriod period of monthly limits
 * @param limitDeductionOrder order in which transactions consume limits
 * @param newCardGrace resolved empty when the terms state no grace
 */
public record RulePolicy(RuleTerm<PerformanceWindow> performanceWindow,
		RuleTerm<TransactionDateBasis> performanceDateBasis, RuleTerm<CancellationAttribution> cancellationAttribution,
		Set<TransactionNature> performanceExcludedNatures, Set<MerchantClass> performanceExcludedClasses,
		RuleTerm<Boolean> excludesBenefitedSales, RuleTerm<LimitPeriod> limitPeriod,
		RuleTerm<LimitDeductionOrder> limitDeductionOrder, RuleTerm<Optional<NewCardGrace>> newCardGrace) {

	public RulePolicy {
		Objects.requireNonNull(performanceWindow, "performanceWindow");
		Objects.requireNonNull(performanceDateBasis, "performanceDateBasis");
		Objects.requireNonNull(cancellationAttribution, "cancellationAttribution");
		performanceExcludedNatures = Set.copyOf(performanceExcludedNatures);
		performanceExcludedClasses = Set.copyOf(performanceExcludedClasses);
		Objects.requireNonNull(excludesBenefitedSales, "excludesBenefitedSales");
		Objects.requireNonNull(limitPeriod, "limitPeriod");
		Objects.requireNonNull(limitDeductionOrder, "limitDeductionOrder");
		Objects.requireNonNull(newCardGrace, "newCardGrace");
	}

	static void addIfUnresolved(List<ConditionalTerm> target, String name, RuleTerm<?> term, TermTrigger trigger) {
		if (term instanceof RuleTerm.Unresolved<?> unresolved) {
			target.add(new ConditionalTerm(name, unresolved.reason(), trigger));
		}
	}

}
