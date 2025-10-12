package com.example.mermaidmaker.ui.common

import androidx.compose.material3.SnackbarHostState

suspend fun SnackbarHostState.showMessage(message: String, actionLabel: String? = null) {
    this.showSnackbar(message = message, actionLabel = actionLabel)
}



