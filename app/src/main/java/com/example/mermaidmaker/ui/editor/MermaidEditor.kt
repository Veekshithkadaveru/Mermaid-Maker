package com.example.mermaidmaker.ui.editor

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

/**
 * A Compose wrapper for the Mermaid syntax editor using WebView and CodeMirror
 */
@Composable
fun MermaidEditor(
    content: String = "",
    onContentChanged: (String) -> Unit = {},
    onCursorPositionChanged: (Int, Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    theme: String = "material-darker",
    fontSize: Int = 14,
    readOnly: Boolean = false
) {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isWebViewReady by remember { mutableStateOf(false) }
    
    // JavaScript interface for communication with WebView
    val javascriptInterface = remember {
        MermaidEditorJavaScriptInterface(
            onContentChanged = onContentChanged,
            onCursorPositionChanged = onCursorPositionChanged,
            onWebViewReady = { isWebViewReady = true }
        )
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                webView = this
                setupWebView(this, javascriptInterface)
            }
        },
        update = { webView ->
            // Update content when it changes
            if (isWebViewReady && webView.url != null) {
                webView.evaluateJavascript("setContent(`${content.escapeForJs()}`);", null)
            }
        }
    )

    // Load content when WebView is ready
    LaunchedEffect(isWebViewReady, content) {
        if (isWebViewReady && webView != null) {
            webView?.evaluateJavascript("setContent(`${content.escapeForJs()}`);", null)
            if (theme != "material-darker") {
                webView?.evaluateJavascript("setTheme('$theme');", null)
            }
            if (fontSize != 14) {
                webView?.evaluateJavascript("setFontSize($fontSize);", null)
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun setupWebView(webView: WebView, javascriptInterface: MermaidEditorJavaScriptInterface) {
    webView.apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
        }
        
        addJavascriptInterface(javascriptInterface, "Android")
        
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                javascriptInterface.onWebViewReady()
            }
        }
        
        loadUrl("file:///android_asset/mermaid_editor.html")
    }
}

/**
 * JavaScript interface for communication between Android and WebView
 */
class MermaidEditorJavaScriptInterface(
    private val onContentChanged: (String) -> Unit,
    private val onCursorPositionChanged: (Int, Int) -> Unit,
    private val onWebViewReady: () -> Unit
) {
    private var isReady = false
    @JavascriptInterface
    fun onContentChanged(content: String) {
        onContentChanged.invoke(content)
    }
    
    @JavascriptInterface
    fun onCursorPositionChanged(line: Int, ch: Int) {
        onCursorPositionChanged.invoke(line, ch)
    }
    
    fun onWebViewReady() {
        if (!isReady) {
            isReady = true
            onWebViewReady.invoke()
        }
    }
}

/**
 * Utility functions for the Mermaid editor
 */
object MermaidEditorUtils {
    /**
     * Get content from the editor
     */
    fun getContent(webView: WebView, callback: (String) -> Unit) {
        webView.evaluateJavascript("getContent();") { result ->
            // Remove quotes from the result and unescape
            val content = result?.removeSurrounding("\"")?.unescapeFromJs() ?: ""
            callback(content)
        }
    }
    
    /**
     * Set content in the editor
     */
    fun setContent(webView: WebView, content: String) {
        webView.evaluateJavascript("setContent(`${content.escapeForJs()}`);", null)
    }
    
    /**
     * Insert text at cursor position
     */
    fun insertText(webView: WebView, text: String) {
        webView.evaluateJavascript("insertText(`${text.escapeForJs()}`);", null)
    }
    
    /**
     * Clear editor content
     */
    fun clear(webView: WebView) {
        webView.evaluateJavascript("clear();", null)
    }
    
    /**
     * Focus the editor
     */
    fun focus(webView: WebView) {
        webView.evaluateJavascript("focus();", null)
    }
    
    /**
     * Insert a template into the editor
     */
    fun insertTemplate(webView: WebView, templateContent: String) {
        webView.evaluateJavascript("insertTemplate(`${templateContent.escapeForJs()}`);", null)
    }
    
    /**
     * Set editor theme
     */
    fun setTheme(webView: WebView, theme: String) {
        webView.evaluateJavascript("setTheme('$theme');", null)
    }
    
    /**
     * Set font size
     */
    fun setFontSize(webView: WebView, fontSize: Int) {
        webView.evaluateJavascript("setFontSize($fontSize);", null)
    }
}

/**
 * Extension functions for string escaping/unescaping for JavaScript
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

private fun String.unescapeFromJs(): String {
    return this
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\`", "`")
        .replace("\\$", "$")
        .replace("\\\\", "\\")
}

/**
 * Composable for a simple read-only Mermaid editor
 */
@Composable
fun MermaidEditorReadOnly(
    content: String,
    modifier: Modifier = Modifier,
    theme: String = "material-darker",
    fontSize: Int = 14
) {
    MermaidEditor(
        content = content,
        onContentChanged = {},
        modifier = modifier,
        theme = theme,
        fontSize = fontSize,
        readOnly = true
    )
}