package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Enhanced native editor with basic syntax highlighting
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyntaxHighlightedEditor(
    content: String = "",
    onContentChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(content)) }
    
    // Update text field when content prop changes
    LaunchedEffect(content) {
        if (content != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = content)
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
                    text = "Enhanced Editor ✓",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${textFieldValue.text.length} characters",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Enhanced text editor with syntax highlighting
        val customTextSelectionColors = TextSelectionColors(
            handleColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        
        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            Box(modifier = Modifier.weight(1f).padding(8.dp)) {
                // Syntax highlighted overlay
                if (textFieldValue.text.isNotEmpty()) {
                    Text(
                        text = applySyntaxHighlighting(textFieldValue.text),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // Actual input field (transparent)
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newValue ->
                        textFieldValue = newValue
                        onContentChanged(newValue.text)
                    },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.Transparent // Make text transparent so highlighting shows
                    ),
                    modifier = Modifier.fillMaxSize(),
                    decorationBox = { innerTextField ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (textFieldValue.text.isEmpty()) {
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
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

/**
 * Apply basic syntax highlighting to Mermaid code
 */
@Composable
private fun applySyntaxHighlighting(text: String): AnnotatedString {
    val keywordColor = Color(0xFF9C27B0) // Purple
    val nodeColor = Color(0xFF2196F3) // Blue
    val arrowColor = Color(0xFF00BCD4) // Cyan
    val stringColor = Color(0xFF4CAF50) // Green
    val commentColor = Color(0xFF757575) // Gray
    
    return buildAnnotatedString {
        val lines = text.split('\n')
        
        lines.forEachIndexed { lineIndex, line ->
            if (lineIndex > 0) append('\n')
            
            // Check for comments (lines starting with %%)
            if (line.trim().startsWith("%%")) {
                withStyle(SpanStyle(color = commentColor)) {
                    append(line)
                }
                return@forEachIndexed
            }
            
            var currentIndex = 0
            val trimmedLine = line.trim()
            
            // Keywords
            val keywords = listOf(
                "graph", "flowchart", "sequenceDiagram", "classDiagram", 
                "stateDiagram", "erDiagram", "journey", "gantt", "pie", 
                "gitgraph", "TD", "TB", "BT", "RL", "LR"
            )
            
            // Check for keywords at the beginning of lines
            keywords.forEach { keyword ->
                if (trimmedLine.startsWith(keyword)) {
                    val leadingSpaces = line.indexOf(keyword)
                    append(line.substring(0, leadingSpaces))
                    withStyle(SpanStyle(color = keywordColor)) {
                        append(keyword)
                    }
                    currentIndex = leadingSpaces + keyword.length
                }
            }
            
            if (currentIndex == 0) {
                // No keyword found, process the rest of the line
                var i = 0
                while (i < line.length) {
                    when {
                        // Arrows
                        line.substring(i).startsWith("-->") -> {
                            withStyle(SpanStyle(color = arrowColor)) {
                                append("-->")
                            }
                            i += 3
                        }
                        line.substring(i).startsWith("->") -> {
                            withStyle(SpanStyle(color = arrowColor)) {
                                append("->")
                            }
                            i += 2
                        }
                        line.substring(i).startsWith("->>") -> {
                            withStyle(SpanStyle(color = arrowColor)) {
                                append("->>")
                            }
                            i += 3
                        }
                        line.substring(i).startsWith("-->>") -> {
                            withStyle(SpanStyle(color = arrowColor)) {
                                append("-->>")
                            }
                            i += 4
                        }
                        
                        // Brackets and node identifiers
                        line[i] in listOf('[', ']', '(', ')', '{', '}', '<', '>') -> {
                            withStyle(SpanStyle(color = arrowColor)) {
                                append(line[i])
                            }
                            i++
                        }
                        
                        // String literals
                        line[i] == '"' -> {
                            val endQuote = line.indexOf('"', i + 1)
                            if (endQuote != -1) {
                                withStyle(SpanStyle(color = stringColor)) {
                                    append(line.substring(i, endQuote + 1))
                                }
                                i = endQuote + 1
                            } else {
                                append(line[i])
                                i++
                            }
                        }
                        
                        // Node identifiers (letters followed by brackets or arrows)
                        line[i].isLetter() -> {
                            val nodeStart = i
                            while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) {
                                i++
                            }
                            // Check if followed by bracket or arrow (likely a node)
                            if (i < line.length && line[i] in listOf('[', '(', '{', '-', ' ')) {
                                withStyle(SpanStyle(color = nodeColor)) {
                                    append(line.substring(nodeStart, i))
                                }
                            } else {
                                append(line.substring(nodeStart, i))
                            }
                        }
                        
                        else -> {
                            append(line[i])
                            i++
                        }
                    }
                }
            } else {
                // Append the rest of the line after the keyword
                append(line.substring(currentIndex))
            }
        }
    }
}

/**
 * Simple version without complex highlighting
 */
@Composable
fun SimpleHighlightedEditor(
    content: String = "",
    onContentChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    NativeTextEditor(
        content = content,
        onContentChanged = onContentChanged,
        modifier = modifier
    )
}