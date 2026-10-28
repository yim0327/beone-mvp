package com.beone.app.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VirtualCardActivationTest {

    private var now = 1_000_000L
    private val activation = VirtualCardActivation { now }

    private fun authenticate(outcome: AuthOutcome = AuthOutcome.Succeeded(AuthMethod.BIOMETRIC)): ActivationState {
        assertTrue(activation.beginAuthentication())
        return activation.onAuthOutcome(outcome)
    }

    private fun inactive(reason: InactiveReason) = ActivationState.Inactive(reason)

    @Test
    fun startsInactiveBeforeAuthentication() {
        assertEquals(inactive(InactiveReason.NOT_AUTHENTICATED), activation.state)
        assertEquals(0L, activation.remainingMillis())
    }

    @Test
    fun successActivatesForSixtySeconds() {
        val state = authenticate(AuthOutcome.Succeeded(AuthMethod.DEVICE_CREDENTIAL))

        assertEquals(ActivationState.Active(now + 60_000L, AuthMethod.DEVICE_CREDENTIAL), state)
        assertEquals(60_000L, activation.remainingMillis())
    }

    @Test
    fun staysActiveOneMillisecondBeforeExpiry() {
        authenticate()
        now += 59_999L

        assertTrue(activation.state is ActivationState.Active)
        assertEquals(1L, activation.remainingMillis())
    }

    @Test
    fun expiresExactlyAtSixtySeconds() {
        authenticate()
        now += 60_000L

        assertEquals(inactive(InactiveReason.EXPIRED), activation.state)
        assertEquals(0L, activation.remainingMillis())
    }

    @Test
    fun expiredStateDoesNotComeBackActive() {
        authenticate()
        now += 60_000L
        activation.state

        now += 1L
        assertEquals(inactive(InactiveReason.EXPIRED), activation.state)
    }

    @Test
    fun reauthenticationWhileActiveRestartsFullSixtySecondsWithoutCarryover() {
        authenticate()
        now += 40_000L

        authenticate()

        assertEquals(60_000L, activation.remainingMillis())
        now += 59_999L
        assertTrue(activation.state is ActivationState.Active)
        now += 1L
        assertEquals(inactive(InactiveReason.EXPIRED), activation.state)
    }

    @Test
    fun reauthenticationAfterExpiryActivatesAgain() {
        authenticate()
        now += 60_000L
        activation.state

        authenticate()

        assertEquals(60_000L, activation.remainingMillis())
    }

    @Test
    fun cancelWhileActiveDeactivatesImmediately() {
        authenticate()
        now += 10_000L

        val state = authenticate(AuthOutcome.Canceled(errorCode = 10))

        assertEquals(inactive(InactiveReason.AUTH_CANCELED), state)
        assertEquals(0L, activation.remainingMillis())
    }

    @Test
    fun failureWhileActiveDeactivatesImmediately() {
        authenticate()

        val state = authenticate(AuthOutcome.Failed(errorCode = 7))

        assertEquals(inactive(InactiveReason.AUTH_FAILED), state)
    }

    @Test
    fun mismatchDeactivatesButKeepsPromptOpenForRetry() {
        authenticate()
        assertTrue(activation.beginAuthentication())

        val state = activation.onAuthOutcome(AuthOutcome.Mismatch)

        assertEquals(inactive(InactiveReason.AUTH_FAILED), state)
        assertTrue(activation.isAuthenticating)
        assertFalse(activation.beginAuthentication())

        activation.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.BIOMETRIC))
        assertEquals(60_000L, activation.remainingMillis())
        assertFalse(activation.isAuthenticating)
    }

    @Test
    fun setupRequiredDoesNotActivate() {
        val state = authenticate(AuthOutcome.SetupRequired)

        assertEquals(inactive(InactiveReason.SETUP_REQUIRED), state)
        assertFalse(activation.isAuthenticating)
    }

    @Test
    fun unavailableDoesNotActivate() {
        val state = authenticate(AuthOutcome.Unavailable(errorCode = 1))

        assertEquals(inactive(InactiveReason.UNAVAILABLE), state)
    }

    @Test
    fun leavingAppWhileActiveDeactivatesImmediately() {
        authenticate()
        now += 5_000L

        activation.onAppLeft()

        assertEquals(inactive(InactiveReason.LEFT_APP), activation.state)
        now -= 5_000L
        assertEquals(inactive(InactiveReason.LEFT_APP), activation.state)
    }

    @Test
    fun returningAfterLeavingRequiresNewAuthentication() {
        authenticate()
        activation.onAppLeft()
        now += 1_000L

        assertEquals(inactive(InactiveReason.LEFT_APP), activation.state)
        authenticate()
        assertEquals(60_000L, activation.remainingMillis())
    }

    @Test
    fun leavingAppWhileInactiveKeepsPreviousReason() {
        authenticate()
        now += 60_000L
        activation.state

        activation.onAppLeft()

        assertEquals(inactive(InactiveReason.EXPIRED), activation.state)
    }

    @Test
    fun duplicateAuthenticationRequestIsRejectedWhileInProgress() {
        assertTrue(activation.beginAuthentication())

        assertFalse(activation.beginAuthentication())
        assertTrue(activation.isAuthenticating)
    }

    @Test
    fun newRequestIsAllowedAfterTerminalOutcome() {
        authenticate(AuthOutcome.Canceled(errorCode = 10))

        assertTrue(activation.beginAuthentication())
    }

    @Test
    fun outcomeWithoutPendingRequestIsIgnored() {
        val state = activation.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.BIOMETRIC))

        assertEquals(inactive(InactiveReason.NOT_AUTHENTICATED), state)
    }

    @Test
    fun lateSuccessAfterTerminalOutcomeDoesNotActivate() {
        authenticate(AuthOutcome.Canceled(errorCode = 5))

        activation.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.BIOMETRIC))

        assertEquals(inactive(InactiveReason.AUTH_CANCELED), activation.state)
    }

    @Test
    fun leavingAppDuringAuthenticationEndsRequestAndIgnoresLateSuccess() {
        assertTrue(activation.beginAuthentication())

        assertTrue(activation.onAppLeft())
        activation.onAuthOutcome(AuthOutcome.Succeeded(AuthMethod.BIOMETRIC))

        assertEquals(inactive(InactiveReason.LEFT_APP), activation.state)
        assertFalse(activation.isAuthenticating)
    }

    @Test
    fun systemCancelAfterLeavingKeepsLeftAppReason() {
        assertTrue(activation.beginAuthentication())
        activation.onAppLeft()

        activation.onAuthOutcome(AuthOutcome.Canceled(errorCode = 5))

        assertEquals(inactive(InactiveReason.LEFT_APP), activation.state)
    }

    @Test
    fun newRequestIsAllowedAfterReturningFromLeave() {
        assertTrue(activation.beginAuthentication())
        activation.onAppLeft()

        assertTrue(activation.beginAuthentication())
    }

    @Test
    fun leavingWithoutPendingRequestReportsNothingToCancel() {
        authenticate()

        assertFalse(activation.onAppLeft())
    }
}
