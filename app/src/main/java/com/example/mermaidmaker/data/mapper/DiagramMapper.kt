package com.example.mermaidmaker.data.mapper

import com.example.mermaidmaker.data.local.entity.DiagramEntity
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.MermaidDiagram

fun DiagramEntity.toDomain(): MermaidDiagram {
    return MermaidDiagram(
        id = id,
        title = title,
        content = content,
        diagramType = DiagramType.valueOf(diagramType),
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite
    )
}

fun MermaidDiagram.toEntity(): DiagramEntity {
    return DiagramEntity(
        id = id,
        title = title,
        content = content,
        diagramType = diagramType.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorite = isFavorite
    )
}