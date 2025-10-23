package com.example.mermaidmaker.ui.editor

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mermaidmaker.ui.common.WebViewUtils
import com.example.mermaidmaker.data.processing.*
import com.example.mermaidmaker.ui.components.ProfessionalLoadingOverlay
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert

@Composable
fun PreviewTab(
    content: String,
    previewState: com.example.mermaidmaker.ui.preview.MermaidPreviewState,
    fileExportService: com.example.mermaidmaker.domain.service.FileExportService,
    onShowSnackbar: (String) -> Unit,
    onFixWithAi: (String) -> Unit = {},
    onExplain: () -> Unit = {},
    onCloseExplanation: () -> Unit = {},
    onExportPng: () -> Unit = {},
    onSharePng: () -> Unit = {},
    isExportingPng: Boolean = false,
    isSharingPng: Boolean = false,
    isExplaining: Boolean = false,
    explanation: com.example.mermaidmaker.domain.model.DiagramExplanation? = null,
    explainError: String? = null
) {
    var zoomLevel by remember { mutableStateOf(100) }
    val isPreviewLoading by previewState.isLoading.collectAsState()
    var isVertical by remember { mutableStateOf(true) }
    val supportsFlowOrGraph = remember(content) {
        Regex("(?m)^\\s*(graph|flowchart)\\b", RegexOption.IGNORE_CASE).containsMatchIn(content)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zoom controls
                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        zoomLevel = (zoomLevel - 25).coerceAtLeast(50)
                        // Also update the hidden previewState for export
                        previewState.setZoom(zoomLevel / 100f)
                    }) {
                        androidx.compose.foundation.Image(
                            painter = rememberVectorPainter(image = Icons.Filled.ZoomOut),
                            contentDescription = "Zoom out"
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = {
                        zoomLevel = (zoomLevel + 25).coerceAtMost(200)
                        // Also update the hidden previewState for export
                        previewState.setZoom(zoomLevel / 100f)
                    }) {
                        androidx.compose.foundation.Image(
                            painter = rememberVectorPainter(image = Icons.Filled.ZoomIn),
                            contentDescription = "Zoom in"
                        )
                    }
                }

                // Export/Share controls + AI actions
                if (content.isNotBlank()) {
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        // Explain button (uses existing Share icon for MVP)
                        IconButton(
                            onClick = onExplain,
                            enabled = !isExplaining
                        ) {
                            if (isExplaining) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                androidx.compose.foundation.Image(
                                    painter = rememberVectorPainter(image = Icons.Filled.Info),
                                    contentDescription = "Explain diagram"
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // PNG Download
                        IconButton(
                            onClick = {
                                Log.d("MainEditorScreen", "PNG Download button clicked")
                                onExportPng()
                            },
                            enabled = !isExportingPng
                        ) {
                            if (isExportingPng) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                androidx.compose.foundation.Image(
                                    painter = rememberVectorPainter(image = Icons.Filled.FileDownload),
                                    contentDescription = "Download PNG"
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Orientation toggle - only for flowchart/graph content
                        if (supportsFlowOrGraph) {
                            IconButton(onClick = {
                                val newIsVertical = !isVertical
                                isVertical = newIsVertical
                                val orientation = if (newIsVertical) "vertical" else "horizontal"
                                previewState.setOrientation(orientation)
                            }) {
                                val icon = if (isVertical) Icons.Filled.SwapVert else Icons.Filled.SwapHoriz
                                val desc = if (isVertical) "Switch to horizontal" else "Switch to vertical"
                                androidx.compose.foundation.Image(
                                    painter = rememberVectorPainter(image = icon),
                                    contentDescription = desc
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                        }

                        // PNG Share  
                        IconButton(
                            onClick = {
                                Log.d("MainEditorScreen", "PNG Share button clicked")
                                onSharePng()
                            },
                            enabled = !isSharingPng
                        ) {
                            if (isSharingPng) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                androidx.compose.foundation.Image(
                                    painter = rememberVectorPainter(image = Icons.Filled.Share),
                                    contentDescription = "Share PNG"
                                )
                            }
                        }

                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (content.isBlank()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No content to preview",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Switch to Code tab to add diagram content",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    com.example.mermaidmaker.ui.preview.MermaidPreview(
                        content = content,
                        state = previewState,
                        modifier = Modifier
                            .fillMaxSize(),
                        showControls = false,
                        zoomLevel = zoomLevel,
                        onFixWithAi = onFixWithAi
                    )

                    if (isPreviewLoading) {
                        ProfessionalLoadingOverlay(
                            title = "Rendering diagram...",
                            isVisible = true
                        )
                    }

                    // Explanation feedback overlay
                    if (isExplaining) {
                        ProfessionalLoadingOverlay(
                            title = "Explaining diagram...",
                            isVisible = true
                        )
                    } else if (explainError != null) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                modifier = Modifier.padding(12.dp),
                                text = explainError,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    } else if (!explanation?.summary.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                androidx.compose.foundation.layout.Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val title = explanation?.title ?: "Diagram explanation"
                                    Text(title, style = MaterialTheme.typography.titleMedium)
                                    IconButton(onClick = onCloseExplanation) {
                                        androidx.compose.foundation.Image(
                                            painter = rememberVectorPainter(image = Icons.Filled.Close),
                                            contentDescription = "Close explanation"
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(explanation?.summary ?: "")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullScreenMermaidPreview(
    content: String,
    zoomLevel: Int,
    modifier: Modifier = Modifier,
    previewState: com.example.mermaidmaker.ui.preview.MermaidPreviewState? = null,
    onFixWithAi: (String) -> Unit = {}
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isReady by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val newWebView = WebView(context)
            webView = newWebView
            setupFullScreenWebView(newWebView, onReady = {
                isReady = true

                previewState?.setWebView(newWebView)
                previewState?.setReady(true)
            }, previewState = previewState, onFixWithAi = onFixWithAi)
            newWebView
        },
        update = { wv ->
            if (isReady) {

                val scale = zoomLevel / 100f
                wv.evaluateJavascript("setScale($scale);", null)
            }
        }
    )

    // Render content when it changes
    LaunchedEffect(content, isReady) {
        if (isReady && content.isNotBlank()) {
            // Set loading state before rendering
            previewState?.setLoading(true)
            previewState?.setError(null)
            webView?.evaluateJavascript("renderMermaid(`${content.escapeForJs()}`);", null)
            // Note: Don't call previewState?.renderDiagram() here to avoid duplicate loading states
        } else if (isReady && content.isBlank()) {
            previewState?.setLoading(false)
            webView?.evaluateJavascript("clearPreview();", null)
            previewState?.clearPreview()
        }
    }

    // Apply zoom when it changes
    LaunchedEffect(zoomLevel, isReady) {
        if (isReady) {
            val scale = zoomLevel / 100f
            webView?.evaluateJavascript("setScale($scale);", null)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
fun setupFullScreenWebView(
    webView: WebView,
    onReady: () -> Unit,
    previewState: com.example.mermaidmaker.ui.preview.MermaidPreviewState? = null,
    onFixWithAi: (String) -> Unit = {}
) {
    webView.apply {
        WebViewUtils.applyCommonPreviewSettings(this)
        // JavaScript interface - compatible with MermaidPreview
        val jsInterface = object {
            @android.webkit.JavascriptInterface
            fun onWebViewReady() {
                onReady()
            }

            @android.webkit.JavascriptInterface
            fun onRenderSuccess(svgLength: Int) {
                Log.d("FullScreenMermaidPreview", "Render success: $svgLength characters")
                previewState?.setLoading(false)
                previewState?.setError(null)
            }

            @android.webkit.JavascriptInterface
            fun onRenderError(error: String) {
                Log.e("FullScreenMermaidPreview", "Render error: $error")
                previewState?.setLoading(false)
                previewState?.setError(error)
            }

            @android.webkit.JavascriptInterface
            fun onFixWithAi(source: String) {
                Log.d("FullScreenMermaidPreview", "Fix with AI requested, length=${'$'}{source.length}")
                onFixWithAi(source)
            }
        }

        addJavascriptInterface(jsInterface, "Android")
        webViewClient = WebViewUtils.createAssetsOnlyClient(tag = "FullScreenMermaidPreview")
        webChromeClient = WebViewUtils.createConsoleLoggingChromeClient(tag = "FullScreenMermaidPreview")
        loadUrl("file:///android_asset/mermaid_preview.html")
    }
}



