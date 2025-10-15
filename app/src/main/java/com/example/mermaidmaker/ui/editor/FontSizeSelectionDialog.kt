package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mermaidmaker.ui.common.rememberCappedDialogWidth
import com.example.mermaidmaker.ui.common.rememberOrientationAwareMaxHeight
import com.example.mermaidmaker.ui.common.rememberOrientationAwarePadding

@Composable
fun FontSizeSelectionDialog(
    currentFontSize: Int,
    onFontSizeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = rememberCappedDialogWidth(baseWidth = 320.dp))
                .padding(4.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            val contentPadding = rememberOrientationAwarePadding(
                horizontalPortrait = 8.dp,
                horizontalLandscape = 8.dp,
                verticalPortrait = 8.dp,
                verticalLandscape = 8.dp
            )
            Column(
                modifier = Modifier.padding(contentPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set font size",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(16.dp)
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                Spacer(modifier = Modifier.height(12.dp))

                val fontSizes = listOf(12, 14, 16, 18, 21, 24, 27, 30, 36)
                val maxListHeight = rememberOrientationAwareMaxHeight()

                LazyColumn(
                    modifier = Modifier.heightIn(max = maxListHeight)
                ) {
                    items(fontSizes) { fontSize ->
                        ListItem(
                            headlineContent = { Text("$fontSize px") },
                            leadingContent = {
                                RadioButton(
                                    selected = fontSize == currentFontSize,
                                    onClick = {
                                        onFontSizeSelected(fontSize)
                                        onDismiss()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

