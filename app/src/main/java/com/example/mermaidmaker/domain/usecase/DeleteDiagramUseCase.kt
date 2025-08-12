package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.repository.DiagramRepository
import javax.inject.Inject

class DeleteDiagramUseCase @Inject constructor(
    private val repository: DiagramRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteDiagram(id)
    }
}