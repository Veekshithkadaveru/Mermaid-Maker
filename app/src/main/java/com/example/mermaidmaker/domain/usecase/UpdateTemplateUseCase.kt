package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.model.Template
import com.example.mermaidmaker.domain.repository.TemplateRepository
import javax.inject.Inject

class UpdateTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(template: Template) {
        repository.updateTemplate(template)
    }
}