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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.roundToInt

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
        webView?.let { webView ->
            if (!_isReady.value) {
                Log.e("MermaidPreview", "WebView not ready for SVG extraction")
                callback(null)
                return
            }
            
            Log.d("MermaidPreview", "Attempting to get rendered SVG")
            webView.evaluateJavascript("getRenderedSVG();") { result ->
                Log.d("MermaidPreview", "SVG result from WebView: $result")
                
                if (result == null || result == "null" || result.isEmpty()) {
                    Log.e("MermaidPreview", "No SVG content returned from WebView")
                    callback(null)
                    return@evaluateJavascript
                }
                
                // Remove quotes and unescape the result
                val svg = result.removeSurrounding("\"").unescapeFromJs()
                Log.d("MermaidPreview", "Processed SVG length: ${svg.length}")
                
                if (svg.isBlank()) {
                    Log.e("MermaidPreview", "SVG content is blank after processing")
                    callback(null)
                } else {
                    callback(svg)
                }
            }
        } ?: run {
            Log.e("MermaidPreview", "WebView is null for SVG extraction")
            callback(null)
        }
    }

    /**
     * Export rendered SVG using FileExportService
     */
    fun exportSVG(
        fileName: String = "diagram.svg", 
        fileExportService: com.example.mermaidmaker.domain.service.FileExportService,
        onResult: (Boolean) -> Unit
    ) {
        getRenderedSVG { svg ->
            if (svg != null && svg.isNotBlank()) {
                CoroutineScope(Dispatchers.Main).launch {
                    fileExportService.exportSvg(svg, fileName) { uri ->
                        val success = uri != null
                        Log.d("MermaidPreview", if (success) "SVG exported successfully to: $uri" else "SVG export failed")
                        onResult(success)
                    }
                }
            } else {
                Log.e("MermaidPreview", "No SVG content available for export")
                onResult(false)
            }
        }
    }

    /**
     * Share rendered SVG using FileExportService
     */
    fun shareSVG(
        fileName: String = "diagram.svg",
        fileExportService: com.example.mermaidmaker.domain.service.FileExportService,
        onResult: (Boolean) -> Unit
    ) {
        getRenderedSVG { svg ->
            if (svg != null && svg.isNotBlank()) {
                CoroutineScope(Dispatchers.Main).launch {
                    val success = fileExportService.shareSvg(svg, fileName)
                    Log.d("MermaidPreview", if (success) "SVG share initiated successfully" else "SVG share failed")
                    onResult(success)
                }
            } else {
                Log.e("MermaidPreview", "No SVG content available for sharing")
                onResult(false)
            }
        }
    }

    /**
     * Generate PNG from rendered diagram using WebView screenshot
     */
    fun generatePNG(callback: (ByteArray?) -> Unit) {
        // First try to render directly from SVG for reliability
        getRenderedSVG { svgContent ->
            var completed = false
            if (!svgContent.isNullOrBlank()) {
                try {
                    val png = renderSvgToPng(svgContent)
                    if (png != null && png.isNotEmpty()) {
                        callback(png)
                        completed = true
                    } else {
                        Log.w("MermaidPreview", "SVG->PNG renderer returned empty; falling back to WebView capture")
                    }
                } catch (e: Exception) {
                    Log.w("MermaidPreview", "SVG->PNG renderer failed; falling back to WebView capture", e)
                }
            }
            if (!completed) {
                capturePngViaWebView(callback)
            }
        }
    }

    private fun capturePngViaWebView(callback: (ByteArray?) -> Unit) {
        webView?.let { webView ->
            try {
                if (!_isReady.value) {
                    Log.e("MermaidPreview", "WebView not ready for PNG generation")
                    callback(null)
                    return
                }
                webView.post {
                    try {
                        webView.evaluateJavascript("getScale();") { currentScaleResult ->
                            val currentScale = try {
                                currentScaleResult?.removeSurrounding("\"")?.toFloatOrNull() ?: 1.0f
                            } catch (e: Exception) { 1.0f }
                            webView.evaluateJavascript("setScale(1.0);", null)
                            webView.postDelayed({
                                try {
                                    webView.evaluateJavascript(
                                        """
                                        (function() {
                                            try {
                                                const hide = (el) => { if (el) el.style.display = 'none'; };
                                                hide(document.getElementById('status'));
                                                hide(document.getElementById('error'));
                                                hide(document.getElementById('loading'));
                                                const svg = document.querySelector('#preview-scale svg') || document.querySelector('#preview svg') || document.querySelector('svg');
                                                if (svg) {
                                                    const bbox = svg.getBBox();
                                                    const vb = (svg.viewBox && svg.viewBox.baseVal) ? svg.viewBox.baseVal : null;
                                                    const w = Math.ceil(bbox.width || (vb ? vb.width : (svg.clientWidth || 800)));
                                                    const h = Math.ceil(bbox.height || (vb ? vb.height : (svg.clientHeight || 600)));
                                                    const minX = Math.floor(bbox.x || (vb ? vb.x : 0));
                                                    const minY = Math.floor(bbox.y || (vb ? vb.y : 0));
                                                    const pad = 8;
                                                    const w2 = w + pad * 2;
                                                    const h2 = h + pad * 2;
                                                    // Add equal margins around content to ensure visual centering
                                                    const centerPad = 24; // exported PNG margin in CSS px
                                                    const canvasW = w2 + centerPad * 2;
                                                    const canvasH = h2 + centerPad * 2;
                                                    const pc = document.querySelector('.preview-container');
                                                    if (pc) { pc.style.padding = '0'; pc.style.border = 'none'; pc.style.alignItems = 'flex-start'; pc.style.justifyContent = 'flex-start'; }
                                                    const preview = document.getElementById('preview');
                                                    if (preview) { preview.style.padding = '0'; preview.style.margin = '0'; preview.style.width = canvasW + 'px'; preview.style.height = canvasH + 'px'; }
                                                    const scale = document.getElementById('preview-scale');
                                                    if (scale) { scale.style.transform = 'scale(1)'; scale.style.width = canvasW + 'px'; scale.style.height = canvasH + 'px'; }
                                                    document.body.style.margin = '0';
                                                    document.body.style.padding = '0';
                                                    const container = document.querySelector('.container');
                                                    if (container) { container.style.padding = '0'; container.style.margin = '0'; container.style.height = canvasH + 'px'; }
                                                    try {
                                                        svg.style.width = canvasW + 'px';
                                                        svg.style.height = canvasH + 'px';
                                                        svg.setAttribute('width', canvasW.toString());
                                                        svg.setAttribute('height', canvasH.toString());
                                                        const vbX = (minX - pad - centerPad);
                                                        const vbY = (minY - pad - centerPad);
                                                        svg.setAttribute('viewBox', vbX + ' ' + vbY + ' ' + canvasW + ' ' + canvasH);
                                                        svg.setAttribute('preserveAspectRatio', 'xMidYMid meet');
                                                    } catch (e) {}
                                                    return JSON.stringify({ width: canvasW, height: canvasH, dpr: (window.devicePixelRatio || 1) });
                                                }
                                            } catch (e) {}
                                            return JSON.stringify({ width: 800, height: 600 });
                                        })();
                                        """.trimIndent()
                                    ) { dimensionsResult ->
                                        try {
                                            val dimensionsJson = dimensionsResult?.removeSurrounding("\"")?.replace("\\\"", "\"") ?: "{\"width\":800,\"height\":600}"
                                            val json = try { JSONObject(dimensionsJson) } catch (e: Exception) { JSONObject().apply { put("width", 800); put("height", 600); put("dpr", 1.0) } }
                                            val cssWidth = json.optInt("width", 800)
                                            val cssHeight = json.optInt("height", 600)
                                            val devicePixelRatio = json.optDouble("dpr", 1.0)
                                            val svgWidth = cssWidth.coerceAtLeast(200).coerceAtMost(8000)
                                            val svgHeight = cssHeight.coerceAtLeast(200).coerceAtMost(8000)
                                            val originalWidth = webView.width
                                            val originalHeight = webView.height
                                            val originalLp = webView.layoutParams
                                            val originalLayerType = webView.layerType
                                            val needsResize = svgWidth != originalWidth || svgHeight != originalHeight
                                            webView.setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                                            val widthSpec = android.view.View.MeasureSpec.makeMeasureSpec(svgWidth, android.view.View.MeasureSpec.EXACTLY)
                                            val heightSpec = android.view.View.MeasureSpec.makeMeasureSpec(svgHeight, android.view.View.MeasureSpec.EXACTLY)
                                            webView.measure(widthSpec, heightSpec)
                                            webView.layout(0, 0, svgWidth, svgHeight)
                                            if (needsResize && originalLp != null) {
                                                originalLp.width = svgWidth
                                                originalLp.height = svgHeight
                                                webView.layoutParams = originalLp
                                            }
                                            webView.postDelayed({
                                                try {
                                                    val maxDimensionPx = 8192
                                                    val maxPixels = 24_000_000
                                                    val desiredWidthPx = (svgWidth * devicePixelRatio).toDouble()
                                                    val desiredHeightPx = (svgHeight * devicePixelRatio).toDouble()
                                                    val scaleByDim = minOf(1.0, maxDimensionPx.toDouble() / desiredWidthPx, maxDimensionPx.toDouble() / desiredHeightPx)
                                                    val scaleByArea = kotlin.math.sqrt((maxPixels / (desiredWidthPx * desiredHeightPx)).coerceAtMost(1.0))
                                                    val outputScale = minOf(1.0, scaleByDim, scaleByArea)
                                                    val outWidthPx = (desiredWidthPx * outputScale).roundToInt().coerceAtLeast(1)
                                                    val outHeightPx = (desiredHeightPx * outputScale).roundToInt().coerceAtLeast(1)
                                                    val bitmap = android.graphics.Bitmap.createBitmap(outWidthPx, outHeightPx, android.graphics.Bitmap.Config.ARGB_8888)
                                                    val canvas = android.graphics.Canvas(bitmap)
                                                    val canvasScale = (outWidthPx.toFloat() / svgWidth.toFloat())
                                                    if (canvasScale != 1f) canvas.scale(canvasScale, canvasScale)
                                                    canvas.drawColor(android.graphics.Color.WHITE)
                                                    try { webView.isVerticalScrollBarEnabled = false } catch (_: Exception) {}
                                                    try { webView.isHorizontalScrollBarEnabled = false } catch (_: Exception) {}
                                                    try { webView.evaluateJavascript("document.body.style.background='transparent'", null) } catch (_: Exception) {}
                                                    webView.draw(canvas)
                                                    if (needsResize) {
                                                        if (originalLp != null) {
                                                            originalLp.width = originalWidth
                                                            originalLp.height = originalHeight
                                                            webView.layoutParams = originalLp
                                                        }
                                                        val origWidthSpec = android.view.View.MeasureSpec.makeMeasureSpec(originalWidth, android.view.View.MeasureSpec.EXACTLY)
                                                        val origHeightSpec = android.view.View.MeasureSpec.makeMeasureSpec(originalHeight, android.view.View.MeasureSpec.EXACTLY)
                                                        webView.measure(origWidthSpec, origHeightSpec)
                                                        webView.layout(0, 0, originalWidth, originalHeight)
                                                    }
                                                    webView.setLayerType(originalLayerType, null)
                                                    webView.evaluateJavascript("setScale(" + currentScale + ");", null)
                                                    val outputStream = java.io.ByteArrayOutputStream()
                                                    val compressed = bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
                                                    if (compressed) {
                                                        val pngData = outputStream.toByteArray()
                                                        outputStream.close()
                                                        bitmap.recycle()
                                                        callback(pngData)
                                                    } else {
                                                        outputStream.close()
                                                        bitmap.recycle()
                                                        callback(null)
                                                    }
                                                } catch (e: Exception) {
                                                    try {
                                                        if (needsResize) {
                                                            if (originalLp != null) {
                                                                originalLp.width = originalWidth
                                                                originalLp.height = originalHeight
                                                                webView.layoutParams = originalLp
                                                            }
                                                            val origWidthSpec = android.view.View.MeasureSpec.makeMeasureSpec(originalWidth, android.view.View.MeasureSpec.EXACTLY)
                                                            val origHeightSpec = android.view.View.MeasureSpec.makeMeasureSpec(originalHeight, android.view.View.MeasureSpec.EXACTLY)
                                                            webView.measure(origWidthSpec, origHeightSpec)
                                                            webView.layout(0, 0, originalWidth, originalHeight)
                                                        }
                                                    } catch (_: Exception) {}
                                                    try { webView.setLayerType(originalLayerType, null) } catch (_: Exception) {}
                                                    webView.evaluateJavascript("setScale(" + currentScale + ");", null)
                                                    callback(null)
                                                }
                                            }, 100)
                                        } catch (e: Exception) {
                                            webView.evaluateJavascript("setScale(" + currentScale + ");", null)
                                            callback(null)
                                        }
                                    }
                                } catch (e: Exception) {
                                    webView.evaluateJavascript("setScale(" + currentScale + ");", null)
                                    callback(null)
                                }
                            }, 100)
                        }
                    } catch (e: Exception) {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                callback(null)
            }
        } ?: run {
            callback(null)
        }
    }

    private fun renderSvgToPng(svgContent: String): ByteArray? {
        return try {

            val decoded = svgContent.decodeUnicodeEscapes()
            val svg = com.caverock.androidsvg.SVG.getFromString(decoded)
            val viewBox = try { svg.documentViewBox } catch (_: Exception) { null }
            var outWidth = try { svg.documentWidth } catch (_: Exception) { 0f }
            var outHeight = try { svg.documentHeight } catch (_: Exception) { 0f }

            if (outWidth <= 0f || outHeight <= 0f) {
                if (viewBox != null) {
                    outWidth = viewBox.width()
                    outHeight = viewBox.height()
                } else {
                    outWidth = 1024f
                    outHeight = 768f
                }
            }
            
            // Add padding for centering - consistent with WebView approach
            val padding = 32f // 8px internal + 24px external margin
            val canvasWidth = outWidth + padding * 2
            val canvasHeight = outHeight + padding * 2
            
            outWidth = canvasWidth.coerceAtLeast(1f).coerceAtMost(8000f)
            outHeight = canvasHeight.coerceAtLeast(1f).coerceAtMost(8000f)
            
            val bitmap = android.graphics.Bitmap.createBitmap(outWidth.toInt(), outHeight.toInt(), android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(android.graphics.Color.WHITE)
            

            canvas.translate(padding, padding)

            try { svg.setDocumentWidth(outWidth - padding * 2) } catch (_: Exception) {}
            try { svg.setDocumentHeight(outHeight - padding * 2) } catch (_: Exception) {}
            
            svg.renderToCanvas(canvas)
            val os = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, os)
            val bytes = os.toByteArray()
            os.close()
            bitmap.recycle()
            bytes
        } catch (e: Exception) {
            Log.e("MermaidPreview", "renderSvgToPng failed", e)
            null
        }
    }

    private fun String.decodeUnicodeEscapes(): String {
        val input = this
        val builder = StringBuilder(input.length)
        var i = 0
        while (i < input.length) {
            val ch = input[i]
            if (ch == '\\') {

                var j = i
                while (j < input.length && input[j] == '\\') j++
                val backslashCount = j - i
                val nextIsU = (j < input.length && input[j] == 'u')
                if (nextIsU && j + 4 < input.length) {
                    val hex = input.substring(j + 1, j + 5)
                    val code = hex.toIntOrNull(16)
                    if (code != null) {

                        val emitBackslashes = (backslashCount - 1).coerceAtLeast(0) / 2
                        repeat(emitBackslashes) { builder.append('\\') }
                        builder.append(code.toChar())
                        i = j + 5
                        continue
                    }
                }

                repeat(backslashCount) { builder.append('\\') }
                i = j
                continue
            }
            builder.append(ch)
            i++
        }
        return builder.toString()
    }

    /**
     * Export rendered diagram as PNG
     */
    fun exportPNG(
        fileName: String = "diagram.png",
        fileExportService: com.example.mermaidmaker.domain.service.FileExportService,
        onResult: (Boolean) -> Unit
    ) {
        generatePNG { pngData ->
            if (pngData != null && pngData.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    fileExportService.exportPng(pngData, fileName) { uri ->
                        val success = uri != null
                        Log.d("MermaidPreview", if (success) "PNG exported successfully to: $uri" else "PNG export failed")
                        onResult(success)
                    }
                }
            } else {
                Log.e("MermaidPreview", "Failed to generate PNG data")
                onResult(false)
            }
        }
    }

    /**
     * Share rendered diagram as PNG
     */
    fun sharePNG(
        fileName: String = "diagram.png",
        fileExportService: com.example.mermaidmaker.domain.service.FileExportService,
        onResult: (Boolean) -> Unit
    ) {
        generatePNG { pngData ->
            if (pngData != null && pngData.isNotEmpty()) {
                CoroutineScope(Dispatchers.Main).launch {
                    val success = fileExportService.sharePng(pngData, fileName)
                    Log.d("MermaidPreview", if (success) "PNG share initiated successfully" else "PNG share failed")
                    onResult(success)
                }
            } else {
                Log.e("MermaidPreview", "Failed to generate PNG data for sharing")
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
    showControls: Boolean = false,
    zoomLevel: Int = 100
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
    
    // Apply zoom when it changes
    LaunchedEffect(zoomLevel, isReady) {
        if (isReady) {
            val scale = zoomLevel / 100f
            state.setZoom(scale)
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
        // Ensure transparent background to avoid white areas behind SVG
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
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
