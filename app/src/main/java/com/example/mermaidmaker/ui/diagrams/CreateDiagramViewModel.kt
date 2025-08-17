package com.example.mermaidmaker.ui.diagrams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.MermaidDiagram
// import com.example.mermaidmaker.domain.usecase.CreateDiagramUseCase
// import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
// import javax.inject.Inject

// @HiltViewModel
class CreateDiagramViewModel /*@Inject constructor(
    private val createDiagramUseCase: CreateDiagramUseCase
)*/ : ViewModel() {

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
                // Mock save operation
                kotlinx.coroutines.delay(1000)
                val saved = MermaidDiagram(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    content = content,
                    diagramType = type,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                onSaved(saved)
            } catch (t: Throwable) {
                onError(t)
            } finally {
                _isSaving.value = false
            }
        }
    }
}

