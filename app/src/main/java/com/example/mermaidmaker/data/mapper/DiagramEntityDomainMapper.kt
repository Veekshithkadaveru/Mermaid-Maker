package com.example.mermaidmaker.data.mapper

import com.example.mermaidmaker.data.local.entity.DiagramEntity
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.MermaidDiagram

/**
 * Mapper implementation for converting between DiagramEntity and MermaidDiagram.
 */
class DiagramEntityDomainMapper : EntityDomainMapper<DiagramEntity, MermaidDiagram> {

    override fun entityToDomain(entity: DiagramEntity): MermaidDiagram {
        return MermaidDiagram(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            diagramType = DiagramType.valueOf(entity.diagramType),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            isFavorite = entity.isFavorite
        )
    }

    override fun domainToEntity(domain: MermaidDiagram): DiagramEntity {
        return DiagramEntity(
            id = domain.id,
            title = domain.title,
            content = domain.content,
            diagramType = domain.diagramType.name,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            isFavorite = domain.isFavorite
        )
    }
}