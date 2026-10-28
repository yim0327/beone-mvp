package com.beone.app.web

import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

// 신뢰 origin 밖으로의 메인 프레임 탐색을 막고 SSL 오류를 무시하지 않는 WebViewClient.
class RestrictedWebViewClient(
    private val policy: WebNavigationPolicy,
    private val listener: Listener,
) : WebViewClient() {

    interface Listener {
        fun onNavigationBlocked()

        fun onTrustedPageStarted()

        fun onHistoryChanged(canGoBack: Boolean)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        if (policy.isAllowed(request.url.toString())) return false
        // 신뢰하지 않는 주소는 WebView에서 열지 않는다. 외부 앱으로 넘기는 동작도 아직 정하지 않았다.
        if (request.isForMainFrame) listener.onNavigationBlocked()
        return true
    }

    // POST 폼 제출은 shouldOverrideUrlLoading을 거치지 않으므로 페이지 시작 시 다시 확인한다.
    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        if (policy.isAllowed(url)) {
            listener.onTrustedPageStarted()
            return
        }
        view.stopLoading()
        if (url != BLANK_PAGE) listener.onNavigationBlocked()
    }

    override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
        listener.onHistoryChanged(view.canGoBack())
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        handler.cancel()
    }

    private companion object {
        const val BLANK_PAGE = "about:blank"
    }
}
