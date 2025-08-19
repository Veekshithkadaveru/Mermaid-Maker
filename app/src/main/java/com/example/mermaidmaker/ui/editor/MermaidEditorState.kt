package com.example.mermaidmaker.ui.editor

import android.webkit.WebView
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * State holder for MermaidEditor component
 */
@Stable
class MermaidEditorState {
    private var _content by mutableStateOf("")
    private var _isReady by mutableStateOf(false)
    private var _webView by mutableStateOf<WebView?>(null)
    private var _cursorPosition by mutableStateOf(CursorPosition(0, 0))
    
    val content: String get() = _content
    val isReady: Boolean get() = _isReady
    val cursorPosition: CursorPosition get() = _cursorPosition
    
    fun setContent(content: String) {
        _content = content
    }
    
    internal fun setReady(ready: Boolean) {
        _isReady = ready
    }
    
    internal fun setWebView(webView: WebView?) {
        _webView = webView
    }
    
    internal fun setCursorPosition(line: Int, ch: Int) {
        _cursorPosition = CursorPosition(line, ch)
    }
    
    /**
     * Update the editor content programmatically
     */
    fun updateContent(newContent: String) {
        _content = newContent
        _webView?.let { webView ->
            if (_isReady) {
                MermaidEditorUtils.setContent(webView, newContent)
            }
        }
    }
    
    /**
     * Insert text at the current cursor position
     */
    fun insertText(text: String) {
        _webView?.let { webView ->
            if (_isReady) {
                MermaidEditorUtils.insertText(webView, text)
            }
        }
    }
    
    /**
     * Clear the editor content
     */
    fun clear() {
        _webView?.let { webView ->
            if (_isReady) {
                MermaidEditorUtils.clear(webView)
                _content = ""
            }
        }
    }
    
    /**
     * Focus the editor
     */
    fun focus() {
        _webView?.let { webView ->
            if (_isReady) {
                MermaidEditorUtils.focus(webView)
            }
        }
    }
    
    /**
     * Insert a template, replacing current content
     */
    fun insertTemplate(templateContent: String) {
        _webView?.let { webView ->
            if (_isReady) {
                MermaidEditorUtils.insertTemplate(webView, templateContent)
                _content = templateContent
            }
        }
    }
    
    /**
     * Get the current content asynchronously
     */
    fun getCurrentContent(callback: (String) -> Unit) {
        _webView?.let { webView ->
            if (_isReady) {
                MermaidEditorUtils.getContent(webView, callback)
            } else {
                callback(_content)
            }
        } ?: callback(_content)
    }
}

/**
 * Represents cursor position in the editor
 */
@Stable
data class CursorPosition(
    val line: Int,
    val column: Int
)

/**
 * Remember a MermaidEditorState
 */
@Composable
fun rememberMermaidEditorState(
    initialContent: String = ""
): MermaidEditorState {
    return remember {
        MermaidEditorState().apply {
            setContent(initialContent)
        }
    }
}

/**
 * Enhanced MermaidEditor with state management
 */
@Composable
fun MermaidEditorWithState(
    state: MermaidEditorState,
    onContentChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    theme: String = "material-darker",
    fontSize: Int = 14,
    readOnly: Boolean = false
) {
    MermaidEditor(
        content = state.content,
        onContentChanged = { newContent ->
            state.setContent(newContent)
            onContentChanged(newContent)
        },
        onCursorPositionChanged = { line, ch ->
            state.setCursorPosition(line, ch)
        },
        modifier = modifier,
        theme = theme,
        fontSize = fontSize,
        readOnly = readOnly
    )
}