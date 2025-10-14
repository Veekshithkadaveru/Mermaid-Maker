package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mermaidmaker.domain.model.DiagramType

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
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
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

            var expanded by remember { mutableStateOf(false) }
            var selectedLabel by remember { mutableStateOf("examples") }

            Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Box {
                TextButton(onClick = { expanded = !expanded }) {
                    Text("examples")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DiagramType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(getDiagramTypeDisplayName(type)) },
                            onClick = {
                                selectedLabel = getDiagramTypeDisplayName(type)
                                expanded = false
                                viewModel.setDiagramType(type)
                                val template = viewModel.generateBasicTemplate()
                                viewModel.updateContent(template)
                                editorState.setContent(template)
                            }
                        )
                    }
                }
            }

            Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)

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


