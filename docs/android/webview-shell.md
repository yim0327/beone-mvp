# Android WebView 셸

React 화면을 Android 앱 안에서 보여줄 WebView의 기본 구조입니다. 근거는 [PRD §2.3·FR-06](../PRD.md), [Android 시작 프롬프트 작업 대기열 3번](../development/android-start.md), [실기기 PoC 구현 참고](../d08-biometrics-poc.md#5-구현-참고-결정-아님)입니다.

## 현재 구현 범위

| 구분 | 상태 |
| --- | --- |
| 시작 주소·신뢰 origin을 빌드 설정값(`BuildConfig.WEB_START_URL`)으로 분리 | 구현 |
| 신뢰 origin 밖 탐색 차단, 웹 이외 스킴 차단 | 구현, 단위 테스트 |
| SSL 오류 시 연결 취소 | 구현 |
| debug만 원격 디버깅·로컬 개발 서버 평문 HTTP 허용 | 구현, 빌드 산출물로 확인 |
| 시스템 바·디스플레이 컷아웃 inset 적용 | 구현 |
| release 로딩 방식(앱 에셋 또는 원격 주소)과 신뢰 origin | **미확정** — release는 아무 페이지도 불러오지 않음 |
| 웹 ↔ 네이티브 브리지 | **미구현** — 계약 미확정. 어떤 origin에도 노출하지 않음 |
| 기기 인증(#22)과의 연결 | 미구현 |
| 실기기·계측 테스트 | 미실행 |

## 빌드별 설정

| 항목 | debug | release |
| --- | --- | --- |
| `WEB_START_URL` | `http://localhost:5173/` (Gradle 속성 `beone.devServerUrl`로 변경) | `""` (설정 없음 → "웹 화면 주소가 설정되지 않았습니다") |
| 평문 HTTP | `localhost`, `127.0.0.1`, `10.0.2.2`만 허용 | 허용하지 않음 |
| 원격 디버깅(`chrome://inspect`) | 켬 | 끔 |

- 네트워크 보안 설정: [main](../../android/app/src/main/res/xml/network_security_config.xml)(release 기본값)과 [debug](../../android/app/src/debug/res/xml/network_security_config.xml)(같은 이름의 리소스로 덮어씀)
- 실기기에서 개발 서버를 볼 때: PC에서 `npm run dev`를 실행한 뒤 `adb reverse tcp:5173 tcp:5173`으로 연결합니다. 이 저장소의 Android 작업선은 프런트엔드를 실행·빌드하지 않습니다.
- LAN IP 주소(예: `http://192.168.0.10:5173`)는 debug 평문 허용 목록에 없어 연결되지 않습니다. 필요하면 debug 네트워크 보안 설정에 추가해야 합니다.

## 패키지 구조

| 파일 | 역할 |
| --- | --- |
| [WebOrigin.kt](../../android/app/src/main/java/com/beone/app/web/WebOrigin.kt) | 주소에서 스킴·호스트·포트를 뽑아 비교. http·https만 받고 기본 포트(443·80)는 같게 봅니다 |
| [WebNavigationPolicy.kt](../../android/app/src/main/java/com/beone/app/web/WebNavigationPolicy.kt) | 시작 주소의 origin과 같은 주소만 허용. 설정이 없거나 잘못되면 아무것도 허용하지 않습니다 |
| [RestrictedWebViewClient.kt](../../android/app/src/main/java/com/beone/app/web/RestrictedWebViewClient.kt) | 메인 프레임 탐색 차단(링크·리디렉션·POST), SSL 오류 취소, 뒤로 가기 가능 여부 전달 |
| [MainActivity.kt](../../android/app/src/main/java/com/beone/app/MainActivity.kt) | WebView 생성·설정, 시스템 바·키보드 inset, 웹 기록이 있을 때만 웹 뒤로 가기, 상태 저장 |

WebView 설정: JavaScript·DOM storage 켬(React 실행에 필요), 파일·콘텐츠 접근 끔, 새 창 열기 끔, 위치 정보 끔, HTTPS 페이지 안의 HTTP 리소스(mixed content) 차단.

## 처리 흐름

```text
MainActivity.onCreate
  → WebNavigationPolicy(BuildConfig.WEB_START_URL)
      ├─ 설정 없음/잘못됨 → WebView에 아무것도 불러오지 않고 안내 문구
      └─ 시작 주소 불러오기(화면 재생성 시 저장된 상태 복원)
페이지 안 링크·리디렉션 → shouldOverrideUrlLoading
  ├─ 신뢰 origin과 같음 → WebView에서 계속
  └─ 다름 → 열지 않음(메인 프레임이면 안내 문구). 외부 브라우저로 넘기지도 않음
메인 프레임 페이지 시작 → onPageStarted (POST 폼 제출처럼 위 단계를 거치지 않는 탐색 대비)
  ├─ 신뢰 origin과 같음 → 안내 문구 숨김
  └─ 다름 → 불러오기 중지 + 안내 문구
SSL 오류 → 연결 취소
```

## 후보 (결정 아님)

release 로딩 방식이 정해지면 아래 중 하나로 `WEB_START_URL`과 신뢰 origin을 정합니다. 사용자 결정 전에는 구현하지 않습니다.

- **후보 A — 앱 에셋:** React 빌드 결과를 APK에 넣고 `WebViewAssetLoader`로 `https://appassets.androidplatform.net/` 아래에서 제공합니다. PoC에서 이 origin으로 제한한 `addWebMessageListener` 브리지가 동작했습니다. 네트워크 없이 열리고 origin이 고정되지만, 프런트엔드 빌드 산출물을 앱 빌드에 넣는 절차가 필요합니다.
- **후보 B — 원격 주소:** 배포된 HTTPS 주소를 엽니다. 웹을 따로 배포할 수 있지만 네트워크가 필요하고, 배포 주소와 인증서를 정해야 합니다.

## 실기기에서 확인할 항목

단위 테스트와 빌드 산출물 확인으로 대신하지 않습니다. 기기 연결과 설치는 사용자가 직접 합니다.

| # | 확인 항목 | 기대 결과 | 상태 |
| --- | --- | --- | --- |
| W1 | debug 앱 + `adb reverse`로 개발 서버 접속 | React 개발 화면이 보이고 상태 바·내비게이션 바에 가려지지 않음 | 미확인 |
| W2 | 화면 회전 | 화면이 다시 처음부터 불러와지지 않고 이어짐 | 미확인 |
| W3 | 다른 origin으로 가는 링크 | 열리지 않고 안내 문구 | 미확인 |
| W3a | 입력란을 눌러 키보드 표시 | 입력란이 키보드에 가려지지 않음 | 미확인 |
| W3b | 웹 안에서 페이지 이동 후 뒤로 가기, 첫 화면에서 뒤로 가기 후 앱 복귀, 다시 이동·뒤로 가기 | 웹 기록이 있으면 웹 이전 페이지, 없으면 앱이 백그라운드로. 복귀 후에도 웹 뒤로 가기가 동작 | 미확인 |
| W4 | release 앱 실행 | "웹 화면 주소가 설정되지 않았습니다" | 미확인 |
| W5 | debug 앱에서 `chrome://inspect` | WebView가 보임. release 앱은 보이지 않음 | 미확인 |

## 아직 하지 않은 것

- release 로딩 방식·신뢰 origin 결정과 그에 맞춘 구현
- 브리지(메시지 이름·필드·방향·origin 계약)와 기기 인증 상태 전달
- 계측 테스트(`androidTest`의 테스트용 HTML). 승인된 기기·에뮬레이터가 없어 이번에는 작성·실행하지 않았습니다.
- 파일 업로드·다운로드·권한 요청 처리. 현재 화면 요구사항에 없어 기본 동작(처리 안 함)으로 둡니다.
- iframe과 이미지·스크립트·fetch 같은 하위 리소스의 origin 제한. 이번에는 메인 프레임 탐색만 제한합니다. 브리지를 노출하지 않으므로 지금 위험은 낮지만, 브리지 도입 전에 `shouldInterceptRequest`나 CSP로 제한할지 정합니다.
- `about:blank`·`blob:` 주소의 메인 프레임 탐색은 신뢰 origin이 아니므로 막습니다. React 화면이 이 방식을 쓰게 되면 다시 판단합니다.
