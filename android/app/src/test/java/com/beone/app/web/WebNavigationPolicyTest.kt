package com.beone.app.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WebNavigationPolicyTest {

    private val devPolicy = WebNavigationPolicy("http://localhost:5173/")
    private val httpsPolicy = WebNavigationPolicy("https://example.test/app/")

    @Test
    fun parsesOriginWithDefaultAndExplicitPorts() {
        assertEquals(WebOrigin("https", "example.test", 443), WebOrigin.parse("https://example.test/a?b=c#d"))
        assertEquals(WebOrigin("https", "example.test", 443), WebOrigin.parse("https://example.test:443/"))
        assertEquals(WebOrigin("http", "localhost", 5173), WebOrigin.parse("http://localhost:5173"))
        assertEquals(WebOrigin.parse("http://localhost/"), WebOrigin.parse("http://localhost:80/"))
    }

    @Test
    fun hostAndSchemeAreCaseInsensitive() {
        assertEquals(WebOrigin.parse("https://example.test/"), WebOrigin.parse("HTTPS://Example.TEST/"))
    }

    @Test
    fun rejectsNonWebSchemesAndMalformedUrls() {
        listOf(
            "file:///android_asset/index.html",
            "content://com.beone.app/x",
            "javascript:alert(1)",
            "data:text/html,hi",
            "intent://scan#Intent;end",
            "/relative/path",
            "https://",
            "http://exa mple.test/",
            "",
        ).forEach { url -> assertNull(url, WebOrigin.parse(url)) }
    }

    @Test
    fun allowsSameOriginPaths() {
        assertTrue(devPolicy.isAllowed("http://localhost:5173/"))
        assertTrue(devPolicy.isAllowed("http://localhost:5173/orders/1?x=1#top"))
        assertTrue(httpsPolicy.isAllowed("https://example.test/other"))
    }

    @Test
    fun rejectsDifferentPortSchemeOrHost() {
        assertFalse(devPolicy.isAllowed("http://localhost:8080/"))
        assertFalse(devPolicy.isAllowed("https://localhost:5173/"))
        assertFalse(devPolicy.isAllowed("http://127.0.0.1:5173/"))
        assertFalse(httpsPolicy.isAllowed("http://example.test/"))
        assertFalse(httpsPolicy.isAllowed("https://example.test.evil.test/"))
        assertFalse(httpsPolicy.isAllowed("https://evil.test/?next=https://example.test/"))
    }

    @Test
    fun userInfoDoesNotChangeTheHost() {
        assertFalse(httpsPolicy.isAllowed("https://example.test@evil.test/"))
    }

    @Test
    fun rejectsNonWebSchemesEvenWhenConfigured() {
        assertFalse(httpsPolicy.isAllowed("javascript:alert(1)"))
        assertFalse(httpsPolicy.isAllowed("file:///android_asset/index.html"))
    }

    // about:blank·blob: 주소는 신뢰 origin이 아니므로 메인 프레임 탐색에서 막는다.
    @Test
    fun blankAndBlobUrlsAreNotTrusted() {
        assertFalse(devPolicy.isAllowed("about:blank"))
        assertFalse(devPolicy.isAllowed("blob:http://localhost:5173/0b6c0f8e-1d2a-4c5e-9f00-000000000000"))
    }

    @Test
    fun missingConfigurationAllowsNothing() {
        val policy = WebNavigationPolicy("")

        assertNull(policy.trustedOrigin)
        assertNull(policy.startUrl)
        assertFalse(policy.isAllowed("https://example.test/"))
    }

    @Test
    fun invalidConfigurationAllowsNothing() {
        listOf("not a url", "file:///android_asset/index.html", "ftp://example.test/").forEach { configured ->
            val policy = WebNavigationPolicy(configured)
            assertNull(configured, policy.startUrl)
            assertFalse(configured, policy.isAllowed(configured))
        }
    }

    @Test
    fun startUrlKeepsConfiguredPath() {
        assertEquals("https://example.test/app/", httpsPolicy.startUrl)
    }
}
