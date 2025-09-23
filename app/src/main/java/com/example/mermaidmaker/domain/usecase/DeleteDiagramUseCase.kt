package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.data.service.ThumbnailGenerator
import com.example.mermaidmaker.domain.repository.DiagramRepository

class DeleteDiagramUseCase(
    private val repository: DiagramRepository,
    private val thumbnailGenerator: ThumbnailGenerator
) {
    suspend operator fun invoke(id: String) {
        // Get the diagram to retrieve thumbnail path before deleting
        val diagram = repository.getDiagramById(id)
        
        // Delete the diagram from repository
        repository.deleteDiagram(id)
        
        // Clean up thumbnail file if it exists
        diagram?.thumbnailPath?.let { thumbnailPath ->
            thumbnailGenerator.deleteThumbnail(thumbnailPath)
        }
    }
}