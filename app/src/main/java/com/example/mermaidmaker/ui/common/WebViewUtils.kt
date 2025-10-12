package com.example.mermaidmaker.ui.common

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

object WebViewUtils {

    @SuppressLint("SetJavaScriptEnabled")
    fun applyCommonPreviewSettings(webView: WebView) {
        webView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false
            safeBrowsingEnabled = true
            textZoom = 100
            minimumFontSize = 12
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun applyCommonEditorSettings(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            safeBrowsingEnabled = true
        }
    }

    fun createAssetsOnlyClient(
        tag: String,
        onPageStarted: ((String?) -> Unit)? = null,
        onPageFinished: ((String?) -> Unit)? = null
    ): WebViewClient {
        return object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return !url.startsWith("file:///android_asset/")
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                onPageStarted?.invoke(url)
                Log.d(tag, "Page started loading: $url")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onPageFinished?.invoke(url)
                Log.d(tag, "Page finished loading: $url")
            }

            @Deprecated("Deprecated in API 24")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e(tag, "WebView error: $errorCode - $description")
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                super.onReceivedHttpError(view, request, errorResponse)
                Log.e(tag, "HTTP error: ${errorResponse?.statusCode} - ${errorResponse?.reasonPhrase}")
            }
        }
    }

    fun createConsoleLoggingChromeClient(tag: String, interceptAlerts: Boolean = false): WebChromeClient {
        return object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d(tag, "Console: ${consoleMessage.message()} at ${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}")
                return true
            }

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                if (interceptAlerts) {
                    Log.d(tag, "JS Alert: $message")
                    result?.confirm()
                    return true
                }
                return super.onJsAlert(view, url, message, result)
            }
        }
    }
}


