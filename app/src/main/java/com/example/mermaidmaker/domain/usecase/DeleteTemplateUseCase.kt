package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.repository.TemplateRepository

class DeleteTemplateUseCase(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(templateId: String) {
        repository.deleteTemplate(templateId)
    }
}