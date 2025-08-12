package com.example.mermaidmaker.domain.repository

import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template
import kotlinx.coroutines.flow.Flow

interface TemplateRepository {
    suspend fun getAllTemplates(): Flow<List<Template>>
    suspend fun getTemplatesByType(type: DiagramType): Flow<List<Template>>
    suspend fun getTemplateById(id: String): Template?
    suspend fun insertTemplate(template: Template)
    suspend fun updateTemplate(template: Template)
    suspend fun deleteTemplate(id: String)
}