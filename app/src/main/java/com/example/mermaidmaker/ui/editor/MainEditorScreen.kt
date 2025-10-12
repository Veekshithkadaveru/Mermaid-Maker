package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mermaidmaker.ui.ai.AiGenerationTab
import com.example.mermaidmaker.ui.components.ProfessionalSnackbarHost
import com.example.mermaidmaker.ui.components.ProfessionalLoadingOverlay
import com.example.mermaidmaker.ui.preview.rememberMermaidPreviewState
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import com.example.mermaidmaker.ui.common.showMessage

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
    val fontSize by viewModel.fontSize.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiErrorMessage by viewModel.aiErrorMessage.collectAsState()
    val isAiAvailable by viewModel.isAiAvailable.collectAsState()
    val isAiFixing by viewModel.isAiFixing.collectAsState()
    val aiFixErrorMessage by viewModel.aiFixErrorMessage.collectAsState()
    val isAutoSaveEnabled by viewModel.isAutoSaveEnabled.collectAsState()
    val lastAutoSaveTime by viewModel.lastAutoSaveTime.collectAsState()
    val isExportingPng by viewModel.isExportingPng.collectAsState()
    val isSharingPng by viewModel.isSharingPng.collectAsState()
    val pngExportResult by viewModel.pngExportResult.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewState = rememberMermaidPreviewState()
    val editorState = rememberMermaidEditorState(
        initialContent = editorContent
    )
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    val tabs = listOf("Text", "Code", "Examples", "Preview")

    // Initialize with existing diagram if provided; otherwise, rely on ViewModel's recent load
    LaunchedEffect(diagramId) {
        if (diagramId != null) {
            viewModel.loadDiagram(diagramId)
        } else if (editorContent.isBlank()) {
            // If nothing loaded yet, ask ViewModel to load most recent diagram
            viewModel.loadMostRecent()
        }
    }

    // Update editor state when content changes from ViewModel
    LaunchedEffect(editorContent) {
        if (editorContent.isNotEmpty()) {
            editorState.setContent(editorContent)
        }
    }

    // Debounced auto-save: save 2s after typing stops
    LaunchedEffect(editorContent) {
        if (editorContent.isNotBlank()) {
            kotlinx.coroutines.delay(2_000)
            viewModel.triggerAutoSave()
        }
    }

    // Show error messages as snackbars
    LaunchedEffect(errorMessage) {
        errorMessage?.let { error ->
            snackbarHostState.showMessage(message = error, actionLabel = "Dismiss")
            // Clear the error after showing
            viewModel.clearError()
        }
    }

    // Refresh AI availability when screen is resumed
    LaunchedEffect(Unit) {
        viewModel.refreshAiAvailability()
    }

    // Trigger auto-save when app goes to background
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.triggerAutoSave()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Auto-navigate to Code tab when AI generation completes successfully
    LaunchedEffect(isAiGenerating) {
        if (!isAiGenerating && editorContent.isNotBlank() && selectedTabIndex == 0) {
            selectedTabIndex = 1 // Switch to Code tab
        }
    }

    // Auto-navigate to Code tab when AI fix completes successfully
    LaunchedEffect(isAiFixing) {
        if (!isAiFixing && editorContent.isNotBlank() && selectedTabIndex != 1) {
            selectedTabIndex = 1
        }
    }

    // Show PNG export result feedback
    LaunchedEffect(pngExportResult) {
        pngExportResult?.let { success ->
            val message = if (success) "PNG exported successfully" else "Failed to export PNG"
            snackbarScope.launch { snackbarHostState.showMessage(message) }
            viewModel.clearPngExportResult()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

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
                            if (title == "Examples") {
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

            // Auto-save Status Bar
            if (isAutoSaveEnabled && lastAutoSaveTime != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-save: ON",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Last saved: ${formatAutoSaveTime(lastAutoSaveTime)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                        },
                        onShowSnackbar = { message ->
            snackbarScope.launch { snackbarHostState.showMessage(message) }
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
                            },
                            onShowSnackbar = { message ->
                                snackbarScope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            }
                        )
                    }

                    3 -> PreviewTab(
                        content = editorContent,
                        previewState = previewState,
                        fileExportService = fileExportService,
                        onShowSnackbar = { message ->
                            snackbarScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = message
                                )
                            }
                        },
                        onFixWithAi = { source ->
                            viewModel.fixMermaidWithAi(source)
                        },
                        onExportPng = {
                            viewModel.exportDiagramAsPng(previewState)
                        },
                        onSharePng = {
                            viewModel.shareDiagramAsPng(previewState)
                        },
                        isExportingPng = isExportingPng,
                        isSharingPng = isSharingPng
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

        // Examples Dialog
        if (showExampleDialog) {
            ExampleSelectionDialog(
                selectedDiagramType = selectedDiagramType,
                onDiagramTypeSelected = { type ->
                    viewModel.setDiagramType(type)
                    val template = viewModel.generateBasicTemplate()
                    viewModel.updateContent(template)
                    editorState.setContent(template)
                    selectedTabIndex = 1 // Switch to Code tab
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

        if (isLoading) {
            ProfessionalLoadingOverlay(
                title = "Loading diagram...",
                isVisible = true
            )
        }

        // Full-screen AI generation overlay
        if (isAiGenerating) {
            FullScreenAiLoadingOverlay()
        }

        // Snackbar host at bottom
        ProfessionalSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
    }
    // Show Fix with AI error messages
    LaunchedEffect(aiFixErrorMessage) {
        aiFixErrorMessage?.let { msg ->
            snackbarScope.launch { snackbarHostState.showMessage(message = msg) }
        }
    }

    // Full-screen AI fixing overlay
    if (isAiFixing) {
        FullScreenAiLoadingOverlay()
    }
}

/**
 * Format the auto-save timestamp for display
 */
private fun formatAutoSaveTime(timestamp: Long?): String {
    if (timestamp == null) return "Never"

    val currentTime = System.currentTimeMillis()
    val timeDiff = currentTime - timestamp

    return when {
        timeDiff < 1000 * 60 -> "Just now"
        timeDiff < 1000 * 60 * 60 -> "${timeDiff / (1000 * 60)}m ago"
        else -> {
            val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            formatter.format(java.util.Date(timestamp))
        }
    }
}
