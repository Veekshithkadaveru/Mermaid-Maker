package com.example.mermaidmaker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.MermaidDiagram
import com.example.mermaidmaker.domain.usecase.GetAllDiagramsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class HomeViewModel(
    private val getAllDiagramsUseCase: GetAllDiagramsUseCase
) : ViewModel() {

    private val _diagrams = MutableStateFlow<List<MermaidDiagram>>(emptyList())
    val diagrams: StateFlow<List<MermaidDiagram>> = _diagrams.asStateFlow()

    init {
        loadDiagrams()
    }

    private fun loadDiagrams() {
        viewModelScope.launch {
            getAllDiagramsUseCase().collectLatest { diagrams ->
                _diagrams.value = diagrams
            }
        }
    }
}

