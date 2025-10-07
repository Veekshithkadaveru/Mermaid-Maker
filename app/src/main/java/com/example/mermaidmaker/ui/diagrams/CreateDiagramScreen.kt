package com.example.mermaidmaker.ui.diagrams

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.ui.editor.MermaidEditorViewModel
import com.example.mermaidmaker.ui.editor.rememberMermaidEditorState
import com.example.mermaidmaker.ui.preview.MermaidPreview
import com.example.mermaidmaker.ui.preview.rememberMermaidPreviewState
import com.example.mermaidmaker.ui.components.ProfessionalTopBar
import com.example.mermaidmaker.ui.theme.Spacing
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDiagramScreen(
    navController: NavController,
    viewModel: CreateDiagramViewModel = koinViewModel(),
    editorViewModel: MermaidEditorViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    var showFullscreenPreview by remember { mutableStateOf(false) }
    val isSaving by viewModel.isSaving.collectAsState()
    val editorState = rememberMermaidEditorState(
        initialContent = ""
    )
    val previewState = rememberMermaidPreviewState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val selectedDiagramType by editorViewModel.selectedDiagramType.collectAsState()
    val availableTemplates by editorViewModel.availableTemplates.collectAsState()
    val editorContent by editorViewModel.editorContent.collectAsState()
    // AI indicators removed for now (handled in a separate PR)

    // Load initial template when screen first loads
    LaunchedEffect(Unit) {
        val initialTemplate = editorViewModel.generateBasicTemplate()
        editorState.setContent(initialTemplate)
        editorViewModel.updateContent(initialTemplate)
    }

    // Open/Save actions moved into editor header

    Scaffold(
        topBar = { ProfessionalTopBar(title = "New Diagram") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.lg)
        ) {
            Spacer(modifier = Modifier.height(Spacing.md))

            // Title input
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = { title = it },
                label = { Text("Diagram Title") },
                placeholder = { Text("Enter a descriptive title") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(Spacing.md))

            // Diagram type selector
            Text(
                text = "Diagram Type",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(Spacing.sm))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(DiagramType.values()) { type ->
                    FilterChip(
                        onClick = {
                            editorViewModel.setDiagramType(type)
                            val basicTemplate = editorViewModel.generateBasicTemplate()
                            editorState.setContent(basicTemplate)
                        },
                        label = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        selected = selectedDiagramType == type
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Template selector
            if (availableTemplates.isNotEmpty()) {
                Text(
                    text = "Quick Templates",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(Spacing.sm))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedButton(
                            onClick = {
                                val basicTemplate = editorViewModel.generateBasicTemplate()
                                editorState.setContent(basicTemplate)
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Basic")
                        }
                    }

                    items(availableTemplates.take(3)) { template ->
                        OutlinedButton(
                            onClick = {
                                editorState.setContent(template.content)
                            }
                        ) {
                            Text(
                                text = template.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            // File actions removed; handled in editor header

            // Cleaner UI: Single-pane tabs (Edit / Preview)
            var selectedTab by remember { mutableStateOf(0) }
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Edit") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Preview") }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (selectedTab == 0) {
                    // Editor only
                    com.example.mermaidmaker.ui.editor.SyntaxHighlightedEditor(
                        content = editorState.content,
                        onContentChanged = { content ->
                            editorViewModel.updateContent(content)
                            editorState.setContent(content)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Preview only
                    MermaidPreview(
                        content = editorContent,
                        state = previewState,
                        modifier = Modifier.fillMaxSize(),
                        showControls = false
                    )
                }
            }

            // Fullscreen preview dialog
            if (showFullscreenPreview) {
                Dialog(
                    onDismissRequest = { showFullscreenPreview = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TopAppBar(title = { Text("Preview") }, actions = {
                                TextButton(onClick = {
                                    showFullscreenPreview = false
                                }) { Text("Close") }
                            })
                            MermaidPreview(
                                content = editorContent,
                                state = rememberMermaidPreviewState(),
                                modifier = Modifier
                                    .fillMaxSize(),
                                showControls = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // Create button
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && editorContent.isNotBlank() && !isSaving,
                onClick = {
                    editorState.getCurrentContent { content ->
                        viewModel.create(
                            title = title,
                            content = content,
                            type = selectedDiagramType,
                            onSaved = { savedDiagram ->
                                // Show success message
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Diagram '${savedDiagram.title}' created successfully!",
                                        actionLabel = "View"
                                    )
                                    kotlinx.coroutines.delay(1000) // Brief delay to show message
                                    navController.popBackStack()
                                }
                            },
                            onError = { error ->
                                // Show error snackbar
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Failed to create diagram: ${error.message}",
                                        actionLabel = "Dismiss"
                                    )
                                }
                            }
                        )
                    }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(Spacing.sm))
                    Text("Creating...")
                } else {
                    Text("Create Diagram")
                }
            }
        }
    }
}

