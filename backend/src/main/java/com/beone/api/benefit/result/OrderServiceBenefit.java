package com.beone.api.benefit.result;

import java.util.Objects;

import com.beone.api.benefit.money.BenefitAmount;

/**
 * 단일 주문에서 서비스 하나의 결과(D-13). 후보 판정, 최종 채택, 실제 기여액을 나눠 담는다.
 *
 * <p>
 * 채택 여부와 기여액은 후속 계산기가 근거를 가지고 정한다. 이 타입은 서로 모순되는 조합만 막고, 미확정 금액을 0원으로 바꾸거나 채택
 * 여부를 추론하지 않는다.
 *
 * @param candidate 이 서비스만 고를 때의 후보 판정
 * @param adoption 최종 카드 금액에 넣었는지
 * @param contribution 최종 카드 금액에 실제로 들어간 금액. 공유 한도 등으로 후보 금액보다 작을 수 있다
 */
public record OrderServiceBenefit(ServiceCandidate candidate, Adoption adoption, BenefitAmount contribution) {

	public OrderServiceBenefit {
		Objects.requireNonNull(candidate, "candidate");
		Objects.requireNonNull(adoption, "adoption");
		Objects.requireNonNull(contribution, "contribution");
		if (candidate.hasUnmetConditions() && adoption != Adoption.NOT_ADOPTED) {
			throw new IllegalArgumentException("service " + candidate.serviceId() + " has unmet conditions, so it cannot be "
					+ adoption);
		}
		if (adoption == Adoption.NOT_ADOPTED && !ServiceCandidate.isConfirmedZero(contribution)) {
			throw new IllegalArgumentException("service " + candidate.serviceId() + " is not adopted but contributes "
					+ contribution);
		}
		if (ServiceCandidate.isConfirmedZero(candidate.standaloneAmount())
				&& !ServiceCandidate.isConfirmedZero(contribution)) {
			throw new IllegalArgumentException("service " + candidate.serviceId() + " has a confirmed zero candidate but contributes "
					+ contribution);
		}
		// 둘 다 확정일 때만 비교한다. 한쪽이 확인 필요이면 대소를 단정하지 않는다.
		if (candidate.standaloneAmount().isConfirmed() && contribution.isConfirmed()
				&& contribution.confirmedWon().compareTo(candidate.standaloneAmount().confirmedWon()) > 0) {
			throw new IllegalArgumentException("service " + candidate.serviceId() + " contributes " + contribution
					+ " above its candidate amount " + candidate.standaloneAmount());
		}
		// 채택 여부를 모르면 미채택(기여액 0원)일 수도 있으므로 양수 기여액을 확정할 수 없다.
		if (adoption == Adoption.UNDETERMINED && contribution.isConfirmed() && contribution.confirmedWon().isPositive()) {
			throw new IllegalArgumentException("service " + candidate.serviceId()
					+ " adoption is undetermined, so it cannot contribute confirmed " + contribution);
		}
	}

}
