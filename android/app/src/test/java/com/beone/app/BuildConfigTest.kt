package com.beone.app

import org.junit.Assert.assertEquals
import org.junit.Test

class BuildConfigTest {

    @Test
    fun applicationIdIsStable() {
        // Changing the application id breaks installs and signing; it must be deliberate.
        assertEquals("com.beone.app", BuildConfig.APPLICATION_ID)
    }
}
