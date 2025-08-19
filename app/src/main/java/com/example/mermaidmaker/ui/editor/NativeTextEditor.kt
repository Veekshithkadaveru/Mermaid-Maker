package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Native text editor using Compose TextField instead of WebView
 * This ensures reliable touch handling and text input
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeTextEditor(
    content: String = "",
    onContentChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(content) }
    
    // Update text field when content prop changes
    LaunchedEffect(content) {
        if (content != textFieldValue) {
            textFieldValue = content
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Status bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Native Editor ✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${textFieldValue.length} characters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Text editor
        val customTextSelectionColors = TextSelectionColors(
            handleColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    onContentChanged(newValue)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                placeholder = {
                    Text(
                        text = """Type your Mermaid diagram here...

Examples:
graph TD
    A[Start] --> B[Process]
    B --> C[End]

sequenceDiagram
    Alice->>Bob: Hello Bob!
    Bob-->>Alice: Hello Alice!""",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

/**
 * Simple syntax highlighting preview (visual only)
 */
@Composable
fun SyntaxHighlightPreview(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "Preview (Basic highlighting)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            
            // Simple text preview with monospace font
            SelectionContainer {
                Text(
                    text = text.ifEmpty { "Type code above to see preview..." },
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = if (text.isEmpty()) 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Enhanced editor with preview
 */
@Composable
fun NativeTextEditorWithPreview(
    content: String = "",
    onContentChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentContent by remember { mutableStateOf(content) }
    
    Column(modifier = modifier) {
        // Main editor (takes most space)
        NativeTextEditor(
            content = currentContent,
            onContentChanged = { newContent ->
                currentContent = newContent
                onContentChanged(newContent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.7f)
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Preview (smaller section)
        SyntaxHighlightPreview(
            text = currentContent,
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.3f)
        )
    }
}