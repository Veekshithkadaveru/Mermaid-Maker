package com.example.mermaidmaker.ui.diagrams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.ui.editor.MermaidEditor
import com.example.mermaidmaker.ui.editor.MermaidEditorViewModel
import com.example.mermaidmaker.ui.editor.rememberMermaidEditorState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDiagramScreen(
    navController: NavController,
    viewModel: CreateDiagramViewModel = koinViewModel(),
    editorViewModel: MermaidEditorViewModel = koinViewModel()
) {
    var title by remember { mutableStateOf("") }
    val isSaving by viewModel.isSaving.collectAsState()
    val editorState = rememberMermaidEditorState(
        initialContent = ""
    )
    
    val selectedDiagramType by editorViewModel.selectedDiagramType.collectAsState()
    val availableTemplates by editorViewModel.availableTemplates.collectAsState()
    val editorContent by editorViewModel.editorContent.collectAsState()
    
    // Load initial template when screen first loads
    LaunchedEffect(Unit) {
        val initialTemplate = editorViewModel.generateBasicTemplate()
        editorState.setContent(initialTemplate)
        editorViewModel.updateContent(initialTemplate)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "New Diagram", 
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title input
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = { title = it },
                label = { Text("Diagram Title") },
                placeholder = { Text("Enter a descriptive title") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Diagram type selector
            Text(
                text = "Diagram Type",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Template selector
            if (availableTemplates.isNotEmpty()) {
                Text(
                    text = "Quick Templates",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
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
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Editor
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mermaid Code",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap to edit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Use enhanced editor with syntax highlighting
                com.example.mermaidmaker.ui.editor.SyntaxHighlightedEditor(
                    content = editorState.content,
                    onContentChanged = { content ->
                        editorViewModel.updateContent(content)
                        editorState.setContent(content) // Update state as well
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                            onSaved = { 
                                navController.popBackStack()
                            },
                            onError = { /* TODO: show snackbar */ }
                        )
                    }
                }
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Creating...")
                } else {
                    Text("Create Diagram")
                }
            }
        }
    }
}

