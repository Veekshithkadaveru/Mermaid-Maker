package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.model.MermaidDiagram
import com.example.mermaidmaker.domain.repository.DiagramRepository
import kotlinx.coroutines.flow.Flow

class GetAllDiagramsUseCase(
    private val repository: DiagramRepository
) {
    suspend operator fun invoke(): Flow<List<MermaidDiagram>> {
        return repository.getAllDiagrams()
    }
}