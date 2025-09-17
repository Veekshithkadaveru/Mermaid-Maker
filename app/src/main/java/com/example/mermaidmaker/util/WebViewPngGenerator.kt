package com.example.mermaidmaker.util

import android.util.Log
import android.webkit.WebView
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.math.roundToInt

/**
 * Utility class for generating PNG images from WebView content
 */
class WebViewPngGenerator {
    
    companion object {
        private const val TAG = "WebViewPngGenerator"
        private const val MAX_DIMENSION_PX = 8192
        private const val MAX_PIXELS = 24_000_000
    }

    /**
     * Generate PNG from WebView content with memory and size validation
     */
    suspend fun generatePng(webView: WebView, isReady: Boolean): ByteArray? {
        return suspendCancellableCoroutine { continuation ->
            if (!isReady) {
                Log.e(TAG, "WebView not ready for PNG generation")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            // Check available memory before proceeding
            if (!checkMemoryAvailability()) {
                Log.w(TAG, "Insufficient memory for PNG generation")
                continuation.resume(null)
                return@suspendCancellableCoroutine
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
                                val jsCode = buildDimensionCalculationJs()
                                webView.evaluateJavascript(jsCode) { dimensionsResult ->
                                    try {
                                        val dimensions = parseDimensions(dimensionsResult)
                                        if (!validateDimensions(dimensions)) {
                                            webView.evaluateJavascript("setScale($currentScale);", null)
                                            continuation.resume(null)
                                            return@evaluateJavascript
                                        }
                                        
                                        captureBitmap(webView, dimensions, currentScale) { result ->
                                            continuation.resume(result)
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error processing dimensions", e)
                                        webView.evaluateJavascript("setScale($currentScale);", null)
                                        continuation.resume(null)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error in PNG generation", e)
                                webView.evaluateJavascript("setScale($currentScale);", null)
                                continuation.resume(null)
                            }
                        }, 100)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error starting PNG generation", e)
                    continuation.resume(null)
                }
            }
        }
    }

    private fun checkMemoryAvailability(): Boolean {
        return MemoryUtils.checkMemoryAvailability()
    }

    private fun buildDimensionCalculationJs(): String {
        return """
            (function() {
                try {
                    const hide = (el) => { if (el) el.style.display = 'none'; };
                    hide(document.getElementById('status'));
                    hide(document.getElementById('error'));
                    hide(document.getElementById('loading'));
                    
                    const svg = document.querySelector('#preview-scale svg') || 
                                document.querySelector('#preview svg') || 
                                document.querySelector('svg');
                    
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
                        const centerPad = 24;
                        const canvasW = w2 + centerPad * 2;
                        const canvasH = h2 + centerPad * 2;
                        
                        // Apply styling for capture
                        const pc = document.querySelector('.preview-container');
                        if (pc) { 
                            pc.style.padding = '0'; 
                            pc.style.border = 'none'; 
                            pc.style.alignItems = 'flex-start'; 
                            pc.style.justifyContent = 'flex-start'; 
                        }
                        
                        const preview = document.getElementById('preview');
                        if (preview) { 
                            preview.style.padding = '0'; 
                            preview.style.margin = '0'; 
                            preview.style.width = canvasW + 'px'; 
                            preview.style.height = canvasH + 'px'; 
                        }
                        
                        const scale = document.getElementById('preview-scale');
                        if (scale) { 
                            scale.style.transform = 'scale(1)'; 
                            scale.style.width = canvasW + 'px'; 
                            scale.style.height = canvasH + 'px'; 
                        }
                        
                        document.body.style.margin = '0';
                        document.body.style.padding = '0';
                        
                        const container = document.querySelector('.container');
                        if (container) { 
                            container.style.padding = '0'; 
                            container.style.margin = '0'; 
                            container.style.height = canvasH + 'px'; 
                        }
                        
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
                        
                        return JSON.stringify({ 
                            width: canvasW, 
                            height: canvasH, 
                            dpr: (window.devicePixelRatio || 1) 
                        });
                    }
                } catch (e) {}
                return JSON.stringify({ width: 800, height: 600 });
            })();
        """.trimIndent()
    }

    private fun parseDimensions(dimensionsResult: String?): Dimensions {
        val dimensionsJson = dimensionsResult?.removeSurrounding("\"")?.replace("\\\"", "\"") 
            ?: "{\"width\":800,\"height\":600}"
        
        val json = try { 
            JSONObject(dimensionsJson) 
        } catch (e: Exception) { 
            JSONObject().apply { 
                put("width", 800)
                put("height", 600)
                put("dpr", 1.0) 
            } 
        }
        
        return Dimensions(
            cssWidth = json.optInt("width", 800),
            cssHeight = json.optInt("height", 600),
            devicePixelRatio = json.optDouble("dpr", 1.0)
        )
    }

    private fun validateDimensions(dimensions: Dimensions): Boolean {
        val svgWidth = dimensions.cssWidth.coerceAtLeast(200).coerceAtMost(8000)
        val svgHeight = dimensions.cssHeight.coerceAtLeast(200).coerceAtMost(8000)
        
        val desiredWidthPx = (svgWidth * dimensions.devicePixelRatio).toDouble()
        val desiredHeightPx = (svgHeight * dimensions.devicePixelRatio).toDouble()
        
        val totalPixels = desiredWidthPx * desiredHeightPx
        
        if (totalPixels > MAX_PIXELS) {
            Log.w(TAG, "Image too large: ${totalPixels.toLong()} pixels (max: $MAX_PIXELS)")
            return false
        }
        
        if (desiredWidthPx > MAX_DIMENSION_PX || desiredHeightPx > MAX_DIMENSION_PX) {
            Log.w(TAG, "Dimension too large: ${desiredWidthPx}x${desiredHeightPx} (max: $MAX_DIMENSION_PX)")
            return false
        }
        
        return true
    }

    private fun captureBitmap(
        webView: WebView, 
        dimensions: Dimensions, 
        currentScale: Float,
        callback: (ByteArray?) -> Unit
    ) {
        val svgWidth = dimensions.cssWidth.coerceAtLeast(200).coerceAtMost(8000)
        val svgHeight = dimensions.cssHeight.coerceAtLeast(200).coerceAtMost(8000)
        
        val originalWidth = webView.width
        val originalHeight = webView.height
        val originalLp = webView.layoutParams
        val originalLayerType = webView.layerType
        val needsResize = svgWidth != originalWidth || svgHeight != originalHeight
        
        try {
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
                    val desiredWidthPx = (svgWidth * dimensions.devicePixelRatio).toDouble()
                    val desiredHeightPx = (svgHeight * dimensions.devicePixelRatio).toDouble()
                    val scaleByDim = minOf(1.0, MAX_DIMENSION_PX.toDouble() / desiredWidthPx, MAX_DIMENSION_PX.toDouble() / desiredHeightPx)
                    val scaleByArea = kotlin.math.sqrt((MAX_PIXELS / (desiredWidthPx * desiredHeightPx)).coerceAtMost(1.0))
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
                    
                    // Restore original state
                    restoreWebViewState(webView, needsResize, originalLp, originalWidth, originalHeight, originalLayerType, currentScale)
                    
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
                    Log.e(TAG, "Error capturing bitmap", e)
                    restoreWebViewState(webView, needsResize, originalLp, originalWidth, originalHeight, originalLayerType, currentScale)
                    callback(null)
                }
            }, 100)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up bitmap capture", e)
            restoreWebViewState(webView, needsResize, originalLp, originalWidth, originalHeight, originalLayerType, currentScale)
            callback(null)
        }
    }

    private fun restoreWebViewState(
        webView: WebView,
        needsResize: Boolean,
        originalLp: android.view.ViewGroup.LayoutParams?,
        originalWidth: Int,
        originalHeight: Int,
        originalLayerType: Int,
        currentScale: Float
    ) {
        try {
            if (needsResize && originalLp != null) {
                originalLp.width = originalWidth
                originalLp.height = originalHeight
                webView.layoutParams = originalLp
            }
            val origWidthSpec = android.view.View.MeasureSpec.makeMeasureSpec(originalWidth, android.view.View.MeasureSpec.EXACTLY)
            val origHeightSpec = android.view.View.MeasureSpec.makeMeasureSpec(originalHeight, android.view.View.MeasureSpec.EXACTLY)
            webView.measure(origWidthSpec, origHeightSpec)
            webView.layout(0, 0, originalWidth, originalHeight)
            webView.setLayerType(originalLayerType, null)
            webView.evaluateJavascript("setScale($currentScale);", null)
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring WebView state", e)
        }
    }

    private data class Dimensions(
        val cssWidth: Int,
        val cssHeight: Int,
        val devicePixelRatio: Double
    )
}