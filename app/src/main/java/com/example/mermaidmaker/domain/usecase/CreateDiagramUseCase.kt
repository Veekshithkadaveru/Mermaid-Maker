package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.MermaidDiagram
import com.example.mermaidmaker.domain.repository.DiagramRepository
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class CreateDiagramUseCase @Inject constructor(
    private val repository: DiagramRepository
) {
    suspend operator fun invoke(
        title: String,
        content: String,
        diagramType: DiagramType
    ): MermaidDiagram {
        val diagram = MermaidDiagram(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            diagramType = diagramType,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            isFavorite = false
        )
        repository.insertDiagram(diagram)
        return diagram
    }
}