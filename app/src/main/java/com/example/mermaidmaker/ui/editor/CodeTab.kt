package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun CodeTab(
    content: String,
    fontSize: Int,
    onContentChanged: (String) -> Unit
) {
    SyntaxHighlightedEditor(
        content = content,
        fontSize = fontSize,
        onContentChanged = onContentChanged,
        modifier = Modifier.fillMaxSize()
    )
}


