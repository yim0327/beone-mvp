package com.beone.app.auth

import android.os.SystemClock
import androidx.lifecycle.ViewModel

// 화면 재생성(회전·폴드·테마 변경) 동안 활성 상태와 진행 중인 인증 요청을 유지한다.
class VirtualCardActivationViewModel(
    elapsedRealtimeMillis: () -> Long,
) : ViewModel() {

    constructor() : this(SystemClock::elapsedRealtime)

    private val activation = VirtualCardActivation(elapsedRealtimeMillis)

    val state: ActivationState get() = activation.state

    val isAuthenticating: Boolean get() = activation.isAuthenticating

    fun remainingMillis(): Long = activation.remainingMillis()

    // 인증 창을 띄웠으면 true. 이미 진행 중이거나 기기에서 인증할 수 없으면 false.
    fun requestAuthentication(authenticator: DeviceAuthenticator): Boolean {
        if (!activation.beginAuthentication()) return false
        val blocked = authenticator.availability()
        if (blocked != null) {
            activation.onAuthOutcome(blocked)
            return false
        }
        authenticator.authenticate()
        return true
    }

    fun onAuthOutcome(outcome: AuthOutcome): ActivationState = activation.onAuthOutcome(outcome)

    // 화면 구성 변경으로 멈춘 경우는 앱 이탈로 보지 않는다.
    // 앱 이탈 때 인증 창이 남아 있으면 닫아, 복귀 후 이전 요청의 결과가 쓰이지 않게 한다.
    fun onScreenStopped(isChangingConfigurations: Boolean, authenticator: DeviceAuthenticator) {
        if (isChangingConfigurations) return
        if (activation.onAppLeft()) {
            authenticator.cancel()
        }
    }
}
