package com.example.mermaidmaker.ui.editor

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.ui.ai.AiGenerationTab
import com.example.mermaidmaker.ui.preview.rememberMermaidPreviewState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainEditorScreen(
    diagramId: String? = null,
    viewModel: MermaidEditorViewModel = koinViewModel()
) {
    val fileExportService: com.example.mermaidmaker.domain.service.FileExportService = koinInject()
    var selectedTabIndex by remember { mutableStateOf(1) } // Start with "Code" tab
    var showExampleDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    val editorContent by viewModel.editorContent.collectAsState()
    val selectedDiagramType by viewModel.selectedDiagramType.collectAsState()
    val availableTemplates by viewModel.availableTemplates.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiErrorMessage by viewModel.aiErrorMessage.collectAsState()
    val isAiAvailable by viewModel.isAiAvailable.collectAsState()

    val context = LocalContext.current

    val previewState = rememberMermaidPreviewState()
    val editorState = rememberMermaidEditorState(
        initialContent = editorContent
    )
    val snackbarHostState = remember { SnackbarHostState() }

    val tabs = listOf("Text", "Code", "example", "Preview")

    // Initialize with basic template when first loaded or load existing diagram
    LaunchedEffect(diagramId) {
        if (diagramId != null) {
            // Load existing diagram
            viewModel.loadDiagram(diagramId)
        } else if (editorContent.isEmpty()) {
            // Create new diagram with basic template
            val initialTemplate = viewModel.generateBasicTemplate()
            viewModel.updateContent(initialTemplate)
            editorState.setContent(initialTemplate)
        }
    }

    // Update editor state when content changes from ViewModel
    LaunchedEffect(editorContent) {
        if (editorContent.isNotEmpty()) {
            editorState.setContent(editorContent)
        }
    }

    // Show error messages as snackbars
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                actionLabel = "Dismiss"
            )
            // Clear the error after showing
            viewModel.clearError()
        }
    }

    // Refresh AI availability when screen is resumed
    LaunchedEffect(Unit) {
        viewModel.refreshAiAvailability()
    }

    // Auto-navigate to Code tab when AI generation completes successfully
    LaunchedEffect(isAiGenerating) {
        if (!isAiGenerating && editorContent.isNotBlank() && selectedTabIndex == 0) {
            selectedTabIndex = 1 // Switch to Code tab
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Compact Top App Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "Mermaid Maker",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            if (title == "example") {
                                showExampleDialog = true
                            } else {
                                selectedTabIndex = index
                            }
                        },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // Refresh AI availability when Text tab is selected
                        LaunchedEffect(selectedTabIndex) {
                            if (selectedTabIndex == 0) {
                                viewModel.refreshAiAvailability()
                            }
                        }


                        AiGenerationTab(
                            selectedDiagramType = selectedDiagramType,
                            onDiagramTypeSelected = { viewModel.setDiagramType(it) },
                            onGenerateClick = { prompt, diagramType ->
                                viewModel.generateAiDiagram(prompt, diagramType)
                            },
                            onSettingsClick = {
                                // TODO: Navigate to settings
                            },
                            onRefreshClick = {
                                viewModel.refreshAiAvailability()
                            },
                            isGenerating = isAiGenerating,
                            errorMessage = aiErrorMessage,
                            isAiAvailable = isAiAvailable
                        )
                    }

                    1 -> CodeTab(
                        content = editorContent,
                        fontSize = fontSize,
                        onContentChanged = { content ->
                            viewModel.updateContent(content)
                            editorState.setContent(content)
                        }
                    )

                    2 -> {
                        // Show code content when example tab is "selected" but dialog handles the actual selection
                        CodeTab(
                            content = editorContent,
                            fontSize = fontSize,
                            onContentChanged = { content ->
                                viewModel.updateContent(content)
                                editorState.setContent(content)
                            }
                        )
                    }

                    3 -> PreviewTab(
                        content = editorContent,
                        previewState = previewState,
                        fileExportService = fileExportService
                    )
                }
            }

            // Bottom controls section - only show for Code tab
            if (selectedTabIndex == 1) { // Code tab
                BottomControlsSection(
                    viewModel = viewModel,
                    context = context,
                    editorState = editorState,
                    onFontSizeClick = { showFontSizeDialog = true }
                )
            }
        }

        // Example Dialog
        if (showExampleDialog) {
            ExampleSelectionDialog(
                selectedDiagramType = selectedDiagramType,
                availableTemplates = availableTemplates,
                onDiagramTypeSelected = { type ->
                    viewModel.setDiagramType(type)
                    val template = viewModel.generateBasicTemplate()
                    viewModel.updateContent(template)
                    editorState.setContent(template)
                    selectedTabIndex = 1 // Switch to Code tab
                },
                onTemplateSelected = { template ->
                    viewModel.updateContent(template.content)
                    editorState.setContent(template.content)
                    selectedTabIndex = 1 // Switch to Code tab
                    showExampleDialog = false
                },
                onDismiss = { showExampleDialog = false }
            )
        }

        // Font Size Dialog
        if (showFontSizeDialog) {
            FontSizeSelectionDialog(
                currentFontSize = fontSize,
                onFontSizeSelected = { newSize ->
                    viewModel.setFontSize(newSize)
                    showFontSizeDialog = false
                },
                onDismiss = { showFontSizeDialog = false }
            )
        }

        // Loading overlay
        if (isLoading) {
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
                            text = "Loading diagram...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Full-screen AI generation overlay
        if (isAiGenerating) {
            FullScreenAiLoadingOverlay()
        }

        // Snackbar host at bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


@Composable
private fun CodeTab(
    content: String,
    fontSize: Int,
    onContentChanged: (String) -> Unit
) {
    SyntaxHighlightedEditor(
        content = content,
        fontSize = fontSize,
        onContentChanged = onContentChanged,
        modifier = Modifier.fillMaxSize()
    )
}


@Composable
private fun DiagramTypeCard(
    diagramType: DiagramType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = diagramType.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = getDiagramTypeDescription(diagramType),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PreviewTab(
    content: String,
    previewState: com.example.mermaidmaker.ui.preview.MermaidPreviewState,
    fileExportService: com.example.mermaidmaker.domain.service.FileExportService
) {
    var zoomLevel by remember { mutableStateOf(100) }
    val isPreviewLoading by previewState.isLoading.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zoom controls
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                // Export/Share controls
                if (content.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // PNG Download
                        IconButton(
                            onClick = {
                                Log.d("MainEditorScreen", "PNG Download button clicked")
                                previewState.exportPNG(
                                    fileName = "mermaid_diagram.png",
                                    fileExportService = fileExportService
                                ) { success ->
                                    Log.d("MainEditorScreen", "PNG download result: $success")
                                }
                            }
                        ) {
                            androidx.compose.foundation.Image(
                                painter = rememberVectorPainter(image = Icons.Filled.FileDownload),
                                contentDescription = "Download PNG"
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // PNG Share  
                        IconButton(
                            onClick = {
                                Log.d("MainEditorScreen", "PNG Share button clicked")
                                previewState.sharePNG(
                                    fileName = "mermaid_diagram.png",
                                    fileExportService = fileExportService
                                ) { success ->
                                    Log.d("MainEditorScreen", "PNG share result: $success")
                                }
                            }
                        ) {
                            androidx.compose.foundation.Image(
                                painter = rememberVectorPainter(image = Icons.Filled.Share),
                                contentDescription = "Share PNG"
                            )
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
                    FullScreenMermaidPreview(
                        content = content,
                        zoomLevel = zoomLevel,
                        modifier = Modifier
                            .fillMaxSize(),
                        previewState = previewState
                    )
                    
                    // Loading overlay for preview
                    if (isPreviewLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Text(
                                        text = "Rendering diagram...",
                                        style = MaterialTheme.typography.bodyLarge
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

@Composable
private fun BottomControlsSection(
    viewModel: MermaidEditorViewModel,
    context: android.content.Context,
    editorState: MermaidEditorState,
    onFontSizeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                viewModel.clearContent()
                editorState.clear()
            }) {
                Text("clear")
            }

            Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)

            TextButton(onClick = {
                viewModel.copyToClipboard(context)
            }) {
                Text("copy")
            }

            Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)

            TextButton(onClick = {
                val updatedContent = viewModel.pasteFromClipboard(context)
                editorState.setContent(updatedContent)
            }) {
                Text("paste")
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = onFontSizeClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    "font",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
private fun FullScreenMermaidPreview(
    content: String,
    zoomLevel: Int,
    modifier: Modifier = Modifier,
    previewState: com.example.mermaidmaker.ui.preview.MermaidPreviewState? = null
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
            }, previewState = previewState)
            newWebView
        },
        update = { webView ->
            if (isReady) {

                val scale = zoomLevel / 100f
                webView.evaluateJavascript("setScale($scale);", null)
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
private fun setupFullScreenWebView(
    webView: WebView, 
    onReady: () -> Unit,
    previewState: com.example.mermaidmaker.ui.preview.MermaidPreviewState? = null
) {
    webView.apply {
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false
            loadWithOverviewMode = true
            useWideViewPort = true
            safeBrowsingEnabled = true
            textZoom = 100
            minimumFontSize = 12
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

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
        }

        addJavascriptInterface(jsInterface, "Android")

        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return !(url as String).startsWith("file:///android_asset/")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // WebView ready callback will be called from JavaScript
            }

            @Deprecated("Deprecated in API level 24")
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                Log.e("FullScreenMermaidPreview", "WebView error: $errorCode - $description")
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d(
                    "FullScreenMermaidPreview",
                    "Console: ${consoleMessage.message()} at ${consoleMessage.sourceId()}:${consoleMessage.lineNumber()}"
                )
                return true
            }
        }

        // Use the same HTML as regular preview for consistent functionality
        loadUrl("file:///android_asset/mermaid_preview.html")
    }
}

private fun String.escapeForJs(): String {
    return this
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("$", "\\$")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}

@Composable
private fun ExampleSelectionDialog(
    selectedDiagramType: DiagramType,
    availableTemplates: List<com.example.mermaidmaker.domain.model.Template>,
    onDiagramTypeSelected: (DiagramType) -> Unit,
    onTemplateSelected: (com.example.mermaidmaker.domain.model.Template) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Diagram examples",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Diagram type selection grid
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(DiagramType.values().toList().chunked(2)) { rowTypes ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowTypes.forEach { diagramType ->
                                ExampleButton(
                                    text = getDiagramTypeDisplayName(diagramType),
                                    isSelected = selectedDiagramType == diagramType,
                                    onClick = { onDiagramTypeSelected(diagramType) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if odd number of items
                            if (rowTypes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Show templates for selected type
                if (availableTemplates.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Templates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(availableTemplates.take(5)) { template ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onTemplateSelected(template) },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = template.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExampleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun getDiagramTypeDisplayName(diagramType: DiagramType): String {
    return when (diagramType) {
        DiagramType.FLOWCHART -> "Flowchart"
        DiagramType.SEQUENCE -> "Sequence Diagram"
        DiagramType.CLASS -> "Class Diagram"
        DiagramType.STATE -> "State Diagram"
        DiagramType.ER_DIAGRAM -> "ER Diagram"
        DiagramType.GITGRAPH -> "Gitgraph"
        DiagramType.JOURNEY -> "Journey"
        DiagramType.GANTT -> "Gantt"
        DiagramType.PIE -> "Pie Chart"
    }
}

private fun getDiagramTypeDescription(diagramType: DiagramType): String {
    return when (diagramType) {
        DiagramType.FLOWCHART -> "Process flows and decisions"
        DiagramType.SEQUENCE -> "Interactions over time"
        DiagramType.CLASS -> "Object-oriented structures"
        DiagramType.STATE -> "State transitions"
        DiagramType.ER_DIAGRAM -> "Database relationships"
        DiagramType.GITGRAPH -> "Git workflow visualization"
        DiagramType.JOURNEY -> "User experience flows"
        DiagramType.GANTT -> "Project timelines"
        DiagramType.PIE -> "Data proportions"
    }
}

@Composable
private fun FontSizeSelectionDialog(
    currentFontSize: Int,
    onFontSizeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .padding(4.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set font size",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(16.dp)
                    ) {
                        Text(
                            "×",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Font size options
                val fontSizes = listOf(12, 14, 16, 18, 21, 24, 27, 30, 36)

                LazyColumn(
                    modifier = Modifier.heightIn(max = 468.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(fontSizes) { fontSize ->
                        FontSizeOption(
                            fontSize = fontSize,
                            isSelected = fontSize == currentFontSize,
                            onClick = { onFontSizeSelected(fontSize) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FontSizeOption(
    fontSize: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                modifier = Modifier.size(20.dp),
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "font size $fontSize",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun FullScreenAiLoadingOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_loading")
    
    // Advanced animation values for full-screen experience
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val backgroundAlpha by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "background_alpha"
    )
    
    val textGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_glow"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        surfaceColor.copy(alpha = backgroundAlpha),
                        primaryColor.copy(alpha = backgroundAlpha * 0.1f),
                        secondaryColor.copy(alpha = backgroundAlpha * 0.05f)
                    ),
                    radius = 1500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Main loading indicator - larger for full screen
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                FullScreenLoadingIndicator(
                    rotationAngle = rotationAngle,
                    pulseScale = pulseScale
                )
            }
            
            // Professional branding and messaging
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🧠",
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.alpha(textGlow)
                )
                
                Text(
                    text = "AI THINKING",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    letterSpacing = 4.sp,
                    modifier = Modifier.alpha(textGlow)
                )
                
                Text(
                    text = "Crafting your perfect diagram",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alpha(textGlow * 0.9f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "ANALYZING • PROCESSING • GENERATING",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    letterSpacing = 2.sp,
                    modifier = Modifier.alpha(textGlow * 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Please wait while our AI creates your diagram...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(textGlow * 0.7f)
                )
            }
        }
    }
}

@Composable
private fun FullScreenLoadingIndicator(
    rotationAngle: Float,
    pulseScale: Float
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Canvas(
        modifier = Modifier.size(200.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val baseRadius = size.minDimension / 8
        
        // Outer orbital ring with multiple particles
        rotate(rotationAngle, center) {
            for (i in 0 until 12) {
                val angle = i * 30f
                val rad = Math.toRadians(angle.toDouble())
                val distance = baseRadius * 4f * pulseScale
                val particleCenter = Offset(
                    center.x + kotlin.math.cos(rad).toFloat() * distance,
                    center.y + kotlin.math.sin(rad).toFloat() * distance
                )
                
                val alpha = (kotlin.math.sin(Math.toRadians((rotationAngle + angle).toDouble())).toFloat() + 1f) / 2f
                drawCircle(
                    color = primaryColor.copy(alpha = alpha * 0.9f),
                    radius = baseRadius * 0.4f * (0.8f + alpha * 0.4f),
                    center = particleCenter
                )
            }
        }
        
        // Middle ring counter-rotating
        rotate(-rotationAngle * 0.6f, center) {
            for (i in 0 until 8) {
                val angle = i * 45f
                val rad = Math.toRadians(angle.toDouble())
                val distance = baseRadius * 2.8f
                val particleCenter = Offset(
                    center.x + kotlin.math.cos(rad).toFloat() * distance,
                    center.y + kotlin.math.sin(rad).toFloat() * distance
                )
                
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.8f),
                    radius = baseRadius * 0.35f * pulseScale,
                    center = particleCenter
                )
            }
        }
        
        // Inner ring with faster rotation
        rotate(rotationAngle * 1.5f, center) {
            for (i in 0 until 6) {
                val angle = i * 60f
                val rad = Math.toRadians(angle.toDouble())
                val distance = baseRadius * 1.8f
                val particleCenter = Offset(
                    center.x + kotlin.math.cos(rad).toFloat() * distance,
                    center.y + kotlin.math.sin(rad).toFloat() * distance
                )
                
                drawCircle(
                    color = tertiaryColor.copy(alpha = 0.7f),
                    radius = baseRadius * 0.25f * pulseScale,
                    center = particleCenter
                )
            }
        }
        
        // Central core with radial gradient
        val gradientColors = listOf(
            primaryColor.copy(alpha = 1f),
            secondaryColor.copy(alpha = 0.8f),
            tertiaryColor.copy(alpha = 0.6f),
            Color.Transparent
        )
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = gradientColors,
                radius = baseRadius * 1.5f * pulseScale
            ),
            radius = baseRadius * 1.2f * pulseScale,
            center = center
        )
        
        // Central bright highlight
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = baseRadius * 0.6f * pulseScale,
            center = center
        )
        
        // Very center dot
        drawCircle(
            color = primaryColor.copy(alpha = 0.9f),
            radius = baseRadius * 0.3f,
            center = center
        )
    }
}
