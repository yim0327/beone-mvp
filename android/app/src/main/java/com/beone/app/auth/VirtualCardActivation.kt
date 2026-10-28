package com.beone.app.auth

// 가상카드 화면의 60초 활성 상태. 앱 내 가상 결제 화면을 여는 트리거일 뿐 결제 승인 근거가 아니다.
// 시간은 단조 시계(SystemClock.elapsedRealtime)를 주입받아 기기 시간 변경의 영향을 받지 않는다.
class VirtualCardActivation(
    private val elapsedRealtimeMillis: () -> Long,
) {

    private var active: ActivationState.Active? = null
    private var lastInactiveReason: InactiveReason = InactiveReason.NOT_AUTHENTICATED

    var isAuthenticating: Boolean = false
        private set

    val state: ActivationState
        get() {
            val current = active ?: return ActivationState.Inactive(lastInactiveReason)
            if (elapsedRealtimeMillis() >= current.expiresAtMillis) {
                deactivate(InactiveReason.EXPIRED)
                return ActivationState.Inactive(InactiveReason.EXPIRED)
            }
            return current
        }

    fun remainingMillis(): Long {
        val current = state as? ActivationState.Active ?: return 0L
        return (current.expiresAtMillis - elapsedRealtimeMillis()).coerceAtLeast(0L)
    }

    // 인증 창을 띄우기 전에 호출한다. 이미 진행 중이면 false를 돌려 중복 요청을 막는다.
    fun beginAuthentication(): Boolean {
        if (isAuthenticating) return false
        isAuthenticating = true
        return true
    }

    // 진행 중인 요청이 없을 때 도착한 결과는 무시한다.
    fun onAuthOutcome(outcome: AuthOutcome): ActivationState {
        if (!isAuthenticating) return state
        if (outcome.isTerminal) {
            isAuthenticating = false
        }
        when (outcome) {
            is AuthOutcome.Succeeded -> {
                // 활성 중 재인증에 성공해도 남은 시간을 더하지 않고 새로 60초를 시작한다.
                active = ActivationState.Active(
                    expiresAtMillis = elapsedRealtimeMillis() + ACTIVE_DURATION_MILLIS,
                    method = outcome.method,
                )
            }
            // 실패·취소는 활성 중이어도 즉시 비활성화한다(PRD §4.3-4).
            AuthOutcome.Mismatch, is AuthOutcome.Failed -> deactivate(InactiveReason.AUTH_FAILED)
            is AuthOutcome.Canceled -> deactivate(InactiveReason.AUTH_CANCELED)
            AuthOutcome.SetupRequired -> deactivate(InactiveReason.SETUP_REQUIRED)
            is AuthOutcome.Unavailable -> deactivate(InactiveReason.UNAVAILABLE)
        }
        return state
    }

    // 앱이 백그라운드로 가면 즉시 비활성화한다. 화면 구성 변경은 여기로 오지 않아야 한다.
    // 진행 중인 요청도 끝내 이탈 뒤 늦게 도착한 성공이 활성화하지 못하게 한다.
    // 진행 중인 요청이 있었으면 true를 돌려 호출 측이 인증 창을 닫게 한다.
    fun onAppLeft(): Boolean {
        val hadPendingRequest = isAuthenticating
        isAuthenticating = false
        if (active != null || hadPendingRequest) {
            deactivate(InactiveReason.LEFT_APP)
        }
        return hadPendingRequest
    }

    private fun deactivate(reason: InactiveReason) {
        active = null
        lastInactiveReason = reason
    }

    companion object {
        const val ACTIVE_DURATION_MILLIS: Long = 60_000L
    }
}

sealed interface ActivationState {

    data class Active(
        val expiresAtMillis: Long,
        val method: AuthMethod,
    ) : ActivationState

    data class Inactive(val reason: InactiveReason) : ActivationState
}

enum class InactiveReason {
    NOT_AUTHENTICATED,
    EXPIRED,
    AUTH_FAILED,
    AUTH_CANCELED,
    LEFT_APP,
    SETUP_REQUIRED,
    UNAVAILABLE,
}
