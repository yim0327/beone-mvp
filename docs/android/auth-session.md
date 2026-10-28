# Android 기기 인증과 60초 활성 상태

가상카드 화면을 쓰기 전에 기기 인증(지문 또는 기기 PIN)을 받고, 성공하면 60초 동안 `가상카드 사용 준비` 상태를 유지하는 Android 네이티브 기반입니다. 근거는 [PRD §4.3·FR-06](../PRD.md), [결정 D-08](../decisions.md#d-08-확정-정책), [실기기 PoC](../d08-biometrics-poc.md)입니다.

> 이 인증은 **앱 안의 가상 결제 화면을 여는 트리거**입니다. 서버 로그인, 카드 소유 증명, 카드사 결제 인증, 실제 금융거래 승인이 아니며 그렇게 사용하지 않습니다.

## 현재 구현 범위

| 구분 | 상태 |
| --- | --- |
| `BIOMETRIC_STRONG \| DEVICE_CREDENTIAL` 단일 프롬프트 호출 | 구현 |
| 성공·지문 불일치·실패·취소·설정 필요·사용 불가 구분 | 구현, 단위 테스트 |
| 60초 활성화, 만료, 재인증 시 새 60초, 실패·취소 즉시 비활성화 | 구현, 단위 테스트 |
| 앱 이탈 즉시 비활성화, 화면 구성 변경은 이탈로 보지 않음 | 구현, 단위 테스트 |
| 중복 인증 요청 방지, 늦게 도착한 결과·앱 이탈 뒤 결과 무시 | 구현, 단위 테스트 |
| debug 전용 실기기 점검 화면 | 구현 (release 미포함) |
| 실제 기기에서의 동작 확인 | 1~15번 통과(2026-10-29, SM-S942N Android 17). 16·17번(기기 보안 설정 변경 필요)은 **미완료** — [실기기 확인 목록](device-checklist.md) |
| WebView 화면 연결, 웹에 상태 전달(브리지) | **미구현** — 로딩 방식·신뢰 origin·브리지 계약 미확정 |

## 용어

- **기기 인증:** Android가 제공하는 지문(생체) 또는 화면 잠금 PIN·패턴·비밀번호(기기 자격 증명, `DEVICE_CREDENTIAL`) 확인입니다. 앱은 생체 정보나 PIN을 직접 받지 않고 성공 여부만 받습니다.
- **단조 시계:** 기기 부팅 후 흐른 시간(`SystemClock.elapsedRealtime`)입니다. 사용자가 기기 시각을 바꿔도 영향을 받지 않아 60초 계산에 씁니다.
- **화면 구성 변경:** 회전, 폴드 펼침·접힘, 다크 모드 전환처럼 Activity가 다시 만들어지지만 앱을 벗어난 것은 아닌 경우입니다.

## 패키지 구조

`android/app/src/main/java/com/beone/app/auth/`

| 파일 | 역할 |
| --- | --- |
| [AuthOutcome.kt](../../android/app/src/main/java/com/beone/app/auth/AuthOutcome.kt) | 인증 결과 종류와 인증 수단(`BIOMETRIC`·`DEVICE_CREDENTIAL`) |
| [VirtualCardActivation.kt](../../android/app/src/main/java/com/beone/app/auth/VirtualCardActivation.kt) | 60초 활성 상태 계산. Android에 의존하지 않는 순수 로직 |
| [DeviceAuthenticator.kt](../../android/app/src/main/java/com/beone/app/auth/DeviceAuthenticator.kt) | 인증 창을 띄우는 경계(인터페이스). 테스트에서는 가짜 구현으로 바꿉니다 |
| [BiometricDeviceAuthenticator.kt](../../android/app/src/main/java/com/beone/app/auth/BiometricDeviceAuthenticator.kt) | `androidx.biometric`의 `BiometricPrompt`를 쓰는 실제 구현 |
| [BiometricResultMapper.kt](../../android/app/src/main/java/com/beone/app/auth/BiometricResultMapper.kt) | `BiometricManager`·`BiometricPrompt` 코드 → `AuthOutcome` 변환 |
| [VirtualCardActivationViewModel.kt](../../android/app/src/main/java/com/beone/app/auth/VirtualCardActivationViewModel.kt) | 화면 재생성 동안 상태 유지, 인증 요청 순서, 화면 정지의 이탈 여부 판정 |

debug 전용: [DeviceAuthCheckActivity.kt](../../android/app/src/debug/java/com/beone/app/auth/DeviceAuthCheckActivity.kt) — 실기기 확인용 점검 화면입니다. 제품 화면이 아니며 `src/debug`에만 있어 release APK에 들어가지 않습니다.

## 처리 흐름

```text
화면 진입 또는 [기기 인증] 버튼
  → ViewModel.requestAuthentication(authenticator)
      ├─ 이미 인증 중 → 무시(중복 요청 방지)
      ├─ canAuthenticate 결과가 설정 필요/사용 불가 → 창을 띄우지 않고 비활성 + 안내
      └─ 인증 창 표시(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
  → 콜백 결과 → ViewModel.onAuthOutcome
      ├─ 성공 → 지금부터 60초 활성(남은 시간을 이어 붙이지 않음)
      ├─ 지문 불일치 → 즉시 비활성, 창은 열린 채 재시도 가능
      └─ 실패·취소·설정 필요·사용 불가 → 즉시 비활성 + 재인증/설정 안내
Activity.onStop
  ├─ isChangingConfigurations = true(회전 등) → 상태 유지
  └─ false(홈 이동, 다른 앱 전환 등) → 즉시 비활성(LEFT_APP), 떠 있는 인증 창은 닫고
     이후 도착하는 결과는 무시, 복귀 시 재인증 안내
```

실기기(SM-S942N, Android 17)에서 인증 창이 떠 있는 채로 홈으로 나가자 비활성화 이유가 `LEFT_APP`이 아니라 `AUTH_CANCELED`로 남았습니다. 시스템의 취소 결과가 `onStop`보다 먼저 처리된 것으로 봅니다(`onStop` 뒤 결과는 무시되므로). 비활성 상태, 인증 창 닫힘, 재인증 안내는 같습니다. `onStop` 뒤 늦게 온 성공을 무시하는 동작은 그대로이며 단위 테스트로 확인합니다([확인 목록](device-checklist.md) 15번).

결과 코드 분류(`BiometricResultMapper`):

| 결과 | 코드 |
| --- | --- |
| 취소 | `ERROR_USER_CANCELED`, `ERROR_CANCELED`, `ERROR_NEGATIVE_BUTTON` |
| 설정 필요 | `canAuthenticate`의 `BIOMETRIC_ERROR_NONE_ENROLLED`, `ERROR_NO_DEVICE_CREDENTIAL`, `ERROR_NO_BIOMETRICS` |
| 인증 시도 | `canAuthenticate`의 `BIOMETRIC_SUCCESS`, `BIOMETRIC_STATUS_UNKNOWN`(알 수 없으면 시도해 보고 콜백으로 판단) |
| 사용 불가 | 그 밖의 `canAuthenticate` 오류, `ERROR_HW_UNAVAILABLE`, `ERROR_HW_NOT_PRESENT`, `ERROR_SECURITY_UPDATE_REQUIRED` |
| 실패 | 잠김(`ERROR_LOCKOUT`·`_PERMANENT`), 시간 초과 등 나머지 오류 |

## 사용 예

```kotlin
// Activity.onCreate — 재생성마다 authenticator를 새로 만들어야 진행 중 결과를 이어 받습니다.
viewModel = ViewModelProvider(this)[VirtualCardActivationViewModel::class.java]
authenticator = BiometricDeviceAuthenticator(this) { outcome -> viewModel.onAuthOutcome(outcome) }
viewModel.requestAuthentication(authenticator)

// Activity.onStop
viewModel.onScreenStopped(isChangingConfigurations, authenticator)

// 화면 갱신: 상태는 읽을 때 만료를 다시 계산합니다.
val active = viewModel.state is ActivationState.Active
val remaining = viewModel.remainingMillis()
```

## 구현 중 정한 작은 결정

운영 규칙상 구현 세부 결정이며, 실기기 확인 결과에 따라 바뀔 수 있습니다.

- 60초 경계: 성공 시각 + 60,000ms가 되는 순간 만료입니다(59,999ms까지 활성).
- 지문 불일치 한 번도 "실패"로 보아 활성 중이면 즉시 비활성화합니다. 인증 창은 열려 있으므로 그대로 재시도해 성공하면 새 60초가 시작됩니다.
- 앱 이탈 시 진행 중인 인증 요청도 끝내고 인증 창을 닫습니다. 이탈 뒤 늦게 도착한 성공이 복귀 시 바로 활성화하지 않게 하고, 결과가 오지 않아 중복 방지 잠금이 풀리지 않는 상황도 막기 위해서입니다. 기기 PIN 입력은 시스템 인증 창 안에서 진행돼 `onPause`·`onStop`이 생기지 않는 것을 실기기(SM-S942N, Android 17)에서 확인했습니다([확인 목록](device-checklist.md) 8번).
- 화면 재생성 처리는 PoC의 `configChanges` 방식 대신 ViewModel + `isChangingConfigurations`를 씁니다. WebView 화면에서 `configChanges`가 필요한지는 WebView 작업에서 따로 판단합니다.

## 의존성

| 라이브러리 | 버전 | 출처 |
| --- | --- | --- |
| `androidx.biometric:biometric` | 1.1.0 (stable) | [Google Maven 메타데이터](https://dl.google.com/android/maven2/androidx/biometric/biometric/maven-metadata.xml), 2026-10-28 확인. 이후 버전은 1.2.0·1.4.0 alpha뿐 |

## 아직 하지 않은 것

- WebView에 React 화면 표시, 웹으로 인증 상태 전달. release 로딩 방식(앱 에셋 또는 원격 주소), 신뢰 origin, 브리지 메시지 계약이 정해지지 않아 구현하지 않았습니다.
- 실제 기기 확인 중 16·17번(지문 없이 PIN만, 화면 잠금 없음). 기기 보안 설정을 바꿔야 해 [실기기 확인 목록](device-checklist.md)에 남아 있으며 단위 테스트·에뮬레이터 결과로 대신하지 않습니다.
- 계측 테스트(`androidTest`). 인증 창은 시스템 UI라 자동화가 제한적이어서 실기기 수동 확인으로 둡니다.
