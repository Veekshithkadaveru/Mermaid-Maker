package com.example.mermaidmaker.ui.editor

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.mermaidmaker.ui.components.ProfessionalFloatingActionButton
import com.example.mermaidmaker.ui.components.ProfessionalLoadingOverlay

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun PreviewTab(
    content: String,
    previewState: com.example.mermaidmaker.ui.preview.MermaidPreviewState,
    onFixWithAi: (String) -> Unit = {},
    onExplain: () -> Unit = {},
    onCloseExplanation: () -> Unit = {},
    onExportPng: () -> Unit = {},
    onSharePng: () -> Unit = {},
    onExportSvg: () -> Unit = {},
    onShareSvg: () -> Unit = {},
    isExportingPng: Boolean = false,
    isSharingPng: Boolean = false,
    isExportingSvg: Boolean = false,
    isSharingSvg: Boolean = false,
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
                                    painter = rememberVectorPainter(image = Icons.Filled.Image),
                                    contentDescription = "Download PNG"
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // SVG Download
                        IconButton(
                            onClick = {
                                Log.d("MainEditorScreen", "SVG Download button clicked")
                                onExportSvg()
                            },
                            enabled = !isExportingSvg
                        ) {
                            if (isExportingSvg) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                androidx.compose.foundation.Image(
                                    painter = rememberVectorPainter(image = Icons.Filled.Code),
                                    contentDescription = "Download SVG"
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

                        Spacer(Modifier.width(8.dp))

                        // SVG Share  
                        IconButton(
                            onClick = {
                                Log.d("MainEditorScreen", "SVG Share button clicked")
                                onShareSvg()
                            },
                            enabled = !isSharingSvg
                        ) {
                            if (isSharingSvg) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                androidx.compose.foundation.Image(
                                    painter = rememberVectorPainter(image = Icons.Filled.Share),
                                    contentDescription = "Share SVG"
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

                    if (explainError != null) {
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
                    }

                    // Clean FAB and explanation overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Always visible Extended FAB
                        if (!isExplaining && explanation?.summary?.isBlank() != false) {
                            ProfessionalFloatingActionButton(
                                onClick = onExplain,
                                icon = Icons.Filled.Psychology,
                                enabled = content.isNotBlank() && !isExplaining,
                                label = "Explain Diagram",
                                modifier = Modifier.align(Alignment.BottomEnd)
                            )
                        }

                        // Explanation loading overlay
                        if (isExplaining) {
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .width(280.dp)
                                    .heightIn(min = 120.dp, max = 160.dp),
                                shape = RoundedCornerShape(16.dp),
                                tonalElevation = 8.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(strokeWidth = 2.dp)
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            "Explaining diagram...", 
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }

                        // Explanation result card
                        if (explanation?.summary?.isNotBlank() == true) {
                            BoxWithConstraints(modifier = Modifier.align(Alignment.Center)) {
                                val screenWidth = maxWidth
                                val screenHeight = maxHeight
                                val targetWidth = (screenWidth * 0.92f)
                                val maxHeight = if (screenHeight * 0.7f < 420.dp) screenHeight * 0.7f else 420.dp

                                Surface(
                                    modifier = Modifier
                                        .width(targetWidth)
                                        .heightIn(min = 120.dp, max = maxHeight)
                                        .animateContentSize(animationSpec = tween(360, easing = FastOutSlowInEasing)),
                                    shape = RoundedCornerShape(16.dp),
                                    tonalElevation = 8.dp
                                ) {
                                    Box {
                                        IconButton(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp),
                                            onClick = onCloseExplanation
                                        ) {
                                            androidx.compose.foundation.Image(
                                                painter = rememberVectorPainter(image = Icons.Filled.Close),
                                                contentDescription = "Close explanation"
                                            )
                                        }
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val title = explanation?.title ?: "Diagram explanation"
                                            Text(
                                                title,
                                                style = MaterialTheme.typography.titleMedium,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                explanation?.summary.orEmpty(),
                                                textAlign = TextAlign.Center,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
