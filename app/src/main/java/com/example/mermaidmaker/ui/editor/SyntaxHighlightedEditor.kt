package com.example.mermaidmaker.ui.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import android.view.KeyEvent as AndroidKeyEvent


private data class EditorLintError(val line: Int, val message: String)

/**
 * Enhanced native editor with basic syntax highlighting
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyntaxHighlightedEditor(
    content: String = "",
    fontSize: Int = 14,
    onContentChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    onShowSnackbar: (String) -> Unit = {}
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(content)) }

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


    var lintErrors by remember { mutableStateOf<List<EditorLintError>>(emptyList()) }
    LaunchedEffect(textFieldValue.text) {

        delay(300)
        lintErrors = analyzeMermaidFast(textFieldValue.text)
    }

    // File open/save launchers
    val context = LocalContext.current
    val openTxtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader(Charsets.UTF_8).readText()
                }
            }.onSuccess { text ->
                if (text != null) {
                    val newValue = textFieldValue.copy(text = text)
                    textFieldValue = newValue
                    onContentChanged(text)
                    pushHistory(newValue, forceNewEntry = true)
                }
            }
        }
    }
    val saveTxtLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            val contentToSave = textFieldValue.text
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.writer(Charsets.UTF_8).use { writer ->
                        writer.write(contentToSave)
                    }
                }
            }.onSuccess {
                onShowSnackbar("Saved")
            }.onFailure { _ ->
                onShowSnackbar("Failed to save")
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // File operations group
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { openTxtLauncher.launch(arrayOf("text/plain")) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FolderOpen, 
                                contentDescription = "Open file",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                val suggested = generateAutoFilename()
                                saveTxtLauncher.launch(suggested)
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Save, 
                                contentDescription = "Save file",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Middle section with status info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Character count
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${textFieldValue.text.length} chars",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    // Error/validation status
                    Surface(
                        color = if (lintErrors.isNotEmpty()) 
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        else 
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (lintErrors.isNotEmpty()) "${lintErrors.size} errors" else "No errors",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (lintErrors.isNotEmpty()) 
                                MaterialTheme.colorScheme.error 
                            else 
                                MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Edit operations group
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { undo() }, 
                            enabled = canUndo(),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo, 
                                contentDescription = "Undo",
                                tint = if (canUndo()) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                        IconButton(
                            onClick = { redo() }, 
                            enabled = canRedo(),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo, 
                                contentDescription = "Redo",
                                tint = if (canRedo()) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }
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

                    Column(
                        modifier = Modifier.width(48.dp)
                    ) {

                        val visibleLines = remember(editorHeightPx, lineHeightPx) {
                            if (lineHeightPx > 0f) kotlin.math.ceil(editorHeightPx / lineHeightPx)
                                .toInt().coerceAtLeast(1) else lines.size
                        }
                        val totalLinesToShow = maxOf(lines.size, visibleLines)
                        val errorLines = remember(lintErrors) { lintErrors.map { it.line }.toSet() }
                        repeat(totalLinesToShow) { index ->
                            Text(
                                text = if (errorLines.contains(index)) "!" else "${index + 1}",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = fontSize.sp,
                                    lineHeight = lineHeightSp,
                                    color = if (errorLines.contains(index)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.6f
                                    ),
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(lineHeightDp)
                                    .background(if (index == selectedLineIndex) gutterHighlightColor else Color.Transparent)
                                    .let { base ->
                                        if (index < lines.size) base.clickable {
                                            clickedLineIndex = index
                                        } else base
                                    }
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
                                    val target =
                                        (horizontalScrollState.value - dragAmount).roundToInt()
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
                        val widthMod =
                            if (contentWidthDp > 0.dp) Modifier.width(contentWidthDp) else Modifier.fillMaxWidth()

                        val contentHeightDp = lineHeightDp * lines.size

                        Box(modifier = widthMod.height(contentHeightDp)) {

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
}
private fun generateAutoFilename(): String {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.getDefault())
    val timestamp = formatter.format(java.util.Date())
    return "mermaid_$timestamp.txt"
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

/**
 * Very fast Mermaid lint: mode keyword presence and basic bracket balance per line
 */
private fun analyzeMermaidFast(text: String): List<EditorLintError> {
    val errors = mutableListOf<EditorLintError>()
    if (text.isBlank()) return emptyList()

    val lines = text.split('\n')

    val keywords = setOf(
        "graph", "flowchart", "sequenceDiagram", "classDiagram",
        "stateDiagram", "stateDiagram-v2", "erDiagram", "journey", "gantt", "pie", "gitgraph"
    )
    val firstNonEmptyIndex = lines.indexOfFirst { it.isNotBlank() }
    if (firstNonEmptyIndex >= 0) {
        val first = lines[firstNonEmptyIndex].trimStart()
        if (keywords.none { first.startsWith(it) }) {
            errors.add(
                EditorLintError(
                    firstNonEmptyIndex,
                    "Missing diagram type (e.g., graph TD, sequenceDiagram)"
                )
            )
        }
    }

    val pairs = mapOf('(' to ')', '[' to ']', '{' to '}')
    lines.forEachIndexed { idx, line ->
        val stack = ArrayDeque<Char>()
        line.forEach { ch ->
            if (ch in pairs.keys) stack.addLast(ch)
            else if (ch in pairs.values) {
                if (stack.isEmpty() || pairs[stack.removeLast()] != ch) {
                    errors.add(EditorLintError(idx, "Unbalanced brackets"))
                    return@forEachIndexed
                }
            }
        }
        if (stack.isNotEmpty()) {
            errors.add(EditorLintError(idx, "Unclosed bracket"))
        }
    }

    return errors
}

