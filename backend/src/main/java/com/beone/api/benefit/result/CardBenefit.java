package com.beone.api.benefit.result;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.card.LimitBucketId;
import com.beone.api.benefit.card.ServiceId;
import com.beone.api.benefit.money.BenefitAmount;
import com.beone.api.benefit.rule.RuleVersionRef;

/**
 * Result for one candidate card.
 *
 * @param appliedRule rule version used; empty only when no rule could be used
 * @param status card-level application status
 * @param currentOrderBenefit benefit on the current order
 * @param monthlyTotal expected monthly benefit when the current order goes to this card
 * @param services per-service benefits on the current order
 * @param remainingLimits remaining monthly limit by bucket after the current order
 */
public record CardBenefit(CardId cardId, Optional<RuleVersionRef> appliedRule, ApplicationStatus status,
		BenefitAmount currentOrderBenefit, BenefitAmount monthlyTotal, List<ServiceBenefit> services,
		Map<LimitBucketId, BenefitAmount> remainingLimits) {

	public CardBenefit {
		Objects.requireNonNull(cardId, "cardId");
		Objects.requireNonNull(appliedRule, "appliedRule");
		Objects.requireNonNull(status, "status");
		Objects.requireNonNull(currentOrderBenefit, "currentOrderBenefit");
		Objects.requireNonNull(monthlyTotal, "monthlyTotal");
		services = List.copyOf(services);
		remainingLimits = Map.copyOf(remainingLimits);
		if (!status.isConsistentWith(currentOrderBenefit)) {
			throw new IllegalArgumentException("card " + cardId + " status " + status + " does not match amount "
					+ currentOrderBenefit);
		}
		boolean ruleUnavailable = status == ApplicationStatus.RULE_EXPIRED || status == ApplicationStatus.NO_VERIFIED_RULE;
		if (ruleUnavailable && (appliedRule.isPresent() || !services.isEmpty() || !remainingLimits.isEmpty())) {
			throw new IllegalArgumentException(
					"card " + cardId + " has no usable rule but reports a rule, services or remaining limits");
		}
		if (ruleUnavailable && monthlyTotal.isConfirmed()) {
			throw new IllegalArgumentException("card " + cardId + " has no usable rule, so its monthly total is unconfirmed");
		}
		if (!ruleUnavailable && appliedRule.isEmpty()) {
			throw new IllegalArgumentException("card " + cardId + " result must name the rule version it used");
		}
		appliedRule.ifPresent(rule -> {
			if (!rule.cardId().equals(cardId)) {
				throw new IllegalArgumentException("card " + cardId + " result names a rule of card " + rule.cardId());
			}
		});
		Set<ServiceId> seen = new HashSet<>();
		for (ServiceBenefit service : services) {
			if (!seen.add(service.serviceId())) {
				throw new IllegalArgumentException("card " + cardId + " reports service " + service.serviceId() + " twice");
			}
		}
	}

}
