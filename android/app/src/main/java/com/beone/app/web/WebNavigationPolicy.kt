package com.beone.app.web

// WebView 안에서 열어도 되는 주소를 판정한다. 신뢰 origin은 빌드 설정값에서 받으며,
// release의 로딩 방식·origin이 정해지기 전까지 release에는 값이 없다.
class WebNavigationPolicy(startUrl: String) {

    // 설정이 없거나 잘못됐으면 null이며, 이때는 어떤 주소도 허용하지 않는다.
    val trustedOrigin: WebOrigin? = startUrl.takeIf { it.isNotBlank() }?.let(WebOrigin::parse)

    val startUrl: String? = startUrl.trim().takeIf { trustedOrigin != null }

    fun isAllowed(url: String): Boolean {
        val trusted = trustedOrigin ?: return false
        return WebOrigin.parse(url) == trusted
    }
}
