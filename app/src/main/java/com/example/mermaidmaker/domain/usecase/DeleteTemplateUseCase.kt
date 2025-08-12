package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.repository.TemplateRepository
import javax.inject.Inject

class DeleteTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(templateId: String) {
        repository.deleteTemplate(templateId)
    }
}