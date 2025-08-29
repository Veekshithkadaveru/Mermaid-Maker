package com.example.mermaidmaker.ui.preview

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State holder for MermaidPreview
 */
@Stable
class MermaidPreviewState {
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _lastRenderedContent = MutableStateFlow("")
    val lastRenderedContent: StateFlow<String> = _lastRenderedContent.asStateFlow()
    
    private var webView: WebView? = null
    private var queuedContent: String? = null
    
    internal fun setWebView(webView: WebView) {
        this.webView = webView
        _isReady.value = false
        queuedContent = null
    }
    
    internal fun setReady(ready: Boolean) {
        _isReady.value = ready
        if (ready && queuedContent != null) {
            Log.d("MermaidPreview", "WebView ready, rendering queued content")
            val content = queuedContent
            queuedContent = null
            content?.let { renderDiagram(it, debounced = false) }
        }
    }
    
    internal fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    internal fun setError(error: String?) {
        _error.value = error
    }
    
    internal fun setLastRenderedContent(content: String) {
        _lastRenderedContent.value = content
    }
    
    /**
     * Render Mermaid diagram with the given source
     */
    fun renderDiagram(source: String, debounced: Boolean = true) {
        webView?.let { webView ->
            if (!_isReady.value) {
                Log.w("MermaidPreview", "WebView not ready, queueing content for render")
                queuedContent = source
                return
            }

            // Avoid no-op re-renders that would leave loading state stuck
            if (source == _lastRenderedContent.value) {
                setLoading(false)
                return
            }

            if (source.isBlank()) {
                clearPreview()
                setLoading(false)
                return
            }

            // Reset any stuck state in the page before rendering and rebuild the preview container
            webView.evaluateJavascript("resetPreviewState(); forceRecreatePreview();", null)
            setLoading(true)
            setError(null)

            val escapedSource = source.escapeForJs()
            val jsFunction = if (debounced) "renderMermaidDebounced" else "renderMermaid"

            webView.evaluateJavascript("$jsFunction(`$escapedSource`);") { result ->
                Log.d("MermaidPreview", "Render initiated: $result")
            }
        }
    }
    
    /**
     * Clear the preview
     */
    fun clearPreview() {
        webView?.evaluateJavascript("clearPreview();", null)
        setLastRenderedContent("")
        setError(null)
        queuedContent = null
    }
    
    /**
     * Set theme for Mermaid diagrams
     */
    fun setTheme(theme: String) {
        webView?.evaluateJavascript("setTheme('$theme');", null)
    }
    
    /**
     * Get rendered SVG content
     */
    fun getRenderedSVG(callback: (String?) -> Unit) {
        webView?.evaluateJavascript("getRenderedSVG();") { result ->
            // Remove quotes and unescape the result
            val svg = result?.removeSurrounding("\"")?.unescapeFromJs()
            callback(svg)
        }
    }

    /**
     * Export rendered SVG using Storage Access Framework
     */
    fun exportSVG(fileName: String = "diagram.svg", onResult: (Boolean) -> Unit) {
        getRenderedSVG { svg ->
            if (svg != null && svg.isNotBlank()) {
                // This would integrate with the FileExportService
                // For now, just log success
                Log.d("MermaidPreview", "SVG ready for export: ${svg.length} characters")
                onResult(true)
            } else {
                Log.e("MermaidPreview", "No SVG content available for export")
                onResult(false)
            }
        }
    }

    /**
     * Share rendered SVG
     */
    fun shareSVG(fileName: String = "diagram.svg", onResult: (Boolean) -> Unit) {
        getRenderedSVG { svg ->
            if (svg != null && svg.isNotBlank()) {
                // This would integrate with the FileExportService
                // For now, just log success
                Log.d("MermaidPreview", "SVG ready for sharing: ${svg.length} characters")
                onResult(true)
            } else {
                Log.e("MermaidPreview", "No SVG content available for sharing")
                onResult(false)
            }
        }
    }

    /**
     * Zoom helpers bridged to WebView JS
     */
    fun zoomIn() {
        webView?.evaluateJavascript("zoomIn();", null)
    }

    fun zoomOut() {
        webView?.evaluateJavascript("zoomOut();", null)
    }

    fun fitToWidth() {
        // Reset scale to 1 first, then try to restore saved scale
        webView?.evaluateJavascript("setScale(1.0); restoreScaleForContent();", null)
    }

    fun setZoom(scale: Float) {
        webView?.evaluateJavascript("setScale(${"%1.2f".format(scale)});", null)
    }
    
    /**
     * Reload the HTML page to ensure fresh WebView state
     */
    fun reloadPreview() {
        webView?.let { webView ->
            Log.d("MermaidPreview", "Reloading preview HTML")
            _isReady.value = false
            setLoading(false)
            setError(null)
            webView.loadUrl("file:///android_asset/mermaid_preview.html")
        }
    }
}

/**
 * Remember MermaidPreviewState
 */
@Composable
fun rememberMermaidPreviewState(): MermaidPreviewState {
    return remember { MermaidPreviewState() }
}

/**
 * MermaidPreview composable - displays live preview of Mermaid diagrams
 */
@Composable
fun MermaidPreview(
    content: String,
    modifier: Modifier = Modifier,
    state: MermaidPreviewState = rememberMermaidPreviewState(),
    onRenderError: (String) -> Unit = {},
    onRenderSuccess: (Int) -> Unit = {},
    theme: String = "default",
    showControls: Boolean = false
) {

    val isReady by state.isReady.collectAsState()
    val isLoading by state.isLoading.collectAsState()
    val error by state.error.collectAsState()
    
    // JavaScript interface for communication with WebView
    val javascriptInterface = remember {
        MermaidPreviewJavaScriptInterface(
            onWebViewReady = { 
                Log.d("MermaidPreview", "WebView ready")
                state.setReady(true)
                state.setTheme(theme)
            },
            onRenderSuccess = { svgLength ->
                Log.d("MermaidPreview", "Render success: $svgLength characters")
                state.setLoading(false)
                state.setError(null)
                onRenderSuccess(svgLength)
            },
            onRenderError = { errorMessage ->
                Log.e("MermaidPreview", "Render error: $errorMessage")
                state.setLoading(false)
                state.setError(errorMessage)
                onRenderError(errorMessage)
            }
        )
    }
    
    Box(modifier = modifier) {
        // WebView for preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    state.setWebView(this)
                    setupMermaidPreviewWebView(this, javascriptInterface)
                }
            }
        )
        
        // Loading overlay - only show when ready
        if (isLoading && isReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Rendering diagram...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        // Overlay zoom controls (optional)
        if (showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                ElevatedCard {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { state.setZoom(0.5f) }) { Text("50%") }
                        TextButton(onClick = { state.setZoom(1.0f) }) { Text("100%") }
                        TextButton(onClick = { state.setZoom(1.5f) }) { Text("150%") }
                        TextButton(onClick = { state.fitToWidth() }) { Text("Fit") }
                        TextButton(onClick = { state.zoomOut() }) { Text("-") }
                        TextButton(onClick = { state.zoomIn() }) { Text("+") }
                    }
                }
            }
        }

        // Error overlay
        error?.let { errorMessage ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "⚠️ $errorMessage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
        
        // Not ready overlay
        if (!isReady) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Initializing preview...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
    
    // Render content when it changes (only if WebView is ready)
    LaunchedEffect(content, isReady) {
        if (content.isNotBlank()) {
            state.renderDiagram(content, debounced = false)
        } else if (isReady) {
            state.clearPreview()
        }
    }
    
    // Set theme when it changes
    LaunchedEffect(theme, isReady) {
        if (isReady) {
            state.setTheme(theme)
        }
    }
}

/**
 * Setup WebView for Mermaid preview
 */
@SuppressLint("SetJavaScriptEnabled")
private fun setupMermaidPreviewWebView(
    webView: WebView, 
    javascriptInterface: MermaidPreviewJavaScriptInterface
) {
    Log.d("MermaidPreview", "Setting up WebView")
    
    webView.apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            // Enable pinch-to-zoom for better readability
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            // Security: Disable universal file access
            @Suppress("DEPRECATION")
            allowUniversalAccessFromFileURLs = false
            @Suppress("DEPRECATION")
            allowFileAccessFromFileURLs = false
            // Enable Safe Browsing
            safeBrowsingEnabled = true
            // Keep system font size sensible; rely on SVG scaling
            textZoom = 100
            minimumFontSize = 12
            cacheMode = WebSettings.LOAD_NO_CACHE // Always fresh content
        }
        
        // Security: Only allow access to assets
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false
                // Only allow file:///android_asset/ URLs
                return !url.startsWith("file:///android_asset/")
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d("MermaidPreview", "Page started loading: $url")
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("MermaidPreview", "Page finished loading: $url")
            }
            
            @Deprecated("Deprecated in Java")
            override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e("MermaidPreview", "WebView error: $errorCode - $description")
            }
            
            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                super.onReceivedHttpError(view, request, errorResponse)
                Log.e("MermaidPreview", "HTTP error: ${errorResponse?.statusCode} - ${errorResponse?.reasonPhrase}")
            }
        }
        
        addJavascriptInterface(javascriptInterface, "Android")
        
        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d("MermaidPreview", "Console: ${consoleMessage.message()} at ${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}")
                return true
            }
            
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                Log.d("MermaidPreview", "JS Alert: $message")
                result?.confirm()
                return true
            }
        }
        
        
        Log.d("MermaidPreview", "Loading Mermaid preview HTML")
        loadUrl("file:///android_asset/mermaid_preview.html")
    }
}

/**
 * JavaScript interface for Mermaid preview communication
 */
class MermaidPreviewJavaScriptInterface(
    private val onWebViewReady: () -> Unit,
    private val onRenderSuccess: (Int) -> Unit,
    private val onRenderError: (String) -> Unit
) {
    @JavascriptInterface
    fun onWebViewReady() {
        Log.d("MermaidPreviewJS", "WebView ready callback received")
        onWebViewReady.invoke()
    }
    
    @JavascriptInterface
    fun onRenderSuccess(svgLength: Int) {
        Log.d("MermaidPreviewJS", "Render success: $svgLength characters")
        onRenderSuccess.invoke(svgLength)
    }
    
    @JavascriptInterface
    fun onRenderError(error: String) {
        Log.e("MermaidPreviewJS", "Render error: $error")
        onRenderError.invoke(error)
    }
}

/**
 * Utility functions for string escaping/unescaping for JavaScript
 */
private fun String.escapeForJs(): String {
    return this
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("$", "\\$")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
        .replace("\"", "\\\"")
        .replace("'", "\\'")
}

private fun String.unescapeFromJs(): String {
    return this
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\`", "`")
        .replace("\\$", "$")
        .replace("\\\"", "\"")
        .replace("\\'", "'")
        .replace("\\\\", "\\")
}

/**
 * Composable for testing MermaidPreview
 */
@Composable
fun MermaidPreviewTest() {
    var content by remember { 
        mutableStateOf("""
            graph TD
                A[Start] --> B{Decision?}
                B -->|Yes| C[Process A]
                B -->|No| D[Process B]
                C --> E[End]
                D --> E
        """.trimIndent()) 
    }
    val previewState = rememberMermaidPreviewState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Test content selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Test Content:", style = MaterialTheme.typography.labelMedium)
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Button(
                        onClick = { 
                            content = """
                                graph TD
                                    A[Start] --> B[Process]
                                    B --> C[End]
                            """.trimIndent()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Flowchart", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Button(
                        onClick = { 
                            content = """
                                sequenceDiagram
                                    Alice->>Bob: Hello Bob!
                                    Bob-->>Alice: Hello Alice!
                            """.trimIndent()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sequence", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    Button(
                        onClick = { content = "invalid syntax" },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Error", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        
        // Preview
        MermaidPreview(
            content = content,
            state = previewState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            onRenderError = { error ->
                Log.e("MermaidPreviewTest", "Render error: $error")
            },
            onRenderSuccess = { svgLength ->
                Log.d("MermaidPreviewTest", "Render success: $svgLength chars")
            }
        )
    }
}
