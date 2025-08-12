package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template
import com.example.mermaidmaker.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTemplatesByTypeUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(diagramType: DiagramType): Flow<List<Template>> {
        return repository.getTemplatesByType(diagramType)
    }
}