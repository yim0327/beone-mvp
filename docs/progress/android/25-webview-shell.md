# #25 Android WebView 셸 기초

- 이슈: [#25](https://github.com/yim0327/beone-mvp/issues/25)
- 브랜치: `feat/25-android-webview-shell` (기준 `origin/develop` `cbc50fb`, #22와 독립)
- 기술 설명: [Android WebView 셸](../../android/webview-shell.md)
- 진행 기록 README는 #22 PR(#24)에 있습니다. 같은 파일을 두 PR이 새로 만들면 충돌하므로, #24가 병합된 뒤 이 작업의 줄을 반영합니다.

### 2026-10-28 20:05 · 신뢰 origin 판정 · 완료
- 한 일: 주소의 스킴·호스트·포트를 비교해 WebView 안에서 열어도 되는지 판정하는 기능을 만들었습니다.
- 서비스에서 달라진 점: 설정된 시작 주소와 같은 origin만 열 수 있고, 설정이 없으면 아무 주소도 열지 않습니다(화면 연결 전).
- 검증: `./gradlew testDebugUnitTest` 실제 실행, 통과(기준 `cbc50fb` + 미커밋 변경)
- 다음: WebView 화면 연결

### 2026-10-28 20:12 · WebView 셸과 debug/release 분리 · 완료
- 한 일: 앱 첫 화면을 WebView로 바꾸고, debug는 개발 서버를, release는 아무것도 열지 않도록 나눴습니다.
- 서비스에서 달라진 점: debug 앱이 `http://localhost:5173/`의 React 개발 화면을 엽니다. release 앱은 "웹 화면 주소가 설정되지 않았습니다"를 보여줍니다. 원격 디버깅과 평문 HTTP는 debug의 로컬 주소에서만 됩니다.
- 검증: `assembleDebug testDebugUnitTest lintDebug assembleRelease lintRelease` 실제 실행, 통과. release의 `BuildConfig`·네트워크 보안 설정·dex에 개발 서버 주소가 없음을 확인(기준 `cbc50fb` + 미커밋 변경)
- 참고: AGP 9 기본 설정에는 release 단위 테스트 태스크가 없어, release 값은 빌드 산출물로 확인했습니다.
- 다음: 내부 리뷰

### 2026-10-28 20:20 · 내부 리뷰 반영 · 완료
- 한 일: 읽기 전용 하위 에이전트의 내부 리뷰(독립 리뷰 아님) 지적을 확인하고 고쳤습니다.
- 서비스에서 달라진 점: POST 폼 제출로 다른 origin에 가는 경우도 막습니다. 키보드가 입력란을 가리지 않습니다. 첫 화면에서 뒤로 가기 후 돌아와도 웹 뒤로 가기가 동작합니다.
- 반영하지 않은 의견: iframe·하위 리소스 origin 제한과 `about:blank`·`blob:` 처리는 브리지·화면 요구가 정해질 때 판단하도록 문서에 남겼습니다.
- 검증: 위 5개 태스크 실제 실행, 통과, lint 경고 8건 모두 기존 항목. `-Pbeone.devServerUrl`에 따옴표가 있어도 빌드됨을 확인(기준 `cbc50fb` + 미커밋 변경)
- 남은 제한: 실기기·계측 테스트 미실행. release 로딩 방식·신뢰 origin·브리지는 사용자 결정 대기
- 다음: 커밋, PR
