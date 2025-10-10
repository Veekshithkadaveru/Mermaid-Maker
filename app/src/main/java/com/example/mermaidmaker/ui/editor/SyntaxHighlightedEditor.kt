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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import android.view.KeyEvent as AndroidKeyEvent

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
            EditorTopBar(
                textLength = textFieldValue.text.length,
                errorCount = lintErrors.size,
                canUndo = canUndo(),
                canRedo = canRedo(),
                onOpen = { openTxtLauncher.launch(arrayOf("text/plain")) },
                onSave = {
                    val suggested = generateAutoFilename()
                    saveTxtLauncher.launch(suggested)
                },
                onUndo = { undo() },
                onRedo = { redo() }
            )
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
                var editorWidthPx by remember { mutableStateOf(0) }
                val verticalScrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                        .drawBehind {
                            val lineTop = selectedLineIndex * lineHeightPx
                            val lineHeight = lineHeightPx
                            drawRect(
                                color = highlightColor,
                                topLeft = Offset(0f, lineTop),
                                size = Size(size.width, lineHeight)
                            )
                        }
                        .onSizeChanged {
                            editorHeightPx = it.height
                            editorWidthPx = it.width
                        }
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
                                    .background(Color.Transparent)
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
                                // Text width in dp plus left+right padding (16.dp) and extra trailing space (16.dp)
                                val textWidthDp = with(density) { maxRight.toDp() }
                                textWidthDp + 32.dp
                            } else 0.dp
                        }
                        val widthMod =
                            Modifier
                                .fillMaxWidth()
                                .then(Modifier.widthIn(min = contentWidthDp))

                        val contentHeightDp = lineHeightDp * lines.size

                        Box(modifier = widthMod.height(contentHeightDp)) {

                            
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
                                        .padding(horizontal = 8.dp),
                                    softWrap = false,
                                    onTextLayout = { result -> textLayout = result }
                                )
                            }

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
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(horizontal = 8.dp),
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
                                                ,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )

                            LaunchedEffect(textLayout, textFieldValue.selection, editorWidthPx) {
                                val layout = textLayout ?: return@LaunchedEffect
                                if (editorWidthPx <= 0) return@LaunchedEffect
                                val caretOffset = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
                                val caretX = layout.getCursorRect(caretOffset).left
                                val leftPad = with(density) { 8.dp.toPx() }
                                val rightPad = with(density) { 8.dp.toPx() }
                                val caretWithPadding = caretX + leftPad
                                val viewportStart = horizontalScrollState.value.toFloat()
                                val gutterPx = with(density) { 48.dp.toPx() }
                                val viewportEnd = viewportStart + (editorWidthPx - gutterPx)
                                val desiredRight = viewportEnd - rightPad - 8f
                                val desiredLeft = viewportStart + leftPad + 8f
                                when {
                                    caretWithPadding > desiredRight -> {
                                        val target = (caretWithPadding - editorWidthPx + rightPad + 16f).roundToInt()
                                        horizontalScrollState.scrollTo(target.coerceIn(0, horizontalScrollState.maxValue))
                                    }
                                    caretWithPadding < desiredLeft -> {
                                        val target = (caretWithPadding - leftPad - 16f).roundToInt().coerceAtLeast(0)
                                        horizontalScrollState.scrollTo(target)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
