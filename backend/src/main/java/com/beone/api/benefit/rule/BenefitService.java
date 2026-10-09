package com.beone.api.benefit.rule;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.beone.api.benefit.card.LimitBucketId;
import com.beone.api.benefit.card.MerchantClass;
import com.beone.api.benefit.card.PaymentMethod;
import com.beone.api.benefit.card.ServiceId;
import com.beone.api.benefit.money.Won;

/**
 * One benefit service of a card rule, such as the 09271 base accrual or the 09174 Great
 * discount.
 *
 * @param id service identifier within the rule
 * @param kind point accrual or billing discount (D-11)
 * @param target merchants the service targets
 * @param excludedClasses merchant classes excluded from the benefit
 * @param paymentMethods payment methods that qualify; listed explicitly
 * @param minimumAmount minimum amount per transaction; zero when the terms state none
 * @param maximumTargetAmount per-transaction amount above which the excess is not discounted
 * @param tiers calculation by prior-month performance, strictly ascending
 * @param limitBuckets limits the benefit consumes; empty when the terms state no limit
 * @param requiresSelection true for an opt-in service the user must have selected (09271)
 * @param stackableWith services this one may be combined with on the same transaction; resolved
 * empty means only the largest single benefit applies (PRD FR-05)
 * @param fractionalWon handling of amounts below 1 won
 */
public record BenefitService(ServiceId id, BenefitKind kind, MerchantTarget target, Set<MerchantClass> excludedClasses,
		Set<PaymentMethod> paymentMethods, Won minimumAmount, Optional<Won> maximumTargetAmount,
		List<ServiceTier> tiers, List<LimitBucketId> limitBuckets, boolean requiresSelection,
		RuleTerm<Set<ServiceId>> stackableWith, RuleTerm<FractionalWonPolicy> fractionalWon) {

	public BenefitService {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(kind, "kind");
		Objects.requireNonNull(target, "target");
		excludedClasses = Set.copyOf(excludedClasses);
		paymentMethods = Set.copyOf(paymentMethods);
		if (paymentMethods.isEmpty()) {
			throw new IllegalArgumentException("service " + id + " must list at least one payment method");
		}
		Objects.requireNonNull(minimumAmount, "minimumAmount");
		if (minimumAmount.isNegative()) {
			throw new IllegalArgumentException("service " + id + " minimum amount must not be negative");
		}
		Objects.requireNonNull(maximumTargetAmount, "maximumTargetAmount");
		if (maximumTargetAmount.isPresent() && maximumTargetAmount.get().compareTo(minimumAmount) < 0) {
			throw new IllegalArgumentException("service " + id + " maximum target amount is below its minimum");
		}
		if (maximumTargetAmount.isPresent() && !maximumTargetAmount.get().isPositive()) {
			throw new IllegalArgumentException("service " + id + " maximum target amount must be positive");
		}
		tiers = Tiers.requireStrictlyAscending(tiers, ServiceTier::minimumPerformance, "service " + id);
		limitBuckets = List.copyOf(limitBuckets);
		if (Set.copyOf(limitBuckets).size() != limitBuckets.size()) {
			throw new IllegalArgumentException("service " + id + " lists a limit bucket twice");
		}
		Objects.requireNonNull(stackableWith, "stackableWith");
		if (stackableWith.isResolved() && stackableWith.resolvedValue().contains(id)) {
			throw new IllegalArgumentException("service " + id + " cannot stack with itself");
		}
		Objects.requireNonNull(fractionalWon, "fractionalWon");
	}

	/**
	 * True when the calculation depends on prior-month performance: more than one tier, or a
	 * single tier above 0 won.
	 */
	public boolean hasPerformanceRequirement() {
		return tiers.size() > 1 || tiers.get(0).minimumPerformance().isPositive();
	}

	/**
	 * Reasons the calculation itself is unresolved. A service with any such reason is never
	 * calculable.
	 */
	public List<String> unresolvedCalculationReasons() {
		return tiers.stream()
			.map(ServiceTier::calculation)
			.filter(term -> !term.isResolved())
			.map(term -> ((RuleTerm.Unresolved<?>) term).reason())
			.toList();
	}

	/**
	 * Unresolved terms that only matter for some inputs (stacking, sub-won handling).
	 */
	public List<ConditionalTerm> unresolvedConditionalTerms() {
		List<ConditionalTerm> unresolved = new ArrayList<>();
		RulePolicy.addIfUnresolved(unresolved, id + ".stackableWith", stackableWith,
				TermTrigger.MULTIPLE_SERVICES_QUALIFY);
		RulePolicy.addIfUnresolved(unresolved, id + ".fractionalWon", fractionalWon, TermTrigger.FRACTIONAL_WON_RESULT);
		return List.copyOf(unresolved);
	}

}
