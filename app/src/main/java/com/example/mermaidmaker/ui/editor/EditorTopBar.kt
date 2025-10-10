package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun EditorTopBar(
    textLength: Int,
    errorCount: Int,
    canUndo: Boolean,
    canRedo: Boolean,
    onOpen: () -> Unit,
    onSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(onClick = onOpen, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = "Open file",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onSave, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "Save file",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "$textLength chars",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                color = if (errorCount > 0)
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (errorCount > 0) "$errorCount errors" else "No errors",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = if (errorCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
                IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}


