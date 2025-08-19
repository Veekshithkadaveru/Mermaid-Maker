package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.ui.theme.MermaidMakerTheme

/**
 * Preview component for the Mermaid Editor
 */
@Preview(showBackground = true)
@Composable
fun MermaidEditorPreview() {
    MermaidMakerTheme {
        val editorState = rememberMermaidEditorState(
            initialContent = """
                graph TD
                    A[Start] --> B{Is it working?}
                    B -->|Yes| C[Great!]
                    B -->|No| D[Debug]
                    D --> A
                    C --> E[End]
            """.trimIndent()
        )
        
        var content by remember { mutableStateOf("") }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Mermaid Editor Preview",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                MermaidEditor(
                    content = editorState.content,
                    onContentChanged = { newContent ->
                        content = newContent
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Content length: ${content.length} characters",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Standalone Mermaid Editor Screen for testing
 */
@Composable
fun MermaidEditorTestScreen() {
    val editorState = rememberMermaidEditorState()
    var showTemplate by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Toolbar
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { 
                        editorState.insertTemplate("""
                            graph TD
                                A[Start] --> B[Process]
                                B --> C[End]
                        """.trimIndent())
                    }
                ) {
                    Text("Flowchart")
                }
                
                Button(
                    onClick = { 
                        editorState.insertTemplate("""
                            sequenceDiagram
                                participant A as Alice
                                participant B as Bob
                                A->>B: Hello Bob!
                                B-->>A: Hello Alice!
                        """.trimIndent())
                    }
                ) {
                    Text("Sequence")
                }
                
                Button(
                    onClick = { editorState.clear() }
                ) {
                    Text("Clear")
                }
            }
        }
        
        // Editor
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            MermaidEditor(
                content = editorState.content,
                onContentChanged = { /* Handle content changes */ },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}