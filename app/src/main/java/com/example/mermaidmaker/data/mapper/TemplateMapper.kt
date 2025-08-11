package com.example.mermaidmaker.data.mapper

import com.example.mermaidmaker.data.local.entity.TemplateEntity
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template

fun TemplateEntity.toDomain(): Template {
    return Template(
        id = id,
        name = name,
        description = description,
        content = content,
        diagramType = DiagramType.valueOf(diagramType),
        isBuiltIn = isBuiltIn,
        previewImage = previewImage
    )
}

fun Template.toEntity(): TemplateEntity {
    return TemplateEntity(
        id = id,
        name = name,
        description = description,
        content = content,
        diagramType = diagramType.name,
        isBuiltIn = isBuiltIn,
        previewImage = previewImage
    )
}