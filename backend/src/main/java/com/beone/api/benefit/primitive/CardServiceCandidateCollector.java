package com.beone.api.benefit.primitive;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.beone.api.benefit.card.ServiceId;
import com.beone.api.benefit.input.CardMonthlyState;
import com.beone.api.benefit.input.Order;
import com.beone.api.benefit.money.BenefitAmount;
import com.beone.api.benefit.money.Won;
import com.beone.api.benefit.result.CardServiceCandidates;
import com.beone.api.benefit.result.ConfirmationStage;
import com.beone.api.benefit.result.ServiceCandidate;
import com.beone.api.benefit.result.ServiceNeedingConfirmation;
import com.beone.api.benefit.result.UnsupportedReason;
import com.beone.api.benefit.result.UnsupportedService;
import com.beone.api.benefit.rule.BenefitService;
import com.beone.api.benefit.rule.CardRuleVersion;
import com.beone.api.benefit.rule.NewCardGrace;
import com.beone.api.benefit.rule.RuleSelection;
import com.beone.api.benefit.rule.RuleTerm;
import com.beone.api.benefit.rule.TriggerEvidence;
import com.beone.api.benefit.state.StateValue;

/**
 * 규칙 선택 결과({@link RuleSelection})의 서비스마다 주문 한 건의 후보를 취합한다(#33, D-13).
 *
 * <p>
 * 서비스마다 아래 순서에서 처음 해당하는 분류 하나에만 담는다.
 * <ol>
 * <li>지원 범위 밖: 한도 버킷이 있거나 선택형인 서비스. 판정기를 호출하지 않는다. 금액·확정 의미가 없는 "이번 기능이 판정하지 않음"
 * 분류이며, 규칙 선택 단계에서 확인 필요로 분리된 서비스면 그 사유 원문을 함께 보존한다.</li>
 * <li>규칙 선택 단계 확인 필요: {@code servicesNeedingConfirmation}의 사유를 그대로 담는다.</li>
 * <li>전월 실적 미입력({@code Optional.empty})·{@code UNKNOWN}: 실적 값을 만들어 넣지 않고 확인 필요로 둔다.</li>
 * <li>판정기({@link ServiceCandidateEvaluator}) 결과: 확정 금액(확정 0원 포함)은 후보, 확인 필요 금액은 사유 원문과 함께 확인 필요로 옮긴다.</li>
 * </ol>
 *
 * <p>
 * 신규 유예 근거는 규칙의 유예 정의가 확정이고, 정의가 없거나 대상 서비스에 이 서비스가 없을 때만 {@code CONFIRMED_ABSENT}다.
 * 그 밖에는 유예 기간을 판정하지 않고 {@code UNDETERMINED}로 넘긴다. {@code unresolvedConditionalTerms}는 해소하거나 무시하지 않고 그대로
 * 전달한다. 채택·합계·한도 차감은 하지 않는다.
 */
public final class CardServiceCandidateCollector {

	private CardServiceCandidateCollector() {
	}

	/**
	 * 규칙 선택 결과의 서비스마다 주문 한 건의 후보를 취합한다. 채택·합계는 정하지 않는다.
	 *
	 * @param selection 카드 한 장의 규칙 선택 결과
	 * @param order 현재 주문
	 * @param state 같은 카드의 월간 상태
	 * @return 규칙을 쓸 수 없으면 {@link CardServiceCandidates.Unavailable}(서비스 목록·금액 없음),
	 * 아니면 규칙의 모든 서비스를 정확히 한 분류에 담은 {@link CardServiceCandidates.Collected}
	 * @throws NullPointerException 입력이 null인 경우
	 * @throws IllegalArgumentException 카드 ID가 다르거나, 확인된 전월 실적이 음수이거나(규칙 사용 불가·범위 밖 경로 포함),
	 * 선택 결과의 계산 가능·확인 필요 서비스가 규칙의 서비스를 중복·누락 없이 나누지 않거나 규칙에 없는 서비스를 담았거나,
	 * 사유 목록이 빈 확인 필요가 있거나, 규칙 선택 단계 확인 필요로 담을 사유에 빈 문구가 있는 경우
	 */
	public static CardServiceCandidates collect(RuleSelection selection, Order order, CardMonthlyState state) {
		Objects.requireNonNull(selection, "selection");
		Objects.requireNonNull(order, "order");
		Objects.requireNonNull(state, "state");
		if (!selection.cardId().equals(state.cardId())) {
			throw new IllegalArgumentException(
					"monthly state card " + state.cardId() + " does not match rule selection card " + selection.cardId());
		}
		// 판정기를 거치지 않는 분류에서도 잘못된 실적이 숨겨지지 않도록 먼저 거부한다.
		Optional<Won> performance = knownPerformance(state);
		if (performance.isPresent() && performance.get().isNegative()) {
			throw new IllegalArgumentException(
					"negative prior performance has no confirmed tier rule: " + performance.get().value());
		}
		return switch (selection) {
			case RuleSelection.Unavailable unavailable -> new CardServiceCandidates.Unavailable(unavailable.cardId(),
					unavailable.status(), unavailable.reason());
			case RuleSelection.Selected selected -> collected(selected, order, state, performance);
		};
	}

	private static CardServiceCandidates.Collected collected(RuleSelection.Selected selected, Order order,
			CardMonthlyState state, Optional<Won> performance) {
		requireConsistent(selected);
		CardRuleVersion rule = selected.rule();
		Map<ServiceId, List<String>> blocked = selected.servicesNeedingConfirmation();
		List<ServiceCandidate> candidates = new ArrayList<>();
		List<ServiceNeedingConfirmation> needingConfirmation = new ArrayList<>();
		List<UnsupportedService> unsupported = new ArrayList<>();
		for (BenefitService service : rule.services()) {
			// 1) 지원 범위 밖
			Set<UnsupportedReason> unsupportedReasons = unsupportedReasons(service);
			if (!unsupportedReasons.isEmpty()) {
				unsupported.add(new UnsupportedService(service.id(), unsupportedReasons,
						blocked.getOrDefault(service.id(), List.of())));
				continue;
			}
			// 2) 규칙 선택 단계 확인 필요
			if (blocked.containsKey(service.id())) {
				needingConfirmation.add(new ServiceNeedingConfirmation(service.id(), ConfirmationStage.RULE_SELECTION,
						blocked.get(service.id())));
				continue;
			}
			// 3) 전월 실적 미입력·미확인
			if (performance.isEmpty()) {
				needingConfirmation.add(new ServiceNeedingConfirmation(service.id(), ConfirmationStage.PRIOR_PERFORMANCE,
						List.of(missingPerformanceReason(state))));
				continue;
			}
			// 4) 판정기 결과
			ServiceCandidate candidate = ServiceCandidateEvaluator.evaluate(service, order, performance.get(),
					newCardGraceEvidence(rule, service));
			if (candidate.standaloneAmount() instanceof BenefitAmount.NeedsConfirmation pending) {
				needingConfirmation.add(new ServiceNeedingConfirmation(service.id(),
						ConfirmationStage.CANDIDATE_EVALUATION, List.of(pending.reason())));
			}
			else {
				candidates.add(candidate);
			}
		}
		return new CardServiceCandidates.Collected(rule.ref(), candidates, needingConfirmation, unsupported,
				selected.unresolvedConditionalTerms());
	}

	private static Optional<Won> knownPerformance(CardMonthlyState state) {
		return state.priorPerformance().filter(StateValue::isKnown).map(StateValue::knownValue);
	}

	private static String missingPerformanceReason(CardMonthlyState state) {
		return state.priorPerformance().isEmpty() ? "prior performance is not provided and has not been derived"
				: "prior performance is UNKNOWN";
	}

	/**
	 * 선택 결과의 계산 가능·확인 필요 서비스가 규칙의 서비스를 중복·누락 없이 나눴는지 확인한다.
	 */
	private static void requireConsistent(RuleSelection.Selected selected) {
		List<BenefitService> services = selected.rule().services();
		Set<ServiceId> ruleIds = new HashSet<>();
		services.forEach(service -> ruleIds.add(service.id()));
		Set<ServiceId> seen = new HashSet<>();
		for (BenefitService calculable : selected.calculableServices()) {
			if (!services.contains(calculable)) {
				throw new IllegalArgumentException("calculable service " + calculable.id() + " is not in rule "
						+ selected.rule().ref());
			}
			if (!seen.add(calculable.id())) {
				throw new IllegalArgumentException("calculable service " + calculable.id() + " is listed twice");
			}
		}
		for (Map.Entry<ServiceId, List<String>> entry : selected.servicesNeedingConfirmation().entrySet()) {
			ServiceId blocked = entry.getKey();
			// 사유 없는 확인 필요는 범위 밖 분류에서 확인 필요 표시가 사라지므로 거부한다.
			if (entry.getValue().isEmpty()) {
				throw new IllegalArgumentException("service " + blocked + " needs confirmation without a reason");
			}
			if (!ruleIds.contains(blocked)) {
				throw new IllegalArgumentException(
						"service " + blocked + " needing confirmation is not in rule " + selected.rule().ref());
			}
			if (!seen.add(blocked)) {
				throw new IllegalArgumentException("service " + blocked + " is both calculable and needing confirmation");
			}
		}
		if (!seen.equals(ruleIds)) {
			Set<ServiceId> missing = new HashSet<>(ruleIds);
			missing.removeAll(seen);
			throw new IllegalArgumentException("rule selection does not classify services " + missing);
		}
	}

	private static Set<UnsupportedReason> unsupportedReasons(BenefitService service) {
		Set<UnsupportedReason> reasons = EnumSet.noneOf(UnsupportedReason.class);
		if (!service.limitBuckets().isEmpty()) {
			reasons.add(UnsupportedReason.HAS_LIMIT_BUCKETS);
		}
		if (service.requiresSelection()) {
			reasons.add(UnsupportedReason.REQUIRES_SELECTION);
		}
		return reasons;
	}

	/**
	 * 유예 기간 자체는 판정하지 않으므로 {@code PRESENT}를 만들지 않는다.
	 */
	private static TriggerEvidence newCardGraceEvidence(CardRuleVersion rule, BenefitService service) {
		RuleTerm<Optional<NewCardGrace>> grace = rule.policy().newCardGrace();
		if (!grace.isResolved()) {
			return TriggerEvidence.UNDETERMINED;
		}
		Optional<NewCardGrace> definition = grace.resolvedValue();
		if (definition.isEmpty() || !definition.get().services().contains(service.id())) {
			return TriggerEvidence.CONFIRMED_ABSENT;
		}
		return TriggerEvidence.UNDETERMINED;
	}

}
