package com.beone.api.benefit.rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.card.LimitBucketId;
import com.beone.api.benefit.card.ServiceId;

/**
 * One versioned rule of a card product (PRD FR-01).
 *
 * <p>
 * {@code evaluationOnly} marks a rule snapshot that exists only as evaluation data, such as the
 * fixture's C02 snapshot. It is never {@code VERIFIED}, so {@link RuleCatalog} never selects it.
 * Selecting or running evaluation-only rules belongs to a later evaluation harness.
 */
public record CardRuleVersion(CardId cardId, String ruleVersion, ValidityPeriod validity, VerificationStatus status,
		boolean evaluationOnly, List<OfficialSource> sources, RulePolicy policy, List<BenefitService> services,
		List<LimitBucket> limitBuckets) {

	public CardRuleVersion {
		Objects.requireNonNull(cardId, "cardId");
		if (ruleVersion == null || ruleVersion.isBlank()) {
			throw new IllegalArgumentException("rule version is required");
		}
		Objects.requireNonNull(validity, "validity");
		Objects.requireNonNull(status, "status");
		sources = List.copyOf(sources);
		Objects.requireNonNull(policy, "policy");
		services = List.copyOf(services);
		limitBuckets = List.copyOf(limitBuckets);
		if (status == VerificationStatus.VERIFIED) {
			if (evaluationOnly) {
				throw new IllegalArgumentException("a VERIFIED rule cannot be evaluation-only");
			}
			if (sources.isEmpty()) {
				throw new IllegalArgumentException("a VERIFIED rule needs an official source");
			}
			if (services.isEmpty()) {
				throw new IllegalArgumentException("a VERIFIED rule needs at least one benefit service");
			}
		}
		Map<ServiceId, BenefitService> servicesById = uniqueBy(services, BenefitService::id, "service");
		Map<LimitBucketId, LimitBucket> bucketsById = uniqueBy(limitBuckets, LimitBucket::id, "limit bucket");
		for (BenefitService service : services) {
			for (LimitBucketId bucket : service.limitBuckets()) {
				if (!bucketsById.containsKey(bucket)) {
					throw new IllegalArgumentException("service " + service.id() + " refers to unknown limit bucket " + bucket);
				}
			}
			if (service.stackableWith().isResolved()) {
				for (ServiceId other : service.stackableWith().resolvedValue()) {
					if (!servicesById.containsKey(other)) {
						throw new IllegalArgumentException("service " + service.id() + " stacks with unknown service " + other);
					}
				}
			}
		}
		requireAcyclicParents(bucketsById);
		if (policy.newCardGrace().isResolved()) {
			policy.newCardGrace().resolvedValue().ifPresent(grace -> {
				for (ServiceId service : grace.services()) {
					if (!servicesById.containsKey(service)) {
						throw new IllegalArgumentException("grace refers to unknown service " + service);
					}
				}
			});
		}
	}

	private static <T, K> Map<K, T> uniqueBy(List<T> items, Function<T, K> key, String name) {
		Map<K, T> byKey = items.stream().collect(Collectors.toMap(key, Function.identity(), (first, second) -> {
			throw new IllegalArgumentException("duplicate " + name + " " + key.apply(first));
		}));
		return Map.copyOf(byKey);
	}

	private static void requireAcyclicParents(Map<LimitBucketId, LimitBucket> bucketsById) {
		for (LimitBucket bucket : bucketsById.values()) {
			Set<LimitBucketId> visited = new HashSet<>();
			Optional<LimitBucketId> current = Optional.of(bucket.id());
			while (current.isPresent()) {
				if (!visited.add(current.get())) {
					throw new IllegalArgumentException("limit bucket parents form a cycle at " + bucket.id());
				}
				LimitBucket currentBucket = bucketsById.get(current.get());
				if (currentBucket == null) {
					throw new IllegalArgumentException("limit bucket refers to unknown parent " + current.get());
				}
				current = currentBucket.parent();
			}
		}
	}

	public RuleVersionRef ref() {
		return new RuleVersionRef(cardId, ruleVersion);
	}

	public boolean isVerified() {
		return status == VerificationStatus.VERIFIED;
	}

	/**
	 * Reasons a service cannot be calculated from this rule alone. Empty means calculable.
	 *
	 * <ul>
	 * <li>its calculation is unresolved;</li>
	 * <li>it or any limit it consumes (including parent limits) depends on performance and the
	 * performance window or date basis is unresolved;</li>
	 * <li>it consumes a limit and the limit period is unresolved.</li>
	 * </ul>
	 * Terms that matter only for some inputs (cancellation attribution, benefited-sale exclusion,
	 * deduction order, grace, stacking, sub-won handling) are reported by
	 * {@link #unresolvedConditionalTerms()} instead.
	 */
	public List<String> blockingReasons(BenefitService service) {
		List<String> reasons = new ArrayList<>(service.unresolvedCalculationReasons());
		boolean dependsOnPerformance = service.hasPerformanceRequirement()
				|| limitChain(service).stream().anyMatch(LimitBucket::dependsOnPerformance);
		if (dependsOnPerformance) {
			addReason(reasons, policy.performanceWindow());
			addReason(reasons, policy.performanceDateBasis());
		}
		if (!service.limitBuckets().isEmpty()) {
			addReason(reasons, policy.limitPeriod());
		}
		return List.copyOf(reasons);
	}

	private static void addReason(List<String> reasons, RuleTerm<?> term) {
		if (term instanceof RuleTerm.Unresolved<?> unresolved) {
			reasons.add(unresolved.reason());
		}
	}

	/**
	 * Unresolved rule-wide and service terms that only matter for some inputs. See
	 * {@link ConditionalTerm} for when one may be disregarded.
	 */
	public List<ConditionalTerm> unresolvedConditionalTerms() {
		List<ConditionalTerm> unresolved = new ArrayList<>();
		RulePolicy.addIfUnresolved(unresolved, "cancellationAttribution", policy.cancellationAttribution(),
				TermTrigger.CANCELLATION_AFFECTING_PERFORMANCE_WINDOW);
		RulePolicy.addIfUnresolved(unresolved, "excludesBenefitedSales", policy.excludesBenefitedSales(),
				TermTrigger.BENEFITED_SALE_IN_PERFORMANCE_WINDOW);
		RulePolicy.addIfUnresolved(unresolved, "limitDeductionOrder", policy.limitDeductionOrder(),
				TermTrigger.SHARED_LIMIT_CONSUMPTION);
		RulePolicy.addIfUnresolved(unresolved, "newCardGrace", policy.newCardGrace(), TermTrigger.WITHIN_NEW_CARD_GRACE);
		for (BenefitService service : services) {
			unresolved.addAll(service.unresolvedConditionalTerms());
		}
		return List.copyOf(unresolved);
	}

	/**
	 * Limit buckets a service consumes, including every parent (integrated) limit.
	 */
	public List<LimitBucket> limitChain(BenefitService service) {
		List<LimitBucket> chain = new ArrayList<>();
		for (LimitBucketId id : service.limitBuckets()) {
			Optional<LimitBucketId> current = Optional.of(id);
			while (current.isPresent()) {
				LimitBucket bucket = limitBucket(current.get());
				if (!chain.contains(bucket)) {
					chain.add(bucket);
				}
				current = bucket.parent();
			}
		}
		return List.copyOf(chain);
	}

	public LimitBucket limitBucket(LimitBucketId id) {
		return limitBuckets.stream()
			.filter(bucket -> bucket.id().equals(id))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("unknown limit bucket " + id));
	}

}
