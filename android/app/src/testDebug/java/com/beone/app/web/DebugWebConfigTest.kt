package com.beone.app.web

import com.beone.app.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class DebugWebConfigTest {

    @Test
    fun debugLoadsLocalDevServer() {
        val origin = WebNavigationPolicy(BuildConfig.WEB_START_URL).trustedOrigin

        // beone.devServerUrl을 바꾸지 않은 기본 빌드 기준
        assertEquals(WebOrigin("http", "localhost", 5173), origin)
    }
}
