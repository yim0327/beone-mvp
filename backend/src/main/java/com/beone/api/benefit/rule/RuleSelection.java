package com.beone.api.benefit.rule;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.card.ServiceId;
import com.beone.api.benefit.money.BenefitAmount;
import com.beone.api.benefit.result.ApplicationStatus;

/**
 * Outcome of selecting a rule for one card on one decision date.
 */
public sealed interface RuleSelection {

	CardId cardId();

	/**
	 * A {@code VERIFIED} rule covers the date. Services are split into those calculable from the rule
	 * and those blocked by unresolved terms, which stay {@code 확인 필요}.
	 *
	 * @param rule the selected rule
	 * @param calculableServices services with no blocking unresolved term
	 * @param servicesNeedingConfirmation blocking reasons by service; never calculated
	 * @param unresolvedConditionalTerms terms that keep the affected benefit {@code 확인 필요}
	 * unless their trigger is confirmed absent ({@link ConditionalTerm})
	 */
	record Selected(CardRuleVersion rule, List<BenefitService> calculableServices,
			Map<ServiceId, List<String>> servicesNeedingConfirmation,
			List<ConditionalTerm> unresolvedConditionalTerms) implements RuleSelection {

		public Selected {
			Objects.requireNonNull(rule, "rule");
			calculableServices = List.copyOf(calculableServices);
			servicesNeedingConfirmation = Map.copyOf(servicesNeedingConfirmation);
			unresolvedConditionalTerms = List.copyOf(unresolvedConditionalTerms);
		}

		@Override
		public CardId cardId() {
			return rule.cardId();
		}

	}

	/**
	 * No rule can be used. The benefit is not calculated and its amount is unconfirmed, not 0 won.
	 */
	record Unavailable(CardId cardId, ApplicationStatus status, String reason) implements RuleSelection {

		public Unavailable {
			Objects.requireNonNull(cardId, "cardId");
			if (status != ApplicationStatus.RULE_EXPIRED && status != ApplicationStatus.NO_VERIFIED_RULE) {
				throw new IllegalArgumentException("unavailable rule status must be RULE_EXPIRED or NO_VERIFIED_RULE");
			}
			if (reason == null || reason.isBlank()) {
				throw new IllegalArgumentException("reason is required");
			}
		}

		public BenefitAmount amount() {
			return BenefitAmount.needsConfirmation(reason);
		}

	}

}
