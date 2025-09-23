package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import android.view.KeyEvent as AndroidKeyEvent
import com.example.mermaidmaker.ui.editor.validation.*

/**
 * Professional native editor with advanced syntax highlighting and validation
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfessionalSyntaxEditor(
    content: String = "",
    fontSize: Int = 14,
    onContentChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(content)) }
    val validator = remember { MermaidSyntaxValidator() }
    var validationResult by remember { mutableStateOf(ValidationResult()) }
    var showErrorPanel by remember { mutableStateOf(false) }
    var hoveredError by remember { mutableStateOf<MermaidValidationError?>(null) }
    var tooltipPosition by remember { mutableStateOf(Offset.Zero) }
    var clickedLineIndex by remember { mutableStateOf<Int?>(null) }

    data class HistoryEntry(val value: TextFieldValue, val timestampMs: Long)

    val history = remember { mutableStateListOf<HistoryEntry>() }
    var historyIndex by remember { mutableStateOf(-1) }
    var lastCommitTimeMs by remember { mutableStateOf(0L) }

    fun canUndo(): Boolean = historyIndex > 0
    fun canRedo(): Boolean = historyIndex in 0 until (history.size - 1)

    fun pushHistory(newValue: TextFieldValue, forceNewEntry: Boolean = false) {
        val now = System.currentTimeMillis()

        if (historyIndex < history.lastIndex) {
            while (history.size - 1 > historyIndex) history.removeAt(history.lastIndex)
        }
        val coalesceWindowMs = 300
        val shouldCoalesce =
            !forceNewEntry && historyIndex >= 0 && (now - lastCommitTimeMs) < coalesceWindowMs
        if (shouldCoalesce) {
            history[historyIndex] = HistoryEntry(newValue, now)
        } else {
            history.add(HistoryEntry(newValue, now))
            historyIndex = history.lastIndex

            val maxEntries = 100
            if (history.size > maxEntries) {
                val removeCount = history.size - maxEntries
                repeat(removeCount) { history.removeAt(0) }
                historyIndex = history.lastIndex
            }
        }
        lastCommitTimeMs = now
    }

    fun undo() {
        if (!canUndo()) return
        historyIndex -= 1
        val entry = history[historyIndex]
        textFieldValue = entry.value
        onContentChanged(entry.value.text)
    }

    fun redo() {
        if (!canRedo()) return
        historyIndex += 1
        val entry = history[historyIndex]
        textFieldValue = entry.value
        onContentChanged(entry.value.text)
    }

    val density = LocalDensity.current

    LaunchedEffect(content) {
        if (content != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(text = content)
            if (history.isEmpty()) {
                pushHistory(textFieldValue, forceNewEntry = true)
            }
        }
    }

    LaunchedEffect(textFieldValue.text) {
        delay(300)
        validationResult = validator.validate(textFieldValue.text)
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Enhanced header with validation status
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Professional Editor",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = when {
                            validationResult.errorCount > 0 -> Icons.Default.Error
                            validationResult.warningCount > 0 -> Icons.Default.Warning
                            else -> Icons.Default.CheckCircle
                        },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = when {
                            validationResult.errorCount > 0 -> MaterialTheme.colorScheme.error
                            validationResult.warningCount > 0 -> Color(0xFFF57C00)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = validationResult.getSummaryText(),
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            validationResult.errorCount > 0 -> MaterialTheme.colorScheme.error
                            validationResult.warningCount > 0 -> Color(0xFFF57C00)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.clickable {
                            if (validationResult.totalCount > 0) {
                                showErrorPanel = !showErrorPanel
                            }
                        }
                    )
                    Text(
                        text = "Undo",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canUndo()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .let { base -> if (canUndo()) base.clickable { undo() } else base }
                    )
                    Text(
                        text = "Redo",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canRedo()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .let { base -> if (canRedo()) base.clickable { redo() } else base }
                    )
                    Text(
                        text = "${textFieldValue.text.length} characters",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val customTextSelectionColors = TextSelectionColors(
            handleColor = MaterialTheme.colorScheme.primary,
            backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )

        CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .onPreviewKeyEvent { event: KeyEvent ->
                        val native = event.nativeKeyEvent
                        if (native.action == AndroidKeyEvent.ACTION_DOWN) {
                            val isCmdOrCtrl = native.isCtrlPressed || native.isMetaPressed
                            when (native.keyCode) {
                                AndroidKeyEvent.KEYCODE_Z -> {
                                    if (isCmdOrCtrl) {
                                        if (native.isShiftPressed) redo() else undo()
                                        return@onPreviewKeyEvent true
                                    }
                                }
                                AndroidKeyEvent.KEYCODE_Y -> {
                                    if (isCmdOrCtrl) {
                                        redo()
                                        return@onPreviewKeyEvent true
                                    }
                                }
                            }
                        }
                        false
                    }
            ) {
                val horizontalScrollState = rememberScrollState()
                val lines = remember(textFieldValue.text) {
                    if (textFieldValue.text.isEmpty()) listOf("") else textFieldValue.text.split('\n')
                }

                LaunchedEffect(textFieldValue.selection) { clickedLineIndex = null }
                val cursorLineIndex = remember(textFieldValue.selection, textFieldValue.text) {
                    val cursor = textFieldValue.selection.start.coerceAtMost(textFieldValue.text.length)
                    var count = 0
                    for (i in 0 until cursor) {
                        if (textFieldValue.text.getOrNull(i) == '\n') count++
                    }
                    count
                }
                val selectedLineIndex = clickedLineIndex ?: cursorLineIndex
                val highlightColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                val gutterHighlightColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                val lineHeightSp = (fontSize + 6).sp
                val lineHeightDp = with(density) { lineHeightSp.toDp() }
                val lineHeightPx = with(density) { lineHeightSp.toPx() }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(48.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                )

                var editorHeightPx by remember { mutableStateOf(0) }
                val verticalScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                        .onSizeChanged { editorHeightPx = it.height }
                ) {
                    // Enhanced line number gutter with error indicators
                    Column(
                        modifier = Modifier.width(48.dp)
                    ) {
                        val visibleLines = remember(editorHeightPx, lineHeightPx) {
                            if (lineHeightPx > 0f) kotlin.math.ceil(editorHeightPx / lineHeightPx)
                                .toInt().coerceAtLeast(1) else lines.size
                        }
                        val totalLinesToShow = maxOf(lines.size, visibleLines)
                        val lineErrors = remember(validationResult) {
                            validationResult.errors.groupBy { it.line }
                        }
                        
                        repeat(totalLinesToShow) { index ->
                            val lineError = lineErrors[index]?.maxByOrNull { it.severity.priority }
                            
                            Text(
                                text = when {
                                    lineError?.severity == ValidationSeverity.ERROR -> "●"
                                    lineError?.severity == ValidationSeverity.WARNING -> "▲"
                                    lineError?.severity == ValidationSeverity.INFO -> "ⓘ"
                                    else -> "${index + 1}"
                                },
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSize.sp,
                                    lineHeight = lineHeightSp,
                                    color = when {
                                        lineError?.severity == ValidationSeverity.ERROR -> MaterialTheme.colorScheme.error
                                        lineError?.severity == ValidationSeverity.WARNING -> Color(0xFFF57C00)
                                        lineError?.severity == ValidationSeverity.INFO -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    },
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(lineHeightDp)
                                    .background(if (index == selectedLineIndex) gutterHighlightColor else Color.Transparent)
                                    .combinedClickable(
                                        onClick = {
                                            if (index < lines.size) {
                                                clickedLineIndex = index
                                            }
                                        },
                                        onLongClick = {
                                            lineError?.let {
                                                hoveredError = it
                                                tooltipPosition = Offset(48f, index * lineHeightPx)
                                            }
                                        }
                                    )
                            )
                        }
                    }

                    val scope = rememberCoroutineScope()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { delta: Float ->
                                    val target = (horizontalScrollState.value - delta).roundToInt()
                                        .coerceIn(0, horizontalScrollState.maxValue)
                                    scope.launch { horizontalScrollState.scrollTo(target) }
                                }
                            )
                            .scrollable(
                                orientation = Orientation.Horizontal,
                                state = rememberScrollableState { delta: Float ->
                                    val target = (horizontalScrollState.value - delta).roundToInt()
                                        .coerceIn(0, horizontalScrollState.maxValue)
                                    scope.launch { horizontalScrollState.scrollTo(target) }
                                    delta
                                }
                            )
                            .pointerInput(horizontalScrollState) {
                                detectHorizontalDragGestures(onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    val target = (horizontalScrollState.value - dragAmount).roundToInt()
                                        .coerceIn(0, horizontalScrollState.maxValue)
                                    scope.launch { horizontalScrollState.scrollTo(target) }
                                })
                            }
                    ) {
                        var textLayout: TextLayoutResult? by remember { mutableStateOf(null) }

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
                        val widthMod = if (contentWidthDp > 0.dp) Modifier.width(contentWidthDp) else Modifier.fillMaxWidth()
                        val contentHeightDp = lineHeightDp * lines.size

                        Box(modifier = widthMod.height(contentHeightDp)) {
                            // Line highlighting
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
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
                                        .matchParentSize()
                                        .padding(start = 8.dp),
                                    softWrap = false,
                                    onTextLayout = { result -> textLayout = result }
                                )
                            }

                            // Actual input field (transparent)
                            BasicTextField(
                                value = textFieldValue,
                                onValueChange = { newValue ->
                                    textFieldValue = newValue
                                    onContentChanged(newValue.text)
                                    pushHistory(newValue)
                                },
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSize.sp,
                                    lineHeight = lineHeightSp,
                                    color = Color.Transparent
                                ),
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(start = 8.dp),
                                decorationBox = { innerTextField ->
                                    Box(modifier = Modifier.matchParentSize()) {
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
        }
        
        // Error Panel
        if (validationResult.totalCount > 0) {
            ErrorPanel(
                validationResult = validationResult,
                isExpanded = showErrorPanel,
                onToggleExpanded = { showErrorPanel = !showErrorPanel },
                onErrorClick = { error ->
                    clickedLineIndex = error.line
                },
                onQuickFix = { error ->
                    error.quickFix?.let { fix ->
                        val lines = textFieldValue.text.split('\n').toMutableList()
                        if (error.line < lines.size) {
                            lines[error.line] = fix
                            val newContent = lines.joinToString("\n")
                            textFieldValue = textFieldValue.copy(text = newContent)
                            onContentChanged(newContent)
                            pushHistory(textFieldValue, forceNewEntry = true)
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // Tooltip for hovered errors
        hoveredError?.let { error ->
            Popup(
                offset = IntOffset(tooltipPosition.x.toInt(), tooltipPosition.y.toInt()),
                onDismissRequest = { hoveredError = null },
                properties = PopupProperties(focusable = false)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = error.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = error.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.inverseOnSurface
                        )
                        error.suggestion?.let { suggestion ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 $suggestion",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Apply enhanced syntax highlighting to Mermaid code
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
                append(line.substring(currentIndex))
            }
        }
    }
}