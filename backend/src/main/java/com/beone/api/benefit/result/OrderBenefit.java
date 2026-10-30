package com.beone.api.benefit.result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.beone.api.benefit.card.CardId;
import com.beone.api.benefit.card.ServiceId;
import com.beone.api.benefit.money.BenefitAmount;

/**
 * 카드 한 장에서 주문 한 건의 혜택 결정 결과(#35, D-13). 서비스별 최종 채택·기여액과 카드의 이번 주문 혜택을 담는다.
 *
 * <p>
 * 카드 간 비교·추천({@link CardBenefit}, {@link Recommendation})과는 아직 연결하지 않는다.
 */
public sealed interface OrderBenefit {

	/**
	 * 결과의 카드.
	 *
	 * @return 카드 ID
	 */
	CardId cardId();

	/**
	 * 이번 주문에서 이 카드의 혜택.
	 *
	 * @return 확정이면 확정 기여액의 합, 아니면 {@code 확인 필요}
	 */
	BenefitAmount orderBenefit();

	/**
	 * 쓸 수 있는 규칙이 없어 서비스 목록을 알 수 없는 경우. 카드 금액은 {@code 확인 필요}이며 0원이나 빈 결정 결과가 아니다.
	 *
	 * @param cardId 카드
	 * @param status {@code RULE_EXPIRED} 또는 {@code NO_VERIFIED_RULE}
	 * @param reason 규칙을 쓸 수 없는 사유. 카드 금액의 확인 필요 사유로 쓴다
	 */
	record Unavailable(CardId cardId, ApplicationStatus status, String reason) implements OrderBenefit {

		/**
		 * 규칙 사용 불가 상태와 사유를 확인한다.
		 *
		 * @throws IllegalArgumentException 상태가 규칙 사용 불가 상태가 아니거나 사유가 비어 있는 경우
		 */
		public Unavailable {
			Objects.requireNonNull(cardId, "cardId");
			if (status != ApplicationStatus.RULE_EXPIRED && status != ApplicationStatus.NO_VERIFIED_RULE) {
				throw new IllegalArgumentException("unavailable rule status must be RULE_EXPIRED or NO_VERIFIED_RULE");
			}
			if (reason == null || reason.isBlank()) {
				throw new IllegalArgumentException("reason is required");
			}
		}

		@Override
		public BenefitAmount orderBenefit() {
			return BenefitAmount.needsConfirmation(reason);
		}

	}

	/**
	 * 후보 취합 결과에서 서비스별 최종 결과를 정한 경우.
	 *
	 * <p>
	 * 생성 시 아래 조합만 확인한다.
	 * <ol>
	 * <li>{@code services}와 {@code rechecks}의 후보를 합치면 {@code source.candidates()}의 원소와 정확히 한 번씩 같다(equals).</li>
	 * <li>재확인 후보의 모양은 {@link PerformanceRecheck}가 확인한다.</li>
	 * <li>재확인 서비스마다 {@code PERFORMANCE_POLICY} 사유, 확인 필요·지원 범위 밖 서비스마다 해당 종류 사유가 있다. 사유의 서비스는
	 * 종류에 맞는 분류에 있다(확인 필요→{@code needingConfirmation}, 범위 밖→{@code unsupported}, 실적 정책→재확인 후보나 양수 후보,
	 * 중복 관계→양수 후보).</li>
	 * <li>카드 금액이 확정이면 사유가 없고, {@code 확인 필요}이면 사유가 있다.</li>
	 * <li>확정 0원 후보는 {@code NOT_ADOPTED}다.</li>
	 * <li>확정일 때: {@code UNDETERMINED}가 없고, 양수 후보가 있으면 후보 금액 최댓값 중 서비스 ID가 가장 앞선 하나만 후보 금액 그대로
	 * {@code ADOPTED}이며, 카드 금액은 기여액 합이다.</li>
	 * <li>미확정일 때: {@code ADOPTED}가 없고 양수 후보는 모두 {@code UNDETERMINED}·기여액 {@code 확인 필요}다.</li>
	 * <li>{@code services}·{@code rechecks}는 서비스 ID 오름차순, {@code reasons}는 {@link PendingReason#ORDER} 순서이며 중복이 없다.
	 * 정렬하지 않고 어긋나면 거부한다. 모든 목록은 불변 복사본이다.</li>
	 * </ol>
	 * 실적 조건 여부, 중복 관계, 취소 귀속·할인 매출 근거의 영향처럼 규칙과 근거가 있어야 알 수 있는 판단은 확인하지 않는다(결정기 책임).
	 *
	 * @param source 원본 후보 취합 결과. 바꾸지 않고 보존한다
	 * @param services 최종 채택·기여액을 정한 후보(미확정 표시 포함)
	 * @param rechecks 실적 정책 조건 때문에 최종 결과를 정하지 못한 후보. 원본 0원은 최종 기여액이 아니다
	 * @param reasons 카드 금액을 확정하지 못한 사유
	 * @param orderBenefit 카드의 이번 주문 혜택
	 */
	record Decided(CardServiceCandidates.Collected source, List<OrderServiceBenefit> services,
			List<PerformanceRecheck> rechecks, List<PendingReason> reasons, BenefitAmount orderBenefit)
			implements OrderBenefit {

		/**
		 * 위 조합을 확인하고 목록을 불변 복사본으로 보관한다.
		 *
		 * @throws IllegalArgumentException 조합이 맞지 않는 경우
		 */
		public Decided {
			Objects.requireNonNull(source, "source");
			services = List.copyOf(services);
			rechecks = List.copyOf(rechecks);
			reasons = List.copyOf(reasons);
			Objects.requireNonNull(orderBenefit, "orderBenefit");
			requireSourceCoverage(source, services, rechecks);
			requireAscending(services.stream().map(service -> service.candidate().serviceId()).toList(), "services");
			requireAscending(rechecks.stream().map(recheck -> recheck.candidate().serviceId()).toList(), "rechecks");
			for (int i = 1; i < reasons.size(); i++) {
				if (PendingReason.ORDER.compare(reasons.get(i - 1), reasons.get(i)) >= 0) {
					throw new IllegalArgumentException("reasons must be unique and in order: " + reasons);
				}
			}
			requireReasonLinks(source, services, rechecks, reasons);
			requireAdoptions(services, reasons, orderBenefit);
		}

		@Override
		public CardId cardId() {
			return source.cardId();
		}

		private static void requireSourceCoverage(CardServiceCandidates.Collected source,
				List<OrderServiceBenefit> services, List<PerformanceRecheck> rechecks) {
			List<ServiceCandidate> remaining = new ArrayList<>(source.candidates());
			List<ServiceCandidate> decided = new ArrayList<>();
			services.forEach(service -> decided.add(service.candidate()));
			rechecks.forEach(recheck -> decided.add(recheck.candidate()));
			for (ServiceCandidate candidate : decided) {
				if (!remaining.remove(candidate)) {
					throw new IllegalArgumentException("candidate " + candidate.serviceId()
							+ " is not in the source or is decided more than once");
				}
			}
			if (!remaining.isEmpty()) {
				throw new IllegalArgumentException("source candidates are not decided: "
						+ remaining.stream().map(ServiceCandidate::serviceId).toList());
			}
		}

		private static void requireAscending(List<ServiceId> ids, String name) {
			for (int i = 1; i < ids.size(); i++) {
				if (PendingReason.SERVICE_ORDER.compare(ids.get(i - 1), ids.get(i)) >= 0) {
					throw new IllegalArgumentException(name + " must be in ascending service id order: " + ids);
				}
			}
		}

		private static void requireReasonLinks(CardServiceCandidates.Collected source, List<OrderServiceBenefit> services,
				List<PerformanceRecheck> rechecks, List<PendingReason> reasons) {
			Set<ServiceId> pending = new HashSet<>();
			source.needingConfirmation().forEach(service -> pending.add(service.serviceId()));
			Set<ServiceId> unsupported = new HashSet<>();
			source.unsupported().forEach(service -> unsupported.add(service.serviceId()));
			Set<ServiceId> rechecked = new HashSet<>();
			rechecks.forEach(recheck -> rechecked.add(recheck.candidate().serviceId()));
			Set<ServiceId> positive = new HashSet<>();
			services.stream()
				.filter(service -> isPositive(service.candidate()))
				.forEach(service -> positive.add(service.candidate().serviceId()));
			Set<ServiceId> policyTargets = new HashSet<>(rechecked);
			policyTargets.addAll(positive);

			Map<PendingReason.Kind, Set<ServiceId>> linked = new HashMap<>();
			for (PendingReason reason : reasons) {
				Set<ServiceId> allowed = switch (reason.kind()) {
					case NEEDS_CONFIRMATION -> pending;
					case UNSUPPORTED -> unsupported;
					case PERFORMANCE_POLICY -> policyTargets;
					case STACKING_UNRESOLVED, STACKING_CONFIRMED -> positive;
				};
				if (!allowed.containsAll(reason.services())) {
					throw new IllegalArgumentException(reason.kind() + " reason refers to services outside its classification: "
							+ reason.services());
				}
				linked.computeIfAbsent(reason.kind(), kind -> new HashSet<>()).addAll(reason.services());
			}
			requireLinked(linked, PendingReason.Kind.NEEDS_CONFIRMATION, pending);
			requireLinked(linked, PendingReason.Kind.UNSUPPORTED, unsupported);
			requireLinked(linked, PendingReason.Kind.PERFORMANCE_POLICY, rechecked);
		}

		private static void requireLinked(Map<PendingReason.Kind, Set<ServiceId>> linked, PendingReason.Kind kind,
				Set<ServiceId> required) {
			Set<ServiceId> missing = new HashSet<>(required);
			missing.removeAll(linked.getOrDefault(kind, Set.of()));
			if (!missing.isEmpty()) {
				throw new IllegalArgumentException("services " + missing + " need a " + kind + " reason");
			}
		}

		private static void requireAdoptions(List<OrderServiceBenefit> services, List<PendingReason> reasons,
				BenefitAmount orderBenefit) {
			if (orderBenefit.isConfirmed() != reasons.isEmpty()) {
				throw new IllegalArgumentException("order benefit " + orderBenefit + " must be confirmed exactly when there are no reasons");
			}
			for (OrderServiceBenefit service : services) {
				// 확정 0원 후보는 U2·U6에 따라 미채택이다.
				if (ServiceCandidate.isConfirmedZero(service.candidate().standaloneAmount())
						&& service.adoption() != Adoption.NOT_ADOPTED) {
					throw new IllegalArgumentException("service " + service.candidate().serviceId()
							+ " has a confirmed zero candidate, so it must be NOT_ADOPTED");
				}
			}
			if (orderBenefit.isConfirmed()) {
				requireConfirmed(services, orderBenefit);
			}
			else {
				requirePending(services);
			}
		}

		private static void requireConfirmed(List<OrderServiceBenefit> services, BenefitAmount orderBenefit) {
			long total = 0;
			for (OrderServiceBenefit service : services) {
				if (service.adoption() == Adoption.UNDETERMINED || !service.contribution().isConfirmed()) {
					throw new IllegalArgumentException("service " + service.candidate().serviceId()
							+ " is undetermined while the order benefit is confirmed");
				}
				total = Math.addExact(total, service.contribution().confirmedWon().value());
			}
			if (orderBenefit.confirmedWon().value() != total) {
				throw new IllegalArgumentException("order benefit " + orderBenefit + " is not the sum of contributions " + total);
			}
			List<OrderServiceBenefit> adopted = services.stream()
				.filter(service -> service.adoption() == Adoption.ADOPTED)
				.toList();
			// 후보 금액 내림차순, 같으면 서비스 ID 오름차순의 첫 후보가 채택 대상이다(U3).
			Comparator<OrderServiceBenefit> preferred = Comparator
				.comparing((OrderServiceBenefit service) -> service.candidate().standaloneAmount().confirmedWon())
				.reversed()
				.thenComparing(service -> service.candidate().serviceId(), PendingReason.SERVICE_ORDER);
			List<OrderServiceBenefit> expected = services.stream()
				.filter(service -> isPositive(service.candidate()))
				.sorted(preferred)
				.limit(1)
				.toList();
			if (!adopted.equals(expected)) {
				throw new IllegalArgumentException("adopted services " + ids(adopted) + " must be the largest candidate "
						+ ids(expected) + " with the first service id");
			}
			for (OrderServiceBenefit service : adopted) {
				if (!service.contribution().equals(service.candidate().standaloneAmount())) {
					throw new IllegalArgumentException("adopted service " + service.candidate().serviceId()
							+ " must contribute its candidate amount " + service.candidate().standaloneAmount());
				}
			}
		}

		private static void requirePending(List<OrderServiceBenefit> services) {
			for (OrderServiceBenefit service : services) {
				if (service.adoption() == Adoption.ADOPTED) {
					throw new IllegalArgumentException("service " + service.candidate().serviceId()
							+ " cannot be ADOPTED while the order benefit needs confirmation");
				}
				if (isPositive(service.candidate())
						&& (service.adoption() != Adoption.UNDETERMINED || service.contribution().isConfirmed())) {
					throw new IllegalArgumentException("positive service " + service.candidate().serviceId()
							+ " must be UNDETERMINED with a contribution needing confirmation");
				}
			}
		}

		private static boolean isPositive(ServiceCandidate candidate) {
			return candidate.standaloneAmount().isConfirmed() && candidate.standaloneAmount().confirmedWon().isPositive();
		}

		private static List<ServiceId> ids(List<OrderServiceBenefit> services) {
			return services.stream().map(service -> service.candidate().serviceId()).toList();
		}

	}

}
