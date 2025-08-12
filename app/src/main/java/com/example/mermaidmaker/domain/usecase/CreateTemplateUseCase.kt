package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template
import com.example.mermaidmaker.domain.repository.TemplateRepository
import java.util.UUID
import javax.inject.Inject

class CreateTemplateUseCase @Inject constructor(
    private val repository: TemplateRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        content: String,
        diagramType: DiagramType,
        isBuiltIn: Boolean = false,
        previewImage: String? = null
    ): Template {
        val template = Template(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            content = content,
            diagramType = diagramType,
            isBuiltIn = isBuiltIn,
            previewImage = previewImage
        )
        repository.insertTemplate(template)
        return template
    }
}