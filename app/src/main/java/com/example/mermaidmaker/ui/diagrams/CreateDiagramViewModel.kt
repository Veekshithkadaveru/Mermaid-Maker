package com.example.mermaidmaker.ui.diagrams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.MermaidDiagram
import com.example.mermaidmaker.domain.usecase.CreateDiagramUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class CreateDiagramViewModel(
    private val createDiagramUseCase: CreateDiagramUseCase
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun create(
        title: String,
        content: String,
        type: DiagramType,
        onSaved: (MermaidDiagram) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                val diagram = createDiagramUseCase(title, content, type)
                onSaved(diagram)
            } catch (t: Throwable) {
                onError(t)
            } finally {
                _isSaving.value = false
            }
        }
    }
}

