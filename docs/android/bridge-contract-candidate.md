# Android WebView 브리지 계약 초안 (후보)

> **상태: 후보.** 확정된 계약이 아닙니다. 메시지 이름·필드·방향·origin은 사용자와 프런트엔드가 함께 정해야 하며, 정해지기 전에는 Android·React 어느 쪽도 이 초안을 공개 계약으로 구현하지 않습니다([운영 규칙 2절](../development/autonomous-workflow.md), [Android 시작 프롬프트 작업 대기열 4번](../development/android-start.md)).

근거: [PRD §4.3·FR-06](../PRD.md), [D-08](../decisions.md#d-08-확정-정책), [실기기 PoC](../d08-biometrics-poc.md)(PoC에서 `addWebMessageListener`로 성공·취소·불일치·잠금 상태 전달과 복귀 시 동기화를 확인).

## 1. 지키려는 원칙

- **네이티브가 기준입니다.** 60초 활성 여부와 남은 시간은 Android가 단조 시계로 판단합니다. 웹의 카운트다운은 표시용입니다. 웹이 "활성"이라고 주장해도 네이티브 상태를 바꾸지 않습니다.
- **필요한 값만 보냅니다.** 생체 정보, PIN, 시스템 오류 메시지 원문, 기기 식별자는 보내지 않습니다. 인증 수단은 `BIOMETRIC`·`DEVICE_CREDENTIAL` 구분까지만 보냅니다.
- **트리거일 뿐입니다.** 인증 결과는 앱 안의 가상 결제 화면을 여는 데만 씁니다. 웹이 이 값을 서버에 결제 승인 근거로 보내지 않고, 서버도 신뢰하지 않습니다(PRD FR-06).
- **신뢰 origin에만 노출합니다.** 브리지는 신뢰 origin 한 곳에만 등록합니다. 신뢰 origin은 release 로딩 방식 결정(#25)에 따릅니다.

## 2. 전달 방식 후보

| 후보 | 내용 | 평가 |
| --- | --- | --- |
| A (권장) | AndroidX WebKit `WebViewCompat.addWebMessageListener`에 허용 origin 규칙을 지정 | origin을 플랫폼이 검사합니다. PoC에서 동작을 확인했습니다. `androidx.webkit` 의존성과 WebView 기능 지원 확인이 필요합니다 |
| B | `addJavascriptInterface` | 모든 프레임·origin에 노출되기 쉬워 권장하지 않습니다 |

## 3. 메시지 초안

모든 메시지는 JSON 문자열이고 `v`(계약 버전)와 `type`을 가집니다. 모르는 `type`이나 다른 `v`는 무시하고, 네이티브는 웹으로 `error`를 보냅니다.

### 웹 → 네이티브

| type | 필드 | 의미 |
| --- | --- | --- |
| `auth.request` | 없음 | 기기 인증 창을 띄워 달라는 요청. 이미 인증 중이면 네이티브가 무시하고 현재 상태를 다시 보냅니다 |
| `state.get` | 없음 | 현재 상태를 다시 보내 달라는 요청(화면 진입·복귀 시) |
| `payment.check` | 없음 | `가상 주문서 결제` 버튼을 누르기 직전, 지금 활성인지 네이티브에 다시 묻는 요청 |

### 네이티브 → 웹

| type | 필드 | 의미 |
| --- | --- | --- |
| `state` | `active: boolean`, `remainingMs: number`, `reason: string`, `method?: string`, `authenticating: boolean` | 현재 상태. 상태가 바뀔 때마다, 그리고 `state.get` 응답으로 보냅니다 |
| `payment.check.result` | `allowed: boolean`, `reason: string` | `payment.check`의 응답. `allowed=false`면 웹은 결제 요청을 보내지 않고 재인증을 안내합니다 |
| `error` | `code: string` | 잘못된 메시지 등 브리지 자체 오류 |

`reason` 값 후보(현재 #22 구현의 `InactiveReason`과 같음): `NOT_AUTHENTICATED`, `EXPIRED`, `AUTH_FAILED`, `AUTH_CANCELED`, `LEFT_APP`, `SETUP_REQUIRED`, `UNAVAILABLE`. 활성 상태면 `ACTIVE`.

예시:

```json
{"v":1,"type":"state","active":true,"remainingMs":60000,"reason":"ACTIVE","method":"BIOMETRIC","authenticating":false}
```

```json
{"v":1,"type":"state","active":false,"remainingMs":0,"reason":"LEFT_APP","authenticating":false}
```

## 4. 흐름 초안

```text
가상카드 화면 진입 → 웹: state.get → 네이티브: state(비활성) → 웹: auth.request
→ 네이티브: 인증 창 → 성공 → state(active, 60000)
→ 웹: 버튼 활성 + 표시용 카운트다운
→ 버튼 누름 → 웹: payment.check → 네이티브: payment.check.result(allowed)
→ 웹 → 서버: 모의 승인 요청 (서버는 인증 결과를 신뢰 근거로 쓰지 않음)

만료·실패·취소·앱 이탈 → 네이티브: state(비활성, reason) → 웹: 버튼 비활성 + 재인증 안내
앱 복귀 → 웹: state.get (또는 네이티브가 먼저 state 전송)
```

## 5. 결정이 필요한 질문

| # | 질문 | 권장안 |
| --- | --- | --- |
| B1 | 전달 방식 | 후보 A(`addWebMessageListener` + origin 규칙) |
| B2 | 위 메시지 이름·필드 | 위 초안을 출발점으로 프런트엔드와 확정 |
| B3 | 화면 진입 시 인증 창을 누가 띄우는가 | 웹이 `auth.request`를 보냄(PRD §4.3-1 "화면을 열면 요청"을 웹 화면 기준으로 판단) |
| B4 | `payment.check`가 필요한가 | 필요. 표시용 카운트다운과 실제 만료 사이의 차이를 막기 위해 |
| B5 | 상태 전송 빈도 | 상태 변화 시에만 전송, 남은 시간은 웹이 `remainingMs`로 계산해 표시 |
| B6 | 신뢰 origin | #25의 release 로딩 방식 결정에 따름 |

## 6. 구현 전 확인할 것

- `androidx.webkit` 안정판 버전과 `WebViewFeature.WEB_MESSAGE_LISTENER` 지원 확인(실기기 WebView 154에서는 PoC로 확인)
- iframe 등 하위 프레임에는 노출하지 않는지
- 메시지 크기·형식 검증과 실패 시 `error` 응답
