# #22 Android 기기 인증과 60초 활성 상태

- 이슈: [#22](https://github.com/yim0327/beone-mvp/issues/22)
- 브랜치: `feat/22-android-auth-session` (기준 `origin/develop` `cbc50fb`)
- 기술 설명: [기기 인증과 60초 활성 상태](../../android/auth-session.md), [실기기 확인 목록](../../android/device-checklist.md)

### 2026-10-28 19:30 · 시작 점검 · 완료
- 한 일: 최신 `origin/develop`(`cbc50fb`)과 기준 문서 5개를 바이트 단위로 비교해 모두 일치함을 확인했습니다.
- 서비스에서 달라진 점: 없음. 기존 Android 빌드가 통과하는 것을 먼저 확인했습니다.
- 검증: `./gradlew assembleDebug testDebugUnitTest lintDebug` 실제 실행, 통과(기준 `cbc50fb`, 미커밋 변경 없음)
- 참고: 점검 스크립트의 임시 폴더 삭제(`rm -rf`)가 작업 환경 규칙으로 막혀, 같은 비교를 세션 임시 파일 하나로 대신 실행했습니다.
- 다음: 60초 상태 로직

### 2026-10-28 19:36 · 60초 활성 상태 로직 · 완료
- 한 일: 인증 결과에 따라 60초 동안 켜지고, 만료·실패·취소·앱 이탈 때 꺼지는 상태 계산을 만들었습니다.
- 서비스에서 달라진 점: 재인증하면 남은 시간을 더하지 않고 새로 60초가 시작됩니다. 인증 창이 떠 있는 동안 중복 요청을 막습니다(화면에는 아직 연결 전).
- 검증: `./gradlew testDebugUnitTest` 실제 실행, 통과 20건(기준 `cbc50fb` + 미커밋 변경)
- 작은 결정: 60,000ms가 되는 순간 만료. 지문 불일치 한 번도 실패로 보아 즉시 비활성화합니다.
- 다음: 실제 인증 창 연결

### 2026-10-28 19:40 · 기기 인증 창 연결과 점검 화면 · 완료
- 한 일: 지문 또는 기기 PIN을 한 창에서 받는 Android 인증 창(`BIOMETRIC_STRONG | DEVICE_CREDENTIAL`)을 연결했습니다. 인증 수단이 없으면 창을 띄우지 않고 설정 안내를 보여줍니다.
- 서비스에서 달라진 점: debug 앱에 **B:ONE 인증 점검** 화면이 생겨 실기기에서 60초 흐름을 확인할 수 있습니다. release 앱에는 들어가지 않습니다.
- 검증: `./gradlew assembleDebug testDebugUnitTest lintDebug` 실제 실행, 통과 38건, lint 새 경고 없음. `./gradlew assembleRelease lintRelease` 실제 실행, 통과, release manifest에 점검 화면 없음(기준 `cbc50fb` + 미커밋 변경)
- 남은 제한: 실제 기기 확인 전입니다. WebView·웹 연결은 하지 않았습니다.
- 의존성: `androidx.biometric` 1.1.0(최신 stable, Google Maven 2026-10-28 확인)
- 다음: 내부 리뷰 반영

### 2026-10-28 19:50 · 내부 리뷰 반영 · 완료
- 한 일: 읽기 전용 하위 에이전트의 내부 리뷰(독립 리뷰 아님) 지적을 코드로 확인하고 고쳤습니다.
- 서비스에서 달라진 점: 인증 창이 떠 있을 때 앱을 벗어나면 창을 닫고, 그 뒤 늦게 온 성공으로는 활성화하지 않습니다. 남은 시간이 음수로 보이지 않습니다. 기기 상태를 알 수 없을 때는 인증을 시도해 봅니다.
- 반영하지 않은 의견: 지문 불일치 한 번에 즉시 비활성화하는 동작은 PRD §4.3-4 문구대로 유지하고 사용자 확인 사항에 올렸습니다.
- 검증: `./gradlew assembleDebug testDebugUnitTest lintDebug` 실제 실행, 통과 44건, lint 경고 8건 모두 기존 항목(기준 `cbc50fb` + 미커밋 변경)
- 남은 제한: 기기 PIN 입력이 `onStop`을 일으키는 기기라면 PIN 입력이 끊깁니다. 실기기 확인 목록 8번에서 확인합니다.
- 다음: 커밋, PR

### 2026-10-28 19:58 · PR과 독립 리뷰 요청 · 리뷰 대기
- 한 일: 모듈 전체 검증 뒤 push하고 draft PR #24를 열었습니다. CodeRabbit 전체 리뷰를 한 번 요청했습니다.
- 서비스에서 달라진 점: 없음(`develop` 미반영, 병합은 사용자 승인 대기)
- 검증: `assembleDebug testDebugUnitTest lintDebug --rerun-tasks`, `assembleRelease lintRelease --rerun-tasks` 실제 실행, 통과(기준 `0752331`, 미커밋 변경 없음)
- 남은 제한: CodeRabbit 무료 한도로 리뷰가 시작되지 않아 리뷰 대기입니다. 실기기 확인도 대기입니다.
- 다음: 독립 작업 #25 WebView 셸 기초

### 2026-10-28 20:50 · 병합 준비 점검 · 리뷰 대기
- 한 일: 새 작업을 멈추고 PR #24의 상태를 다시 확인했습니다. 마지막 검증 뒤 바뀐 것은 진행 기록 문서뿐입니다.
- 서비스에서 달라진 점: 없음
- 검증: `assembleDebug testDebugUnitTest lintDebug --rerun-tasks`, `assembleRelease lintRelease --rerun-tasks` 실제 실행, 통과 44건(기준 `1a7d87d`, 미커밋 변경 없음)
- 남은 제한: 독립 리뷰 미시작(CodeRabbit 한도), 실기기 확인 대기, 병합 승인 대기
- 다음: 독립 리뷰가 오면 의견 확인·반영

### 2026-10-29 00:06 · 실기기 확인 · 1~15번 통과, 16·17번 대기
- 한 일: 자동 검증을 다시 실행한 뒤, 사용자가 무선 디버깅으로 연결하고 설치·실행을 승인한 SM-S942N(이번 확인 기기: Android 17, 보안 패치 2026-09-05)에 debug 앱을 설치했습니다. 사용자가 조작하는 동안 화면 문구와 Activity 생명주기를 기록해 [실기기 확인 목록](../../android/device-checklist.md) 1~15번을 대조했습니다.
- 서비스에서 달라진 점: 없음(코드 변경 없음)
- 검증: `assembleDebug testDebugUnitTest lintDebug --rerun-tasks`, `assembleRelease lintRelease --rerun-tasks` 실제 실행, 통과 44건, lint 오류 0건·경고 8건 모두 기존 항목, release APK에 점검 화면 없음(기준 `0b1cc3f`, 미커밋 변경 없음). 실기기 1~15번 통과, 앱 crash·ANR 없음.
- 확인된 점: PIN 입력 중 `onPause`·`onStop`이 생기지 않아 위 "남은 제한"(PIN 입력이 끊길 가능성)은 이 기기에서 해당하지 않습니다.
- 정정: 15번은 복귀 문구가 `인증이 취소되어 비활성화`로 나왔습니다. 취소 결과가 `onStop`보다 먼저 처리된 것으로 판단합니다(오류 코드·도착 시각은 기록하지 않음). 앱 이탈 뒤 비활성·인증 창 종료·복귀 뒤 재인증은 실기기에서 확인했으므로 코드는 유지하고 기대 문구만 정정했습니다(사용자 결정).
- 남은 제한: 16·17번은 기기 보안 설정 변경이 필요해 실행하지 않았고 기기 소유자 판단 대기입니다. 앱 이탈 뒤 늦게 온 성공의 무시는 실기기에서 재현되지 않아 단위 테스트로만 확인했습니다. 이번 결과는 Android 17 기준이며 D-08·과거 PoC의 Android 16 기록과 별개입니다. 독립 리뷰는 아직 없습니다.
- 다음: 16·17번 진행 여부 결정, 독립 리뷰 확인, 병합 승인 대기
