package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottomControlsSection(
    viewModel: MermaidEditorViewModel,
    context: android.content.Context,
    editorState: MermaidEditorState,
    onFontSizeClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                viewModel.clearContent()
                editorState.clear()
            }) {
                Text("clear")
            }

            Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)

            TextButton(onClick = {
                viewModel.copyToClipboard(context)
            }) {
                Text("copy")
            }

            Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)

            TextButton(onClick = {
                val updatedContent = viewModel.pasteFromClipboard(context)
                editorState.setContent(updatedContent)
            }) {
                Text("paste")
            }

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = onFontSizeClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    "font",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


