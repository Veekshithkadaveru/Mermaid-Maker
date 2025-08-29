package com.example.mermaidmaker.ui.migration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * State management for database migration UI
 */
data class MigrationUiState(
    val isInProgress: Boolean = false,
    val currentStep: String = "",
    val progress: Float = 0f,
    val error: String? = null
)

/**
 * State holder for database migration
 */
class DatabaseMigrationState {
    private val _uiState = mutableStateOf(MigrationUiState())
    val uiState: State<MigrationUiState> = _uiState
    
    fun setInProgress(inProgress: Boolean) {
        _uiState.value = _uiState.value.copy(isInProgress = inProgress)
    }
    
    fun setCurrentStep(step: String) {
        _uiState.value = _uiState.value.copy(currentStep = step)
    }
    
    fun setProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(progress = progress)
    }
    
    fun setError(error: String?) {
        _uiState.value = _uiState.value.copy(error = error)
    }
    
    fun reset() {
        _uiState.value = MigrationUiState()
    }
}

/**
 * Remember database migration state
 */
@Composable
fun rememberDatabaseMigrationState(): DatabaseMigrationState {
    return remember { DatabaseMigrationState() }
}