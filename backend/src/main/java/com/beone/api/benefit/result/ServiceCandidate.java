package com.beone.api.benefit.result;

import java.util.Objects;
import java.util.Set;

import com.beone.api.benefit.card.ServiceId;
import com.beone.api.benefit.money.BenefitAmount;

/**
 * 서비스 하나만 선택한다고 할 때의 후보 판정(D-13). 최종 카드 금액에 넣었는지와 실제 기여액은 담지 않는다.
 *
 * <p>
 * 기존 {@link ServiceBenefit}과 다른 타입이다. 후보에는 적용 상태({@code APPLIED} 계열)를 붙이지 않는다.
 *
 * @param standaloneAmount 이 서비스만 고를 때의 금액. 원 미만 처리 방식 등 근거가 없으면 확인 필요로 둔다
 * @param unmetConditions 확인된 미충족 사유. 순서는 계약이 아니다(사유 우선순위는 미결)
 */
public record ServiceCandidate(ServiceId serviceId, BenefitAmount standaloneAmount,
		Set<ApplicationStatus> unmetConditions) {

	/** 후보의 미충족 사유로 쓸 수 있는 상태. 상태가 추가돼도 자동으로 허용하지 않으며, 목록은 바꿀 수 없다. */
	static final Set<ApplicationStatus> ALLOWED_UNMET_CONDITIONS = Set.of(ApplicationStatus.NOT_APPLICABLE_EXCLUDED,
			ApplicationStatus.NOT_APPLICABLE_CATEGORY, ApplicationStatus.NOT_APPLICABLE_PERFORMANCE,
			ApplicationStatus.NOT_APPLICABLE_MIN_AMOUNT, ApplicationStatus.NOT_APPLICABLE_PAYMENT_METHOD,
			ApplicationStatus.LIMIT_EXHAUSTED);

	public ServiceCandidate {
		Objects.requireNonNull(serviceId, "serviceId");
		Objects.requireNonNull(standaloneAmount, "standaloneAmount");
		Objects.requireNonNull(unmetConditions, "unmetConditions");
		unmetConditions = Set.copyOf(unmetConditions);
		for (ApplicationStatus condition : unmetConditions) {
			if (!ALLOWED_UNMET_CONDITIONS.contains(condition)) {
				throw new IllegalArgumentException("service " + serviceId + " cannot use " + condition + " as an unmet condition");
			}
		}
		// 조건은 모두 충족해야 하므로 미충족이 하나라도 확인되면 후보 금액은 확정 0원이다.
		if (!unmetConditions.isEmpty() && !isConfirmedZero(standaloneAmount)) {
			throw new IllegalArgumentException("service " + serviceId + " has unmet conditions " + unmetConditions
					+ " but amount " + standaloneAmount);
		}
	}

	public boolean hasUnmetConditions() {
		return !unmetConditions.isEmpty();
	}

	static boolean isConfirmedZero(BenefitAmount amount) {
		return amount.isConfirmed() && amount.confirmedWon().isZero();
	}

}
