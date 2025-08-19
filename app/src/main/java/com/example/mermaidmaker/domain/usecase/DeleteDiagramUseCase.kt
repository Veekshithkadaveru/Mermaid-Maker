package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.repository.DiagramRepository

class DeleteDiagramUseCase(
    private val repository: DiagramRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteDiagram(id)
    }
}