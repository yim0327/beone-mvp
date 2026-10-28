package com.beone.app.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.beone.app.R

// D-08: BIOMETRIC_STRONG | DEVICE_CREDENTIAL 단일 프롬프트. 지문을 쓸 수 없으면 같은 창에서 기기 PIN으로 진행한다.
// 화면 재생성 뒤에도 결과를 받도록 Activity의 onCreate에서 매번 새로 만든다(androidx.biometric 권장 방식).
class BiometricDeviceAuthenticator(
    activity: FragmentActivity,
    onOutcome: (AuthOutcome) -> Unit,
) : DeviceAuthenticator {

    private val biometricManager = BiometricManager.from(activity)

    private val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onOutcome(AuthOutcome.Succeeded(BiometricResultMapper.authMethod(result.authenticationType)))
            }

            override fun onAuthenticationFailed() {
                onOutcome(AuthOutcome.Mismatch)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onOutcome(BiometricResultMapper.errorOutcome(errorCode))
            }
        },
    )

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(activity.getString(R.string.device_auth_prompt_title))
        .setSubtitle(activity.getString(R.string.device_auth_prompt_subtitle))
        .setAllowedAuthenticators(ALLOWED_AUTHENTICATORS)
        .build()

    override fun availability(): AuthOutcome? =
        BiometricResultMapper.availabilityOutcome(biometricManager.canAuthenticate(ALLOWED_AUTHENTICATORS))

    override fun authenticate() {
        prompt.authenticate(promptInfo)
    }

    override fun cancel() {
        prompt.cancelAuthentication()
    }

    companion object {
        const val ALLOWED_AUTHENTICATORS: Int = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    }
}
