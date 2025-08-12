package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template
import com.example.mermaidmaker.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetBuiltInTemplatesUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    suspend operator fun invoke(): Flow<List<Template>> {
        return templateRepository.getAllTemplates().map { templates ->
            templates.filter { it.isBuiltIn }
        }
    }
    
    suspend fun getByType(diagramType: DiagramType): Flow<List<Template>> {
        return templateRepository.getTemplatesByType(diagramType).map { templates ->
            templates.filter { it.isBuiltIn }
        }
    }
    
    suspend fun getUserTemplates(): Flow<List<Template>> {
        return templateRepository.getAllTemplates().map { templates ->
            templates.filter { !it.isBuiltIn }
        }
    }
    
    suspend fun getUserTemplatesByType(diagramType: DiagramType): Flow<List<Template>> {
        return templateRepository.getTemplatesByType(diagramType).map { templates ->
            templates.filter { !it.isBuiltIn }
        }
    }
}