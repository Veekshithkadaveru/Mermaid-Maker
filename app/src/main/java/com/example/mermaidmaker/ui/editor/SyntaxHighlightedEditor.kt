package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
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
    fontSize: Int = 14,
    onContentChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(content)) }
    val density = LocalDensity.current

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
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                val horizontalScrollState = rememberScrollState()
                val lines = remember(textFieldValue.text) {
                    if (textFieldValue.text.isEmpty()) listOf("") else textFieldValue.text.split(
                        '\n'
                    )
                }

                var clickedLineIndex by remember { mutableStateOf<Int?>(null) }

                LaunchedEffect(textFieldValue.selection) { clickedLineIndex = null }
                val cursorLineIndex = remember(textFieldValue.selection, textFieldValue.text) {
                    val cursor =
                        textFieldValue.selection.start.coerceAtMost(textFieldValue.text.length)
                    var count = 0
                    for (i in 0 until cursor) {
                        if (textFieldValue.text.getOrNull(i) == '\n') count++
                    }
                    count
                }
                val selectedLineIndex = clickedLineIndex ?: cursorLineIndex
                val highlightColor =
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                val gutterHighlightColor =
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                val lineHeightSp = (fontSize + 6).sp
                val lineHeightDp = with(density) { lineHeightSp.toDp() }
                val lineHeightPx = with(density) { lineHeightSp.toPx() }

                // Line numbers background
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                )

                Row(
                    modifier = Modifier.fillMaxSize()
                ) {

                    Column(
                        modifier = Modifier.width(48.dp)
                    ) {
                        lines.forEachIndexed { index, _ ->
                            Text(
                                text = "${index + 1}",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSize.sp,
                                    lineHeight = lineHeightSp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(lineHeightDp)
                                    .background(if (index == selectedLineIndex) gutterHighlightColor else Color.Transparent)
                                    .clickable {
                                        clickedLineIndex = index
                                    }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        // Track text layout to align highlight exactly
                        var textLayout: TextLayoutResult? by remember { mutableStateOf(null) }
                        // Determine content width from layout (widest line)
                        val contentWidthDp = remember(textLayout) {
                            val layout = textLayout
                            if (layout != null && layout.lineCount > 0) {
                                var maxRight = 0f
                                for (i in 0 until layout.lineCount) {
                                    maxRight = maxOf(maxRight, layout.getLineRight(i))
                                }
                                with(density) { (maxRight + 16f).toDp() }
                            } else 0.dp
                        }
                        // Draw highlight using actual layout line positions
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(min = contentWidthDp)
                                .drawBehind {
                                    val layout = textLayout
                                    if (layout != null) {
                                        val lineIndex = selectedLineIndex.coerceIn(
                                            0,
                                            (layout.lineCount - 1).coerceAtLeast(0)
                                        )
                                        if (layout.lineCount > 0) {
                                            val top = layout.getLineTop(lineIndex)
                                            val bottom = layout.getLineBottom(lineIndex)
                                            drawRect(
                                                color = highlightColor,
                                                topLeft = Offset(0f, top),
                                                size = Size(size.width, bottom - top)
                                            )
                                        }
                                    }
                                }
                        )
                        // Syntax highlighted overlay
                        if (textFieldValue.text.isNotEmpty()) {
                            Text(
                                text = applySyntaxHighlighting(textFieldValue.text),
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSize.sp,
                                    lineHeight = lineHeightSp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .widthIn(min = contentWidthDp)
                                    .padding(start = 8.dp),
                                onTextLayout = { result -> textLayout = result }
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
                                fontSize = fontSize.sp,
                                lineHeight = lineHeightSp,
                                color = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(min = contentWidthDp)
                                .padding(start = 8.dp),
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
                                                fontSize = fontSize.sp,
                                                lineHeight = (fontSize + 6).sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.6f
                                                )
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

                        line[i] in listOf('[', ']', '(', ')', '{', '}', '<', '>') -> {
                            withStyle(SpanStyle(color = arrowColor)) {
                                append(line[i])
                            }
                            i++
                        }

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

                        line[i].isLetter() -> {
                            val nodeStart = i
                            while (i < line.length && (line[i].isLetterOrDigit() || line[i] == '_')) {
                                i++
                            }

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

