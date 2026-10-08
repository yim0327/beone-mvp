# 카드 4종 독립 평가 사례집 (검토용 초안)

- 이슈: [#9](https://github.com/yim0327/beone-mvp/issues/9) · 요구: [PRD §6](PRD.md) · 기계 판독 입력: [evaluation-cases.json](../fixtures/evaluation-cases.json)
- **상태: `DRAFT_CONDITIONAL_NOT_VERIFIED`.** 아래 금액은 공식 자료에서 읽은 **후보 규칙을 합성 입력에 적용한 손계산**이다. `VERIFIED` 룰, 실제 카드의 약관 버전, 엔진 통과 결과나 확정 추천을 뜻하지 않는다.
- 모든 입력은 `SYNTHETIC`이다. 실계정·CODEF 거래나 카드번호는 사용하지 않는다. 각 C사례는 다른 C사례의 원장·한도를 공유하지 않는다. M사례만 지정한 C사례의 **입력 한 필드**를 바꾼다.
- 기본 시계는 2026-10-15(한국시간), 등록일은 2026-01-01, 이전 실적 창 원장은 완전하며 적히지 않은 거래는 없다. 이전 실적용 `ORDINARY`는 할인받지 않은 인정 거래, `TAX`는 제외 거래, `CANCEL_RECEIVED`는 표기된 월에 접수된 취소다. 지정한 `merchantClass`·결제 방식은 이 **가상 시나리오에서 확인된 분류**이고 카드사나 CODEF가 실제 반환했다는 뜻이 아니다.
- 적지 않은 혜택 사용액은 0원, 예정 소비는 0건이다. 상품권·무이자할부·입점매장·통합청구 등 추가 제외 조건은 없는 입력으로 고정한다. 선택 서비스가 필요한 C04는 `FUEL`이 9월 1일부터 유효하다고 가정한다. 카드별 공식 규칙의 실제 효력 시작일은 미검증이다.
- `기대 카드`는 후보 규칙만 모두 적용 가능하다고 **조건부**로 가정한 값이다. 금액 0원은 조건 불충족 또는 한도 소진이고, `확인 필요`는 0원과 다르다. 카드별 `applicationStatusByCard`가 사유를 분리한다. C02는 가상 규칙 만료를 시험하므로 금액·추천을 비워 두며 0원으로 대체하지 않는다.
- PRD가 요구하는 독립 교차 검토 담당(D-06)은 미결이다. 규칙 버전·대상 분류·취소/한도/원 미만 처리의 검증 후 별도 승인해야 정답을 고정할 수 있다. 엔진이 아직 없어서 이 파일만으로 자동 테스트 통과를 주장하지 않는다.

## 공식 근거

- **TI_BASE**: [공식 원문](https://img2.kbcard.com/obj/card/download/09271__prdctOpmn_20240417.pdf) (pp.1,3)
- **TI_STORE**: [공식 원문](https://img2.kbcard.com/obj/card/download/09271__prdctOpmn_20240417.pdf) (pp.1-4)
- **TT**: [공식 원문](https://img2.kbcard.com/obj/card/download/09174__prdctOpmn_20260714.pdf) (pp.1-2)
- **NO**: [공식 상품설명서](https://img2.kbcard.com/obj/card/download/01664__prdctOpmn_20260818.pdf) 1쪽; [공식 상품 상세](https://card.kbcard.com/CRD/DVIEW/HCAMCXPRICAC0076?mainCC=a&cooperationcode=01664) 통신 할인 항목.
- **CC**: [공식 원문](https://img2.kbcard.com/obj/card/download/01914__prdctOpmn_20260818.pdf) (pp.1-2)
- **D03**: [저장소 문서](../docs/decisions.md#d-03d-04-확정-정책)
- **PRD_RULE**: [저장소 문서](../docs/PRD.md#fr-01-공식-약관과-규칙-데이터)
- **PRD_OPT**: [저장소 문서](../docs/PRD.md#fr-05-결정론적-혜택-계산과-최적화)

## 대표 사례 20개

### C01. 기본 적립

- 카드: `09271`. 전월 합성 원장: 없음.
- 현재 주문: 2026-10-15 ORDINARY_DOMESTIC 100,000원, DIRECT. 당월 사용: 0원.
- 후보 기대: 09271 1,000원. 적용 상태: 09271 APPLIED. 기대 카드: `09271`.
- 손계산·판정: 100,000×1%=1,000. 근거: [TI_BASE](https://img2.kbcard.com/obj/card/download/09271__prdctOpmn_20240417.pdf).

### C02. 약관 버전 만료/부재

- 카드: `09271`. 전월 합성 원장: 없음.
- 현재 주문: 2026-10-15 ORDINARY_DOMESTIC 100,000원, DIRECT. 당월 사용: 0원. 가상 버전 상태: EXPIRED_TEST_SENTINEL.
- 후보 기대: 09271 확인 필요. 적용 상태: 09271 UNKNOWN_RULE_VERSION. 기대 카드: 확정하지 않음.
- 손계산·판정: 적용 가능한 검증 버전이 없으므로 확인 필요. 0원 아님. 근거: [PRD_RULE](../docs/PRD.md#fr-01-공식-약관과-규칙-데이터), [D03](../docs/decisions.md#d-03d-04-확정-정책).

### C03. 적립 제외

- 카드: `09271`. 전월 합성 원장: 없음.
- 현재 주문: 2026-10-15 TAX 100,000원, DIRECT. 당월 사용: 0원.
- 후보 기대: 09271 0원. 적용 상태: 09271 NOT_APPLICABLE_EXCLUDED. 기대 카드: `09271`.
- 손계산·판정: 세금·공과금은 기본 적립 제외. 근거: [TI_BASE](https://img2.kbcard.com/obj/card/download/09271__prdctOpmn_20240417.pdf).

### C04. 선택 FUEL 업종 불일치와 기본 적립

- 카드: `09271`. 전월 합성 원장: 2026-09-15 09271 ORDINARY 400,000원.
- 현재 주문: 2026-10-15 GS25_STORE 50,000원, DIRECT. 선택: FUEL (2026-09-01부터). 당월 사용: 0원.
- 후보 기대: 09271 500원. 적용 상태: 09271 APPLIED. 서비스별 09271:FUEL 0원 / 09271:BASE 500원. 기대 카드: `09271`.
- 손계산·판정: GS25는 FUEL 대상 아님. 선택 할인 0, 일반 국내 기본 적립 50,000×1%=500. 근거: [TI_BASE](https://img2.kbcard.com/obj/card/download/09271__prdctOpmn_20240417.pdf), [TI_STORE](https://img2.kbcard.com/obj/card/download/09271__prdctOpmn_20240417.pdf).

### C05. Great 한도 완전 소진

- 카드: `09174`. 전월 합성 원장: 2026-09-15 09174 ORDINARY 300,000원.
- 현재 주문: 2026-10-15 STARBUCKS_STORE 20,000원, DIRECT. 당월 사용: 09174:Great 10,000원 사용.
- 후보 기대: 09174 0원. 적용 상태: 09174 LIMIT_EXHAUSTED. 기대 카드: `09174`.
- 손계산·판정: Great 월 한도 10,000을 이미 모두 사용해 추가 할인 0. 근거: [TT](https://img2.kbcard.com/obj/card/download/09174__prdctOpmn_20260714.pdf).

### C06. Great 정상 할인

- 카드: `09174`. 전월 합성 원장: 2026-09-15 09174 ORDINARY 300,000원.
- 현재 주문: 2026-10-15 STARBUCKS_STORE 20,000원, DIRECT. 당월 사용: 0원.
- 후보 기대: 09174 10,000원. 적용 상태: 09174 APPLIED. 기대 카드: `09174`.
- 손계산·판정: 20,000×50%=10,000 (Great 월 상한). 근거: [TT](https://img2.kbcard.com/obj/card/download/09174__prdctOpmn_20260714.pdf).

### C07. Great+Check 허용 중복

- 카드: `09174`. 전월 합성 원장: 2026-09-15 09174 ORDINARY 300,000원.
- 현재 주문: 2026-10-15 STARBUCKS_STORE 20,000원, ELIGIBLE_SIMPLE_PAY. 당월 사용: 0원.
- 후보 기대: 09174 12,000원. 적용 상태: 09174 APPLIED. 서비스별 09174:Great 10,000원 / 09174:Check 2,000원. 기대 카드: `09174`.
- 손계산·판정: Great 10,000 + Check 20,000×10%=2,000. 근거: [TT](https://img2.kbcard.com/obj/card/download/09174__prdctOpmn_20260714.pdf).

### C08. 실적 1원 미달

- 카드: `09174`. 전월 합성 원장: 2026-09-15 09174 ORDINARY 299,999원.
- 현재 주문: 2026-10-15 STARBUCKS_STORE 20,000원, DIRECT. 당월 사용: 0원.
- 후보 기대: 09174 0원. 적용 상태: 09174 NOT_APPLICABLE_PERFORMANCE. 기대 카드: `09174`.
- 손계산·판정: 전월 299,999 < 300,000. 근거: [TT](https://img2.kbcard.com/obj/card/download/09174__prdctOpmn_20260714.pdf).

### C09. 실적 제외 거래

- 카드: `09174`. 전월 합성 원장: 2026-09-15 09174 ORDINARY 290,000원; 2026-09-20 09174 TAX 20,000원.
- 현재 주문: 2026-10-15 STARBUCKS_STORE 20,000원, DIRECT. 당월 사용: 0원.
- 후보 기대: 09174 0원. 적용 상태: 09174 NOT_APPLICABLE_PERFORMANCE. 기대 카드: `09174`.
- 손계산·판정: 전월 승인 합계 310,000이나 세금 20,000 제외, 인정 290,000. 근거: [TT](https://img2.kbcard.com/obj/card/download/09174__prdctOpmn_20260714.pdf).

### C10. Great 잔여한도

- 카드: `09174`. 전월 합성 원장: 2026-09-15 09174 ORDINARY 300,000원.
- 현재 주문: 2026-10-15 STARBUCKS_STORE 20,000원, DIRECT. 당월 사용: 09174:Great 9,000원 사용.
- 후보 기대: 09174 1,000원. 적용 상태: 09174 APPLIED_CAP_REMAINING. 기대 카드: `09174`.
- 손계산·판정: 산식 10,000, Great 남은 1,000. 근거: [TT](https://img2.kbcard.com/obj/card/download/09174__prdctOpmn_20260714.pdf).

### C11. 노리 스타벅스 정상

- 카드: `01664`. 전월 합성 원장: 2026-09-15 01664 ORDINARY 200,000원.
- 현재 주문: 2026-10-15 STARBUCKS_STORE 10,000원, DIRECT. 당월 사용: 0원.
- 후보 기대: 01664 2,000원. 적용 상태: 01664 APPLIED. 기대 카드: `01664`.
- 손계산·판정: 10,000×20%=2,000; 통합한도 10,000 이내. 근거: [NO](https://img2.kbcard.com/obj/card/download/01664__prdctOpmn_20260818.pdf).

### C12. 노리 건당 최소금액 미달

- 카드: `01664`. 전월 합성 원장: 2026-09-15 01664 ORDINARY 200,000원.
- 현재 주문: 2026-10-15 STARBUCKS_STORE 9,999원, DIRECT. 당월 사용: 0원.
- 후보 기대: 01664 0원. 적용 상태: 01664 NOT_APPLICABLE_MIN_AMOUNT. 기대 카드: `01664`.
- 손계산·판정: 스타벅스 건당 10,000 미달. 근거: [NO](https://img2.kbcard.com/obj/card/download/01664__prdctOpmn_20260818.pdf).

### C13. 노리 통신 자동납부

- 카드: `01664`. 전월 합성 원장: 2026-09-15 01664 ORDINARY 300,000원.
- 현재 주문: 2026-10-15 TELECOM_SKT 50,000원, AUTOPAY. 당월 사용: 0원.
- 후보 기대: 01664 2,500원. 적용 상태: 01664 APPLIED. 기대 카드: `01664`.
- 손계산·판정: 전월 300,000, 건당 50,000 이상, 월 1회 2,500. 근거: [NO](https://img2.kbcard.com/obj/card/download/01664__prdctOpmn_20260818.pdf).

### C14. 노리 취소월 실적 차감

- 카드: `01664`. 전월 합성 원장: 2026-09-15 01664 ORDINARY 300,000원; 2026-09-20 01664 ORDINARY 100,000원; 2026-09-25 01664 CANCEL_RECEIVED -100,000원.
- 현재 주문: 2026-10-15 TELECOM_SKT 50,000원, AUTOPAY. 당월 사용: 0원.
- 후보 기대: 01664 2,500원. 적용 상태: 01664 APPLIED. 기대 카드: `01664`.
- 손계산·판정: 전월 300,000+100,000−100,000=300,000; 통신 2,500. 근거: [NO](https://img2.kbcard.com/obj/card/download/01664__prdctOpmn_20260818.pdf).

### C15. 노리 자동납부 방식 불일치

- 카드: `01664`. 전월 합성 원장: 2026-09-15 01664 ORDINARY 300,000원.
- 현재 주문: 2026-10-15 TELECOM_SKT 50,000원, DIRECT. 당월 사용: 0원.
- 후보 기대: 01664 0원. 적용 상태: 01664 NOT_APPLICABLE_PAYMENT_METHOD. 기대 카드: `01664`.
- 손계산·판정: 건당 금액·실적은 충족하나 자동납부 아님. 근거: [NO](https://img2.kbcard.com/obj/card/download/01664__prdctOpmn_20260818.pdf).

### C16. 첵첵 1구간 CU 정액

- 카드: `01914`. 전월 합성 원장: 2026-09-15 01914 ORDINARY 300,000원.
- 현재 주문: 2026-10-15 CU_STORE 10,000원, DIRECT. 당월 사용: 0원.
- 후보 기대: 01914 1,000원. 적용 상태: 01914 APPLIED. 기대 카드: `01914`.
- 손계산·판정: 전월 300,000, CU 10,000 이상: 1,000. 근거: [CC](https://img2.kbcard.com/obj/card/download/01914__prdctOpmn_20260818.pdf).

### C17. 첵첵 최소금액 미달

- 카드: `01914`. 전월 합성 원장: 2026-09-15 01914 ORDINARY 300,000원.
- 현재 주문: 2026-10-15 CU_STORE 9,999원, DIRECT. 당월 사용: 0원.
- 후보 기대: 01914 0원. 적용 상태: 01914 NOT_APPLICABLE_MIN_AMOUNT. 기대 카드: `01914`.
- 손계산·판정: CU 건당 10,000 미달; 적용 혜택 없음. 근거: [CC](https://img2.kbcard.com/obj/card/download/01914__prdctOpmn_20260818.pdf).

### C18. 첵첵 2구간 CU 정액

- 카드: `01914`. 전월 합성 원장: 2026-09-15 01914 ORDINARY 600,000원.
- 현재 주문: 2026-10-15 CU_STORE 10,000원, DIRECT. 당월 사용: .
- 후보 기대: 01914 2,000원. 적용 상태: 01914 APPLIED. 기대 카드: `01914`.
- 손계산·판정: 전월 600,000 이상, CU 건당 2,000. 근거: [CC](https://img2.kbcard.com/obj/card/download/01914__prdctOpmn_20260818.pdf).

### C19. 기간 경계 및 동률

- 카드: `01914`, `09271`. 전월 합성 원장: 2026-09-30 01914 ORDINARY 300,000원.
- 현재 주문: 2026-10-01 CU_STORE 100,000원, DIRECT. 당월 사용: 0원.
- 후보 기대: 01914 1,000원 / 09271 1,000원. 적용 상태: 01914 APPLIED / 09271 APPLIED. 기대 카드: `01914`.
- 손계산·판정: 9/30 실적 300,000; 10/1 주문: 첵첵 1,000 = 티타늄 1%; 동률 카드 ID 01914 우선. 근거: [CC](https://img2.kbcard.com/obj/card/download/01914__prdctOpmn_20260818.pdf), [TI_BASE](https://img2.kbcard.com/obj/card/download/09271__prdctOpmn_20240417.pdf), [PRD_OPT](../docs/PRD.md#fr-05-결정론적-혜택-계산과-최적화).

### C20. 당장 최대와 월간 최대의 차이

- 카드: `09174`, `09271`. 전월 합성 원장: 2026-09-15 09174 ORDINARY 300,000원.
- 현재 주문: 2026-10-15 STARBUCKS_STORE 10,000원, DIRECT. 예정 소비: 2026-10-20 STARBUCKS_STORE 20,000원, DIRECT. 당월 사용: 0원.
- 후보 기대: 09174 5,000원 / 09271 100원. 적용 상태: 09174 APPLIED / 09271 APPLIED. 월간 총혜택 10,100원. 기대 카드: `09271`.
- 손계산·판정: 즉시 톡톡 5,000>티타늄 100. 현재 티타늄 100+미래 톡톡 10,000=10,100; 현재 톡톡 5,000+미래 톡톡 잔여 5,000=10,000. 근거: [TT](https://img2.kbcard.com/obj/card/download/09174__prdctOpmn_20260714.pdf), [TI_BASE](https://img2.kbcard.com/obj/card/download/09271__prdctOpmn_20240417.pdf), [PRD_OPT](../docs/PRD.md#fr-05-결정론적-혜택-계산과-최적화).

## 입력 변경 시험 10개

각 시험은 기준 C사례를 새로 복제한 뒤 아래 JSON Pointer의 **한 필드만** 바꾼다. 원본 C사례와 다른 M사례에 영향을 주지 않는다. `/usedBenefits/01914:CU_AREA`는 첵첵 CU 영역의 당월 사용액(원)이며 공식 업종코드가 아닌 평가용 키다. 카드 제거 후 남은 비보유 카드의 과거 원장은 비교에서 무시한다.

| ID | 기준 | 변경 차원·패치 | 새 기대 (원) 및 상태 | 손계산·판정 |
| --- | --- | --- | --- | --- |
| M01 | C06 | 주문 금액: `replace /current/amountWon = 10000` | 09174 5,000원; 09174 APPLIED; 카드 09174 | Great 10,000×50%=5,000 |
| M02 | C06 | 주문 날짜: `replace /current/at = "2026-11-01"` | 09174 0원; 09174 NOT_APPLICABLE_PERFORMANCE; 카드 09174 | 11월 주문은 10월 실적을 본다. 완전한 합성 원장에 10월 승인 0원 |
| M03 | C06 | 업종: `replace /current/merchantClass = "ORDINARY_DOMESTIC"` | 09174 0원; 09174 NOT_APPLICABLE_CATEGORY; 카드 09174 | 스타벅스 대상 분류가 아니므로 Great 불적용 |
| M04 | C07 | 결제 방식: `replace /current/paymentMethod = "DIRECT"` | 09174 10,000원; 09174 APPLIED; 카드 09174 | Great는 유지, Check 2,000 제거 |
| M05 | C11 | 원장 거래: `replace /prior/0/amountWon = 199999` | 01664 0원; 01664 NOT_APPLICABLE_PERFORMANCE; 카드 01664 | 생활혜택 전월 20만원 문턱 1원 미달 |
| M06 | C13 | 주문 금액: `replace /current/amountWon = 49999` | 01664 0원; 01664 NOT_APPLICABLE_MIN_AMOUNT; 카드 01664 | 통신 자동납부 건당 5만원 문턱 1원 미달 |
| M07 | C16 | 원장 거래: `replace /prior/0/amountWon = 600000` | 01914 2,000원; 01914 APPLIED; 카드 01914 | 첵첵 2구간 60만원에서 건당 2,000 |
| M08 | C18 | 월 한도 상태: `add /usedBenefits/01914:CU_AREA = 4000` | 01914 0원; 01914 LIMIT_EXHAUSTED; 카드 01914 | 2구간 CU 영역 한도 4,000 전부 소진 |
| M09 | C20 | 예정 소비: `replace /future = []` | 09174 5,000원 / 09271 100원; 09174 APPLIED / 09271 APPLIED; 카드 09174; 월간 5,000 | 예정 소비 없으면 현재 주문 최대인 톡톡 5,000 선택 |
| M10 | C19 | 카드 구성: `replace /cards = ["09271"]` | 09271 1,000원; 09271 APPLIED; 카드 09271 | 첵첵을 보유 카드에서 제거하면 티타늄 1,000만 비교 |

## PRD §6.2 범위 점검

| 요구 범주 | 해당 사례 |
| --- | --- |
| 정상 할인 | C01, C06, C07, C11, C13, C16, C18 |
| 실적 미달·충족, 실적 제외 | C08, C09, C14, C16, C18, M05, M07 |
| 최소 결제금액·업종·결제 방식 | C04, C12, C15, C17, M03, M04, M06 |
| 월 한도 잔여·소진 | C05, C10, M08 |
| 적용 가능한 혜택 없음·동률 | C03, C05, C17, C19 |
| 기준 기간 경계·취소·버전 만료 | C14, C19, C02, M02 |
| 즉시 최대와 월간 최적의 차이 | C20, M09 |

## 확정 전에 남은 일

1. 상품별 공식 약관의 실제 적용 버전·기간과 가맹점 분류, 티타늄 중복·원 미만 처리, 한도 복원 순서를 검증한다.
2. D-06의 교차 검토 방식과 담당을 사용자와 확정하고, 엔진 출력에 의존하지 않은 손계산을 재검토한다.
3. `VERIFIED` 규칙이 생기면 이 JSON을 동일 엔진/API에 연결하고 20+10 결과·근거를 비교한다. 불일치 시 기대값을 엔진에 맞춰 조용히 바꾸지 않는다.
