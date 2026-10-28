package com.beone.app.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VirtualCardActivationViewModelTest {

    private class FakeDeviceAuthenticator(var blocked: AuthOutcome? = null) : DeviceAuthenticator {
        var promptCount = 0
        var cancelCount = 0

        override fun availability(): AuthOutcome? = blocked

        override fun authenticate() {
            promptCount++
        }

        override fun cancel() {
            cancelCount++
        }
    }

    private var now = 5_000L
    private val viewModel = VirtualCardActivationViewModel { now }
    private val authenticator = FakeDeviceAuthenticator()

    @Test
    fun requestShowsPromptOnceUntilResultArrives() {
        assertTrue(viewModel.requestAuthentication(authenticator))
        assertFalse(viewModel.requestAuthentication(authenticator))

        assertEquals(1, authenticator.promptCount)
        assertTrue(viewModel.isAuthenticating)
    }

    @Test
    fun successFromPromptActivatesForSixtySeconds() {
        viewModel.requestAuthentication(authenticator)

        viewModel.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.BIOMETRIC))

        assertEquals(60_000L, viewModel.remainingMillis())
    }

    @Test
    fun deviceWithoutAnyCredentialIsNotPromptedAndAsksForSetup() {
        authenticator.blocked = AuthOutcome.SetupRequired

        assertFalse(viewModel.requestAuthentication(authenticator))

        assertEquals(0, authenticator.promptCount)
        assertEquals(ActivationState.Inactive(InactiveReason.SETUP_REQUIRED), viewModel.state)
        assertFalse(viewModel.isAuthenticating)
    }

    @Test
    fun unavailableDeviceIsNotPrompted() {
        authenticator.blocked = AuthOutcome.Unavailable(errorCode = 1)

        assertFalse(viewModel.requestAuthentication(authenticator))

        assertEquals(0, authenticator.promptCount)
        assertEquals(ActivationState.Inactive(InactiveReason.UNAVAILABLE), viewModel.state)
    }

    @Test
    fun configurationChangeStopDoesNotDeactivate() {
        viewModel.requestAuthentication(authenticator)
        viewModel.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.BIOMETRIC))
        now += 10_000L

        viewModel.onScreenStopped(isChangingConfigurations = true, authenticator)

        assertEquals(50_000L, viewModel.remainingMillis())
    }

    @Test
    fun configurationChangeDoesNotExtendTime() {
        viewModel.requestAuthentication(authenticator)
        viewModel.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.BIOMETRIC))
        now += 30_000L
        viewModel.onScreenStopped(isChangingConfigurations = true, authenticator)
        now += 30_000L

        assertEquals(ActivationState.Inactive(InactiveReason.EXPIRED), viewModel.state)
    }

    @Test
    fun resultDeliveredAfterConfigurationChangeIsApplied() {
        viewModel.requestAuthentication(authenticator)
        viewModel.onScreenStopped(isChangingConfigurations = true, authenticator)

        // 재생성된 화면이 새 authenticator로 결과를 받는다.
        viewModel.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.DEVICE_CREDENTIAL))

        assertTrue(viewModel.state is ActivationState.Active)
        assertEquals(0, authenticator.cancelCount)
    }

    @Test
    fun appLeaveStopDeactivatesImmediately() {
        viewModel.requestAuthentication(authenticator)
        viewModel.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.BIOMETRIC))

        viewModel.onScreenStopped(isChangingConfigurations = false, authenticator)

        assertEquals(ActivationState.Inactive(InactiveReason.LEFT_APP), viewModel.state)
    }

    @Test
    fun appLeaveDuringPromptClosesPromptAndIgnoresLateSuccess() {
        viewModel.requestAuthentication(authenticator)

        viewModel.onScreenStopped(isChangingConfigurations = false, authenticator)
        viewModel.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.BIOMETRIC))

        assertEquals(1, authenticator.cancelCount)
        assertEquals(ActivationState.Inactive(InactiveReason.LEFT_APP), viewModel.state)
        assertTrue(viewModel.requestAuthentication(authenticator))
    }

    @Test
    fun appLeaveWithoutPromptDoesNotCancel() {
        viewModel.requestAuthentication(authenticator)
        viewModel.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.BIOMETRIC))

        viewModel.onScreenStopped(isChangingConfigurations = false, authenticator)

        assertEquals(0, authenticator.cancelCount)
    }
}
