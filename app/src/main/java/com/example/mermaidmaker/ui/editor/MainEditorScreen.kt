package com.example.mermaidmaker.ui.editor

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.ui.preview.MermaidPreview
import com.example.mermaidmaker.ui.preview.rememberMermaidPreviewState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainEditorScreen(
    viewModel: MermaidEditorViewModel = koinViewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(1) } // Start with "Code" tab
    var showExampleDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    val editorContent by viewModel.editorContent.collectAsState()
    val selectedDiagramType by viewModel.selectedDiagramType.collectAsState()
    val availableTemplates by viewModel.availableTemplates.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    
    val context = LocalContext.current
    
    val previewState = rememberMermaidPreviewState()
    val editorState = rememberMermaidEditorState(
        initialContent = editorContent
    )

    val tabs = listOf("Text", "Code", "example", "Preview")

    // Initialize with basic template when first loaded
    LaunchedEffect(Unit) {
        if (editorContent.isEmpty()) {
            val initialTemplate = viewModel.generateBasicTemplate()
            viewModel.updateContent(initialTemplate)
            editorState.setContent(initialTemplate)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Compact Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 2.dp
        ) {
            Text(
                text = "AI Mermaid",
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
                0 -> TextTab()
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
                    onReturnToCode = { selectedTabIndex = 1 }
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
}

@Composable
private fun TextTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Text Mode",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Convert natural language to diagrams",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
private fun ExampleTab(
    selectedDiagramType: DiagramType,
    availableTemplates: List<com.example.mermaidmaker.domain.model.Template>,
    onDiagramTypeSelected: (DiagramType) -> Unit,
    onTemplateSelected: (com.example.mermaidmaker.domain.model.Template) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Choose Diagram Type",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Diagram type selection
        items(DiagramType.values().toList()) { diagramType ->
            DiagramTypeCard(
                diagramType = diagramType,
                isSelected = selectedDiagramType == diagramType,
                onClick = { onDiagramTypeSelected(diagramType) }
            )
        }

        // Show templates for selected type
        if (availableTemplates.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Templates for ${selectedDiagramType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(availableTemplates) { template ->
                TemplateCard(
                    template = template,
                    onClick = { onTemplateSelected(template) }
                )
            }
        }
    }
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
private fun TemplateCard(
    template: com.example.mermaidmaker.domain.model.Template,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PreviewTab(
    content: String,
    @Suppress("UNUSED_PARAMETER") previewState: com.example.mermaidmaker.ui.preview.MermaidPreviewState,
    onReturnToCode: () -> Unit = {}
) {
    var zoomLevel by remember { mutableStateOf(100) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Compact preview header with zoom indicator and return button
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
                // Compact zoom percentage indicator
                Card(
                    onClick = {
                        // Cycle through zoom levels: 50% -> 75% -> 100% -> 125% -> 150% -> 50%
                        zoomLevel = when (zoomLevel) {
                            50 -> 75
                            75 -> 100
                            100 -> 125
                            125 -> 150
                            150 -> 50
                            else -> 100
                        }
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = "${zoomLevel}%",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Compact return button
                Button(
                    onClick = onReturnToCode,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        "Return",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Preview content - now takes full available space
        Box(
            modifier = Modifier.fillMaxSize()
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
                // Custom fullscreen preview without any containers or padding
                FullScreenMermaidPreview(
                    content = content,
                    zoomLevel = zoomLevel,
                    modifier = Modifier.fillMaxSize()
                )
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
                viewModel.pasteFromClipboard(context)
                editorState.setContent(viewModel.editorContent.value)
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
    modifier: Modifier = Modifier
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isReady by remember { mutableStateOf(false) }
    
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val newWebView = WebView(context)
            webView = newWebView
            setupFullScreenWebView(newWebView) { isReady = true }
            newWebView
        },
        update = { webView ->
            if (isReady) {
                // Apply zoom level
                val scale = zoomLevel / 100f
                webView.evaluateJavascript("setScale($scale);", null)
            }
        }
    )
    
    // Render content when it changes
    LaunchedEffect(content, isReady) {
        if (isReady && content.isNotBlank()) {
            webView?.evaluateJavascript("renderMermaid(`${content.escapeForJs()}`);", null)
        } else if (isReady && content.isBlank()) {
            webView?.evaluateJavascript("clearPreview();", null)
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
private fun setupFullScreenWebView(webView: WebView, onReady: () -> Unit) {
    webView.apply {
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
        }
        
        // JavaScript interface
        val jsInterface = object {
            @android.webkit.JavascriptInterface
            fun onWebViewReady() {
                onReady()
            }
            
            @android.webkit.JavascriptInterface
            fun onRenderSuccess(@Suppress("UNUSED_PARAMETER") svgLength: Int) {
                // Success handled silently
            }
            
            @android.webkit.JavascriptInterface
            fun onRenderError(@Suppress("UNUSED_PARAMETER") error: String) {
                // Errors handled silently
            }
        }
        
        addJavascriptInterface(jsInterface, "Android")
        
        webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in API level 24")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return url?.startsWith("file:///android_asset/")?.not() ?: false
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // WebView ready callback will be called from JavaScript
            }
        }
        
        // Load the custom fullscreen HTML
        loadUrl("file:///android_asset/mermaid_preview_fullscreen.html")
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
