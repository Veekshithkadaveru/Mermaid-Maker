package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Simple test screen for debugging the Mermaid editor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MermaidEditorDebugScreen() {
    var isEditorReady by remember { mutableStateOf(false) }
    var contentLength by remember { mutableStateOf(0) }
    
    val testContent = """
        graph TD
            A[Start] --> B{Decision}
            B -->|Yes| C[Success]
            B -->|No| D[Try Again]
            C --> E[End]
            D --> A
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Status indicators
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Mermaid Editor Debug Screen",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Editor Ready: ${if (isEditorReady) "✅" else "❌"}",
                    color = if (isEditorReady) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Text("Content Length: $contentLength characters")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Editor
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            MermaidEditor(
                content = testContent,
                onContentChanged = { newContent ->
                    contentLength = newContent.length
                },
                onCursorPositionChanged = { line, ch ->
                    // Debug cursor position if needed
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Test buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* Will be handled by editor state */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Load Test Content")
            }
            
            Button(
                onClick = { /* Clear editor */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }
        }
    }
}