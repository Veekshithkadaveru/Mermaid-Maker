package com.example.mermaidmaker.ui.migration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Screen to show database migration progress
 */
@Composable
fun DatabaseMigrationScreen(
    state: DatabaseMigrationState,
    onMigrationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by state.uiState

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    text = "Updating Database",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Please wait while we update your database to the latest version. This will only take a moment.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress indicator
                if (uiState.isInProgress) {
                    if (uiState.progress > 0f) {
                        // Determinate progress
                        LinearProgressIndicator(
                            progress = { uiState.progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        
                        Text(
                            text = "${(uiState.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        // Indeterminate progress
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Current step
                if (uiState.currentStep.isNotBlank()) {
                    Text(
                        text = uiState.currentStep,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Error state
                uiState.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Migration Error: $error",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onMigrationComplete
                    ) {
                        Text("Continue Anyway")
                    }
                }
            }
        }
    }

    // Auto-dismiss when migration is complete and no error
    LaunchedEffect(uiState.isInProgress, uiState.error) {
        if (!uiState.isInProgress && uiState.error == null && uiState.currentStep.isNotBlank()) {
            kotlinx.coroutines.delay(1500) // Show completion for a moment
            onMigrationComplete()
        }
    }
}