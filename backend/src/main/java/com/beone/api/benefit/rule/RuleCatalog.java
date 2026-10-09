package com.beone.api.benefit.rule;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.card.ServiceId;
import com.beone.api.benefit.result.ApplicationStatus;

/**
 * Rule versions of all cards and the {@code VERIFIED} rule that applies on a decision date
 * (PRD FR-01).
 *
 * <p>
 * Selection checks the validity period first, then verification (Q1 of issue #15):
 * <ol>
 * <li>no version for the card: {@code NO_VERIFIED_RULE};</li>
 * <li>no version covers the date: {@code RULE_EXPIRED};</li>
 * <li>a covering version exists but none is {@code VERIFIED}: {@code NO_VERIFIED_RULE};</li>
 * <li>otherwise the single {@code VERIFIED} covering version.</li>
 * </ol>
 * Only {@code VERIFIED} rules are ever selected. Candidates, AI drafts and evaluation-only
 * snapshots are kept for representation and never selected here.
 */
public final class RuleCatalog {

	private final List<CardRuleVersion> versions;

	public RuleCatalog(List<CardRuleVersion> versions) {
		this.versions = List.copyOf(versions);
		Set<RuleVersionRef> refs = new HashSet<>();
		for (CardRuleVersion version : this.versions) {
			if (!refs.add(version.ref())) {
				throw new IllegalArgumentException("duplicate rule version " + version.ref());
			}
		}
		requireNoOverlappingVerifiedVersions();
	}

	private void requireNoOverlappingVerifiedVersions() {
		List<CardRuleVersion> verified = versions.stream().filter(CardRuleVersion::isVerified).toList();
		for (int i = 0; i < verified.size(); i++) {
			for (int j = i + 1; j < verified.size(); j++) {
				CardRuleVersion first = verified.get(i);
				CardRuleVersion second = verified.get(j);
				if (first.cardId().equals(second.cardId()) && first.validity().overlaps(second.validity())) {
					throw new IllegalArgumentException("VERIFIED rule versions " + first.ref() + " and " + second.ref()
							+ " overlap");
				}
			}
		}
	}

	public RuleSelection select(CardId cardId, LocalDate decisionDate) {
		Objects.requireNonNull(cardId, "cardId");
		Objects.requireNonNull(decisionDate, "decisionDate");
		List<CardRuleVersion> forCard = versions.stream().filter(v -> v.cardId().equals(cardId)).toList();
		if (forCard.isEmpty()) {
			return new RuleSelection.Unavailable(cardId, ApplicationStatus.NO_VERIFIED_RULE,
					"no rule version for card " + cardId);
		}
		List<CardRuleVersion> covering = forCard.stream().filter(v -> v.validity().contains(decisionDate)).toList();
		if (covering.isEmpty()) {
			return new RuleSelection.Unavailable(cardId, ApplicationStatus.RULE_EXPIRED,
					"no rule version of card " + cardId + " is valid on " + decisionDate);
		}
		List<CardRuleVersion> verified = covering.stream().filter(CardRuleVersion::isVerified).toList();
		if (verified.isEmpty()) {
			return new RuleSelection.Unavailable(cardId, ApplicationStatus.NO_VERIFIED_RULE,
					"no VERIFIED rule version of card " + cardId + " is valid on " + decisionDate);
		}
		return selected(verified.get(0));
	}

	private static RuleSelection.Selected selected(CardRuleVersion rule) {
		Map<ServiceId, List<String>> blocked = new LinkedHashMap<>();
		List<BenefitService> calculable = rule.services().stream().filter(service -> {
			List<String> reasons = rule.blockingReasons(service);
			if (!reasons.isEmpty()) {
				blocked.put(service.id(), reasons);
			}
			return reasons.isEmpty();
		}).toList();
		return new RuleSelection.Selected(rule, calculable, blocked, rule.unresolvedConditionalTerms());
	}

}
