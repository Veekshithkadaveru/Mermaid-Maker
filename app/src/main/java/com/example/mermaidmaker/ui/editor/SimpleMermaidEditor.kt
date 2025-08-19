package com.example.mermaidmaker.ui.editor

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Simplified Mermaid Editor for debugging WebView issues
 */
@Composable
fun SimpleMermaidEditor(
    content: String = "",
    onContentChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isWebViewReady by remember { mutableStateOf(false) }
    
    val javascriptInterface = remember {
        SimpleEditorJavaScriptInterface(
            onContentChanged = onContentChanged,
            onWebViewReady = { 
                Log.d("SimpleMermaidEditor", "WebView ready callback")
                isWebViewReady = true 
            }
        )
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            Log.d("SimpleMermaidEditor", "Creating WebView")
            WebView(context).apply {
                webView = this
                setupSimpleWebView(this, javascriptInterface)
            }
        }
    )

    // Set content when WebView is ready
    LaunchedEffect(isWebViewReady, content) {
        if (isWebViewReady && webView != null && content.isNotEmpty()) {
            Log.d("SimpleMermaidEditor", "Setting content: $content")
            webView?.evaluateJavascript("setContent(`${content.escapeForJs()}`);", null)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun setupSimpleWebView(webView: WebView, javascriptInterface: SimpleEditorJavaScriptInterface) {
    Log.d("SimpleMermaidEditor", "Setting up WebView")
    
    webView.apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            
            // Additional settings for better compatibility
            loadWithOverviewMode = true
            useWideViewPort = true
            allowUniversalAccessFromFileURLs = true
            allowFileAccessFromFileURLs = true
            
            // Better text selection and input handling
            textZoom = 100
            minimumFontSize = 12
        }
        
        addJavascriptInterface(javascriptInterface, "Android")
        
        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("WebView", "Console: ${consoleMessage.message()} at ${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}")
                return true
            }
        }
        
        webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d("SimpleMermaidEditor", "Page started loading: $url")
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("SimpleMermaidEditor", "Page finished loading: $url")
                javascriptInterface.onWebViewReady()
            }
            
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e("SimpleMermaidEditor", "WebView error: $errorCode - $description")
            }
        }
        
        Log.d("SimpleMermaidEditor", "Loading HTML file")
        loadUrl("file:///android_asset/simple_editor.html")
    }
}

/**
 * Simple JavaScript interface for debugging
 */
class SimpleEditorJavaScriptInterface(
    private val onContentChanged: (String) -> Unit,
    private val onWebViewReady: () -> Unit
) {
    private var isReady = false
    
    @JavascriptInterface
    fun onContentChanged(content: String) {
        Log.d("SimpleEditorJS", "Content changed: ${content.length} characters")
        onContentChanged.invoke(content)
    }
    
    @JavascriptInterface
    fun onWebViewReady() {
        Log.d("SimpleEditorJS", "WebView ready called")
        if (!isReady) {
            isReady = true
            onWebViewReady.invoke()
        }
    }
}

/**
 * Extension function for JavaScript string escaping
 */
private fun String.escapeForJs(): String {
    return this
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("$", "\\$")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}