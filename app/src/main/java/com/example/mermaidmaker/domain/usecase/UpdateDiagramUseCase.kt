package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.model.MermaidDiagram
import com.example.mermaidmaker.domain.repository.DiagramRepository
import java.time.LocalDateTime
import javax.inject.Inject

class UpdateDiagramUseCase @Inject constructor(
    private val repository: DiagramRepository
) {
    suspend operator fun invoke(diagram: MermaidDiagram): MermaidDiagram {
        val updatedDiagram = diagram.copy(updatedAt = LocalDateTime.now())
        repository.updateDiagram(updatedDiagram)
        return updatedDiagram
    }
}