package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.model.Template
import com.example.mermaidmaker.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow

class GetAllTemplatesUseCase(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(): Flow<List<Template>> {
        return repository.getAllTemplates()
    }
}