package com.beone.app.web

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element

// 소스의 네트워크 보안 설정과 Manifest 연결을 고정한다. 빌드 산출물이나 기기 동작을 확인하지 않는다.
// 단위 테스트는 app 모듈 폴더에서 실행되므로 상대 경로로 소스 파일을 읽는다.
class NetworkSecurityConfigTest {

    private fun parse(path: String): Document {
        val file = File(path)
        assertTrue("파일 없음: $path", file.isFile)
        return DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            .newDocumentBuilder()
            .parse(file)
    }

    private fun Document.elements(tag: String): List<Element> {
        val nodes = getElementsByTagName(tag)
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }

    private fun Document.assertBaseDeniesCleartextAndTrustsSystemOnly() {
        val base = elements("base-config").single()
        assertEquals("false", base.getAttribute("cleartextTrafficPermitted"))
        assertEquals(listOf("system"), elements("certificates").map { it.getAttribute("src") })
    }

    @Test
    fun mainConfigDeniesCleartextEverywhere() {
        val config = parse("src/main/res/xml/network_security_config.xml")

        config.assertBaseDeniesCleartextAndTrustsSystemOnly()
        assertTrue(config.elements("domain-config").isEmpty())
        assertTrue(config.elements("debug-overrides").isEmpty())
    }

    @Test
    fun debugConfigAllowsCleartextOnlyForLocalDevHosts() {
        val config = parse("src/debug/res/xml/network_security_config.xml")

        config.assertBaseDeniesCleartextAndTrustsSystemOnly()
        val domainConfig = config.elements("domain-config").single()
        assertEquals("true", domainConfig.getAttribute("cleartextTrafficPermitted"))
        val domains = config.elements("domain")
        assertEquals(setOf("localhost", "127.0.0.1", "10.0.2.2"), domains.map { it.textContent.trim() }.toSet())
        assertEquals(3, domains.size)
        domains.forEach { assertEquals("false", it.getAttribute("includeSubdomains")) }
    }

    @Test
    fun manifestUsesNetworkSecurityConfigWithoutGlobalCleartext() {
        val application = parse("src/main/AndroidManifest.xml").elements("application").single()

        assertEquals(
            "@xml/network_security_config",
            application.getAttributeNS(ANDROID_NS, "networkSecurityConfig"),
        )
        assertFalse(application.hasAttributeNS(ANDROID_NS, "usesCleartextTraffic"))
        assertFalse(application.hasAttributeNS(ANDROID_NS, "debuggable"))
    }

    private companion object {
        const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    }
}
