package com.example.mermaidmaker.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mermaidmaker.domain.model.DiagramType

@Composable
fun ExampleSelectionDialog(
    selectedDiagramType: DiagramType,
    onDiagramTypeSelected: (DiagramType) -> Unit,
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
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Diagram examples",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Diagram type selection grid
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(DiagramType.values().toList().chunked(2)) { rowTypes ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowTypes.forEach { diagramType ->
                                ExampleButton(
                                    text = getDiagramTypeDisplayName(diagramType),
                                    isSelected = selectedDiagramType == diagramType,
                                    onClick = {
                                        onDiagramTypeSelected(diagramType)
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowTypes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Templates removed from Examples section per request
            }
        }
    }
}

@Composable
fun ExampleButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

fun getDiagramTypeDisplayName(diagramType: DiagramType): String {
    return when (diagramType) {
        DiagramType.FLOWCHART -> "Flowchart"
        DiagramType.SEQUENCE -> "Sequence Diagram"
        DiagramType.CLASS -> "Class Diagram"
        DiagramType.STATE -> "State Diagram"
        DiagramType.ER_DIAGRAM -> "ER Diagram"
        DiagramType.GITGRAPH -> "Gitgraph"
        DiagramType.JOURNEY -> "Journey"
        DiagramType.GANTT -> "Gantt"
        DiagramType.PIE -> "Pie Chart"
    }
}

fun getDiagramTypeDescription(diagramType: DiagramType): String {
    return when (diagramType) {
        DiagramType.FLOWCHART -> "Process flows and decisions"
        DiagramType.SEQUENCE -> "Interactions over time"
        DiagramType.CLASS -> "Object-oriented structures"
        DiagramType.STATE -> "State transitions"
        DiagramType.ER_DIAGRAM -> "Database relationships"
        DiagramType.GITGRAPH -> "Git workflow visualization"
        DiagramType.JOURNEY -> "User experience flows"
        DiagramType.GANTT -> "Project timelines"
        DiagramType.PIE -> "Data proportions"
    }
}


