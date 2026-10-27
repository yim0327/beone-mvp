package com.beone.api.benefit.rule;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.beone.api.benefit.card.MerchantClass;
import com.beone.api.benefit.card.TransactionNature;

/**
 * 카드 전체에 적용되는 실적·한도 조건(D-03). 공식 근거가 없는 조건은 {@link RuleTerm.Unresolved}로 둔다.
 *
 * @param performanceWindow 전월 실적 산정 기간
 * @param performanceDateBasis 거래가 어느 실적 기간에 속하는지 정하는 기준일
 * @param cancellationAttribution 취소 금액을 실적에서 차감하는 달
 * @param performanceExcludedNatures 실적에서 제외하는 원장 거래 성격
 * @param performanceExcludedClasses 실적에서 제외하는 업종
 * @param excludesBenefitedSales 할인받은 매출을 실적에서 제외하는지 여부
 * @param limitPeriod 월 한도 기간
 * @param limitDeductionOrder 거래가 한도를 차감하는 순서
 * @param newCardGrace 신규 유예. 약관에 유예가 없으면 빈 값으로 확정한다
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
