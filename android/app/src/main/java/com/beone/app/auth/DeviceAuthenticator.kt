package com.beone.app.auth

// 기기 인증 창을 띄우는 경계. 실제 구현과 테스트용 대체 구현을 분리한다.
interface DeviceAuthenticator {

    // 인증을 시작할 수 없으면 그 이유를, 시작할 수 있으면 null을 돌려준다.
    fun availability(): AuthOutcome?

    // 인증 창을 띄운다. 결과는 생성 시 받은 콜백으로 전달된다.
    fun authenticate()

    // 떠 있는 인증 창을 닫는다. 이후 도착하는 취소 결과는 무시된다.
    fun cancel()
}
