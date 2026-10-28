# Android 진행 상황

- 작성: 2026-10-28, 기준 `origin/develop` `cbc50fb`
- 상태 구분: 아래 내용은 모두 **작업 브랜치 `feat/22-android-auth-session` 상태**이며 아직 `develop`에 반영되지 않았습니다.

## 지금 가능한 것

- 네이티브 기기 인증: 지문 또는 기기 PIN을 한 인증 창으로 받습니다(D-08). 인증 수단이 없으면 활성화하지 않고 설정 안내를 합니다.
- 60초 활성 상태: 성공 시 60초 활성, 만료·실패·취소·앱 이탈 시 즉시 비활성, 재인증 시 새 60초. 회전 등 화면 구성 변경은 이탈로 보지 않습니다.
- 확인 수단: 단위 테스트 44건, debug 앱의 **B:ONE 인증 점검** 화면(release 미포함)
- 연결 단계: 네이티브 로직까지만. WebView·React 화면·서버와는 연결되지 않았습니다.

## 아직 안 되는 것

- 실제 기기 확인: [실기기 확인 목록](../../android/device-checklist.md) 17개 항목 모두 미확인
- WebView로 React 화면 표시, 웹으로 인증 상태 전달(브리지)
- `가상 주문서 결제` 버튼과 모의 승인 흐름

## 현재 작업과 PR

- [#22](https://github.com/yim0327/beone-mvp/issues/22) 기기 인증과 60초 활성 상태 — draft [PR #24](https://github.com/yim0327/beone-mvp/pull/24), 독립 리뷰 대기(CodeRabbit 한도), 실기기 확인 대기, 병합 승인 대기 ([단계 기록](22-auth-session.md))
- [#25](https://github.com/yim0327/beone-mvp/issues/25) WebView 셸 기초 — 별도 브랜치에서 진행

## 사용자 확인 사항

1. **release에서 React 화면을 불러오는 방식과 신뢰 origin** — 앱 번들 에셋(`WebViewAssetLoader`, `https://appassets.androidplatform.net`) 또는 원격 주소. 권장안: 앱 에셋 방식(PoC에서 동작 확인, 네트워크 없이 origin 고정 가능). 정해지기 전에는 WebView 작업을 설정값 분리까지만 합니다.
2. **WebView 브리지 계약**(메시지 이름·필드·방향·origin) — 프런트엔드와 함께 정해야 합니다. 정해지기 전에는 `후보` 초안만 둡니다.
3. **지문 불일치 한 번에 즉시 비활성화** — PRD §4.3-4 문구대로 구현했습니다. 인증 창이 열린 채 다시 시도할 수 있습니다. 권장안: 유지.
4. **실기기 확인** — 확인 목록 16·17번은 기기 보안 설정을 바꿔야 하므로 기기 소유자의 판단이 필요합니다.

## 다음 작업

- #22 PR 독립 리뷰 확인과 지적 사항 반영
- #25 WebView 셸 기초 마무리
