# benefit 도메인 안내

## 목적과 범위

`benefit`은 카드 혜택 계산과 추천에 필요한 식별자, 금액, 상태, 입력, 약관 규칙, 결과를 정의합니다. 값만 담는 것이 아니라 잘못된 조합을 생성 시점에 차단하고, 모르는 정보는 확인 필요 상태로 유지합니다.

현재는 도메인 모델과 규칙 선택·보조 검증 로직이 구현되어 있습니다. 실제 혜택 금액 계산, 월간 최적화, 추천 API, CODEF 호출은 이 모델의 존재만으로 구현되었다고 볼 수 없습니다.

## 핵심 용어

- 서비스: 카드 하나가 제공하는 혜택 항목. 기본 적립·주유 할인 등이 각각 서비스입니다. Spring의 서비스 계층을 뜻하지 않습니다.
- 한도 버킷: 혜택이 소비하는 한도 묶음. 서비스별 한도와 여러 서비스를 묶는 통합 한도를 표현합니다.
- 실적 구간(tier): 전월 실적에 따라 계산 방식이나 월 한도가 달라지는 구간.
- 상태: 사용자의 전월 실적·사용 혜택 등 값과 그 출처·기준 기간.
- 규칙 버전: 특정 카드의 특정 적용 기간에 사용하는 약관 규칙 묶음.
- 확인 필요: 값이나 조건을 확정할 수 없는 상태. 확정된 0원과 다릅니다.

## 패키지 구조

```text
benefit/
├── card/    식별자와 거래 분류
├── money/   원화와 혜택 금액
├── state/   값의 출처·기준 기간·갱신 시각
├── input/   계산에 필요한 주문·카드·거래·선택 설정
├── rule/    약관 규칙과 검증 규칙 선택
└── result/  카드별 혜택과 최종 추천 결과
```

## 주요 클래스

### card: 식별자와 거래 분류

- [CardId](../../backend/src/main/java/com/beone/api/benefit/card/CardId.java): 문자열 카드 상품 ID. 앞자리 0을 보존하며 문자열 순서로 비교합니다.
- [ServiceId](../../backend/src/main/java/com/beone/api/benefit/card/ServiceId.java): 카드 안의 혜택 서비스 ID.
- [LimitBucketId](../../backend/src/main/java/com/beone/api/benefit/card/LimitBucketId.java): 서비스가 사용하는 한도 ID.
- [MerchantClass](../../backend/src/main/java/com/beone/api/benefit/card/MerchantClass.java): 사용자나 시나리오가 확인한 가맹점·업종 분류. 비슷한 이름만으로 추정하지 않습니다.
- [PaymentMethod](../../backend/src/main/java/com/beone/api/benefit/card/PaymentMethod.java): 직접 결제·대상 간편결제·자동이체 구분.
- [TransactionNature](../../backend/src/main/java/com/beone/api/benefit/card/TransactionNature.java): 일반 거래·세금 등 거래 성격. 승인/취소 구분과 독립적입니다.
- [Identifiers](../../backend/src/main/java/com/beone/api/benefit/card/Identifiers.java): 빈 값과 공백을 차단하는 패키지 내부 ID 검증 유틸리티.

### money: 금액

- [Won](../../backend/src/main/java/com/beone/api/benefit/money/Won.java): long 정수 원화. 취소 표현을 위해 음수도 허용하고 개별 모델이 허용 부호를 검사합니다.
- [BenefitAmount](../../backend/src/main/java/com/beone/api/benefit/money/BenefitAmount.java): 확정 금액 Confirmed와 사유를 가진 NeedsConfirmation으로 구분합니다. 확정 혜택은 음수일 수 없습니다.

### state: 값의 근거

- [StateValue](../../backend/src/main/java/com/beone/api/benefit/state/StateValue.java): 값과 출처·기준 기간·갱신 시각·추론 규칙 버전을 묶습니다. UNKNOWN은 값을 가질 수 없습니다.
- [StateSource](../../backend/src/main/java/com/beone/api/benefit/state/StateSource.java): CODEF·MANUAL·INFERRED·SYNTHETIC·UNKNOWN을 구분합니다. 출처 표시는 연동 구현 자체가 아닙니다.
- [BasisPeriod](../../backend/src/main/java/com/beone/api/benefit/state/BasisPeriod.java): 값의 기준 날짜 범위. 시작일과 종료일을 모두 포함합니다.

CODEF·수동 입력·추론 값에는 갱신 시각이 필요합니다. 추론 값은 사용한 규칙 버전도 필요합니다. 합성 데이터에는 가상의 갱신 시각을 강제로 만들지 않습니다.

### input: 계산 입력

- [RecommendationInput](../../backend/src/main/java/com/beone/api/benefit/input/RecommendationInput.java): 기준 날짜, 주문, 보유 카드, 원장, 월별 상태, 예정 지출, 선택 설정을 묶는 최상위 입력. 보유 카드 중복과 미보유 카드 참조 등을 검사합니다.
- [Order](../../backend/src/main/java/com/beone/api/benefit/input/Order.java): 현재 결제할 주문의 금액·날짜·가맹점 분류·결제 방식.
- [PlannedSpending](../../backend/src/main/java/com/beone/api/benefit/input/PlannedSpending.java): 사용자가 확인한 예정 지출 한 건.
- [PlannedSpendings](../../backend/src/main/java/com/beone/api/benefit/input/PlannedSpendings.java): 예정 지출 목록. 최대 5건.
- [HeldCard](../../backend/src/main/java/com/beone/api/benefit/input/HeldCard.java): 보유 카드와 보유 정보 출처, 등록일 상태.
- [CardMonthlyState](../../backend/src/main/java/com/beone/api/benefit/input/CardMonthlyState.java): 전월 실적, 버킷별 사용 혜택, 기타 버킷 사용량, 선택형 서비스 상태. 전월 실적 Optional.empty는 원장에서 산출해야 함을 뜻합니다.
- [ServiceSelection](../../backend/src/main/java/com/beone/api/benefit/input/ServiceSelection.java): 선택형 서비스와 적용 시작일.
- [LedgerTransaction](../../backend/src/main/java/com/beone/api/benefit/input/LedgerTransaction.java): 거래 한 건. 승인은 양수, 취소는 음수이며 취소 원거래 날짜 등을 검증합니다.
- [Ledger](../../backend/src/main/java/com/beone/api/benefit/input/Ledger.java): 거래 목록과 완전하게 확보된 기간. isComplete()로 기간 전체 확보 여부, cancellationEvidence()로 취소 관련 증거를 판정합니다.
- [LedgerCoverage](../../backend/src/main/java/com/beone/api/benefit/input/LedgerCoverage.java): 특정 카드·기간에 모든 거래가 확보되었다는 선언. 선언하지 않은 기간은 완전하다고 간주하지 않습니다.
- [LedgerEntryType](../../backend/src/main/java/com/beone/api/benefit/input/LedgerEntryType.java): 승인/취소 구분.
- [LedgerSource](../../backend/src/main/java/com/beone/api/benefit/input/LedgerSource.java): 시뮬레이션/CODEF 원장 구분.
- [SelectionPreference](../../backend/src/main/java/com/beone/api/benefit/input/SelectionPreference.java): 자동 추천·대표 카드 고정 설정. 고정 모드에는 대표 카드가 필요합니다.
- [SelectionMode](../../backend/src/main/java/com/beone/api/benefit/input/SelectionMode.java): AUTO_RECOMMEND와 FIXED_REPRESENTATIVE를 정의합니다.
- [InputChecks](../../backend/src/main/java/com/beone/api/benefit/input/InputChecks.java): 양수 금액과 선택적 문자열 검증을 공유하는 내부 유틸리티.

### rule: 약관 규칙

- [CardRuleVersion](../../backend/src/main/java/com/beone/api/benefit/rule/CardRuleVersion.java): 카드별 규칙 버전의 최상위 모델. 출처·정책·서비스·한도를 묶고 중복 ID, 없는 참조, 한도 부모 순환 등을 검사합니다. blockingReasons()는 계산을 막는 규칙 사유를, limitChain()은 상위 통합 한도까지 반환합니다.
- [RuleCatalog](../../backend/src/main/java/com/beone/api/benefit/rule/RuleCatalog.java): 카드와 판단 날짜에 맞는 VERIFIED 규칙만 선택합니다. 검증 규칙의 기간 중첩을 차단합니다.
- [RuleSelection](../../backend/src/main/java/com/beone/api/benefit/rule/RuleSelection.java): 규칙 선택 성공 Selected와 사용 불가 Unavailable. 성공 시에도 규칙상 계산 가능한 서비스와 확인 필요 서비스를 구분합니다.
- [RuleVersionRef](../../backend/src/main/java/com/beone/api/benefit/rule/RuleVersionRef.java): 카드 ID와 규칙 버전 참조.
- [ValidityPeriod](../../backend/src/main/java/com/beone/api/benefit/rule/ValidityPeriod.java): 규칙 유효 기간과 포함·중첩 검사. 종료일 미지정은 알려진 종료일이 없음을 뜻합니다.
- [VerificationStatus](../../backend/src/main/java/com/beone/api/benefit/rule/VerificationStatus.java): VERIFIED·AI_DRAFT·CANDIDATE·SYNTHETIC_TEST_SNAPSHOT 구분.
- [OfficialSource](../../backend/src/main/java/com/beone/api/benefit/rule/OfficialSource.java): 공식 자료 주소와 페이지·절 위치.
- [BenefitService](../../backend/src/main/java/com/beone/api/benefit/rule/BenefitService.java): 대상·제외 업종, 결제 방식, 금액 조건, 실적 구간, 한도, 선택 여부, 중복 혜택과 원 미만 처리 조건.
- [BenefitKind](../../backend/src/main/java/com/beone/api/benefit/rule/BenefitKind.java): 포인트 적립/청구 할인 구분.
- [BenefitCalculation](../../backend/src/main/java/com/beone/api/benefit/rule/BenefitCalculation.java): Rate 비율형과 FixedAmount 정액형 계산 방식의 표현. 금액을 직접 계산하는 엔진은 아닙니다.
- [ServiceTier](../../backend/src/main/java/com/beone/api/benefit/rule/ServiceTier.java): 최소 실적과 해당 구간의 계산 방식.
- [MerchantTarget](../../backend/src/main/java/com/beone/api/benefit/rule/MerchantTarget.java): 전체 가맹점 AllMerchants 또는 특정 분류 SpecificClasses.
- [LimitBucket](../../backend/src/main/java/com/beone/api/benefit/rule/LimitBucket.java): 실적 구간별 한도와 부모 통합 한도 참조.
- [LimitTier](../../backend/src/main/java/com/beone/api/benefit/rule/LimitTier.java): 최소 실적과 월 한도.
- [Tiers](../../backend/src/main/java/com/beone/api/benefit/rule/Tiers.java): 실적 구간이 비어 있지 않고 엄격한 오름차순인지 검사하는 내부 유틸리티.
- [RulePolicy](../../backend/src/main/java/com/beone/api/benefit/rule/RulePolicy.java): 카드 전체 실적·취소·제외·한도·신규 카드 유예 정책.
- [NewCardGrace](../../backend/src/main/java/com/beone/api/benefit/rule/NewCardGrace.java): 신규 카드 유예 기간, 간주 실적, 적용 서비스.
- [GraceSpan](../../backend/src/main/java/com/beone/api/benefit/rule/GraceSpan.java): 다음 달 말까지 또는 등록일부터 일정 일수라는 유예 기간 방식.
- [PerformanceWindow](../../backend/src/main/java/com/beone/api/benefit/rule/PerformanceWindow.java): 전월 실적 산정 기간.
- [TransactionDateBasis](../../backend/src/main/java/com/beone/api/benefit/rule/TransactionDateBasis.java): 승인일·이용일·명세서 기재일·매입일 구분.
- [CancellationAttribution](../../backend/src/main/java/com/beone/api/benefit/rule/CancellationAttribution.java): 취소의 실적 차감 귀속 방식.
- [LimitPeriod](../../backend/src/main/java/com/beone/api/benefit/rule/LimitPeriod.java): 한도 적용 기간.
- [LimitDeductionOrder](../../backend/src/main/java/com/beone/api/benefit/rule/LimitDeductionOrder.java): 승인 순서/매입 순서의 차감 방식.
- [FractionalWonPolicy](../../backend/src/main/java/com/beone/api/benefit/rule/FractionalWonPolicy.java): 원 미만 절사/반올림 방식. 타입이 존재해도 개별 카드 약관이 확정되었다는 뜻은 아닙니다.
- [RuleTerm](../../backend/src/main/java/com/beone/api/benefit/rule/RuleTerm.java): Resolved 확정 조건과 Unresolved 미확정 조건. 미확정 값 접근은 예외를 발생시킵니다.
- [ConditionalTerm](../../backend/src/main/java/com/beone/api/benefit/rule/ConditionalTerm.java): 특정 상황에서만 영향을 주는 미확정 조건. mayBeDisregarded()는 관련 상황이 없다고 확인된 경우에만 true입니다.
- [TermTrigger](../../backend/src/main/java/com/beone/api/benefit/rule/TermTrigger.java): 취소, 혜택받은 거래, 공유 한도 소비, 신규 유예, 중복 서비스, 원 미만 결과 등 조건 발생 상황.
- [TriggerEvidence](../../backend/src/main/java/com/beone/api/benefit/rule/TriggerEvidence.java): PRESENT 발생, CONFIRMED_ABSENT 부재 확인, UNDETERMINED 판단 불가.

RuleCatalog.select()는 규칙 없음 → NO_VERIFIED_RULE, 날짜 불일치 → RULE_EXPIRED, 유효하지만 미검증 → NO_VERIFIED_RULE, 유효한 VERIFIED 존재 → Selected 순서로 판단합니다. 계산 가능 서비스라는 표현은 규칙 자체의 차단 조건이 없다는 의미이며 현재 주문의 실적·업종·한도 조건 충족까지 보장하지 않습니다.

### result: 결과와 설명 근거

- [ServiceBenefit](../../backend/src/main/java/com/beone/api/benefit/result/ServiceBenefit.java): 서비스별 상태와 현재 주문 혜택.
- [CardBenefit](../../backend/src/main/java/com/beone/api/benefit/result/CardBenefit.java): 카드별 적용 규칙, 현재 주문 혜택, 예상 월 총혜택, 서비스 내역, 주문 이후 잔여 한도.
- [ApplicationStatus](../../backend/src/main/java/com/beone/api/benefit/result/ApplicationStatus.java): 적용·한도 소진·업종/실적/금액/결제 방식 미충족·규칙 부재·확인 필요 등 결과 사유. 금액 형태와 상태의 일관성을 검사합니다.
- [Recommendation](../../backend/src/main/java/com/beone/api/benefit/result/Recommendation.java): 카드별 결과와 최종 선택 결과. 중복 카드와 결과 없는 선택 등을 차단하고 선택 카드의 월 총혜택이 확정되었는지 검사합니다.
- [RecommendationOutcome](../../backend/src/main/java/com/beone/api/benefit/result/RecommendationOutcome.java): Selected 선택 결과 또는 NotDetermined 추천 불가 사유. Selected는 선택 근거, 동률 카드, 차순위와의 차이, 조건부 여부를 담습니다.
- [SelectionBasis](../../backend/src/main/java/com/beone/api/benefit/result/SelectionBasis.java): 월 총혜택 → 현재 주문 혜택 → 대표 카드 → 카드 ID 순서의 선택 근거를 표현합니다. 대표 카드·ID 선택은 혜택 우월성을 뜻하지 않는 임의 동률 규칙입니다.

결과 모델은 실제로 카드를 비교·선택하는 알고리즘이 아닙니다. 조건부 추천 여부를 판단하는 것도 후속 계산 로직의 책임입니다.

## 데이터·처리 흐름

1. Order, HeldCard, Ledger, CardMonthlyState 등을 RecommendationInput으로 묶습니다.
2. RuleCatalog가 판단 날짜에 적용 가능한 검증 규칙을 선택합니다.
3. 미확정 약관과 입력 상태를 고려해 혜택을 계산하고 월간 후보를 비교합니다. **이 단계의 엔진은 후속 구현입니다.**
4. 계산 로직이 ServiceBenefit, CardBenefit, Recommendation을 구성합니다. 현재 구현은 결과를 담는 모델과 일관성 검사입니다.

## 짧은 코드 예시

아래는 모델 사용 예시이며 계산 결과나 카드 약관 확정 근거가 아닙니다. 클래스는 해당 패키지에서 import합니다.

```java
CardId card = CardId.of("09271"); // 앞자리 0 유지
Won payment = Won.of(10_000);
BenefitCalculation rate = BenefitCalculation.rate(100); // 1%를 표현
BenefitCalculation fixed = BenefitCalculation.fixed(2_500);

BenefitAmount zero = BenefitAmount.confirmed(0); // 확정된 0원
BenefitAmount unknown = BenefitAmount.needsConfirmation("잔여 한도 확인 필요");
// unknown.confirmedWon()은 예외: 모르는 금액을 0원으로 대체하지 않음

RuleTerm<FractionalWonPolicy> rounding = RuleTerm.unresolved("공식 근거 확인 필요");
```

## 제약과 주의 사항

- record 생성자에서 잘못된 값을 조기에 차단합니다. List.copyOf(), Map.copyOf(), Set.copyOf()로 컬렉션 자체의 외부 변경을 막습니다.
- 금액은 정수 원화, 비율은 basis point 단위 정수로 표현합니다. 100 basis points = 1%, 10,000 = 100%입니다.
- UNKNOWN 상태, Unresolved 약관, NeedsConfirmation 혜택은 서로 다른 종류의 불확실성입니다. 모두 기본값이나 0원으로 숨기지 않습니다.
- 미확정 조건은 관련 상황의 부재가 확인된 경우에만 무시합니다. 증거 없음은 부재가 아니라 UNDETERMINED입니다.
- 날짜는 Asia/Seoul의 달력 날짜라는 입력 계약입니다. LocalDate 자체가 시간대를 변환하는 것은 아닙니다.
- 모델 검증과 규칙 선택 성공은 실제 공식 약관의 정확성 또는 추천 엔진 정확성을 자동으로 입증하지 않습니다.

## 현재 구현 범위와 관련 문서

구현된 범위는 위 모델, 생성 시 일관성 검사, 규칙 선택, 일부 기간·취소 증거 보조 로직입니다. 계산·최적화 엔진과 외부 연동은 별도 구현이 필요합니다.

테스트는 [benefit 테스트 디렉터리](../../backend/src/test/java/com/beone/api/benefit/)에서 확인합니다. fixture 매핑 테스트를 계산 결과 검증으로 혼동하지 않습니다. 이 문서 작성 과정에서는 테스트를 실행하지 않았습니다.

- [전체 구조](../architecture.md)
- [PRD](../PRD.md)
- [결정 기록](../decisions.md)
- [카드 혜택 조사](../card-benefit-research.md)
- [평가 사례집](../evaluation-casebook.md)

패키지·클래스 책임·입출력·검증 조건 변경 시 코드와 같은 PR에서 이 문서를 갱신합니다. 갱신 규칙은 [CLAUDE.md](../../CLAUDE.md), PR 확인 항목은 [CONTRIBUTING.md](../../CONTRIBUTING.md)를 따릅니다.
