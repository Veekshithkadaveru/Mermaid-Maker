package com.example.mermaidmaker.data.mapper

import com.example.mermaidmaker.data.local.entity.TemplateEntity
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template

/**
 * Mapper implementation for converting between TemplateEntity and Template.
 */
class TemplateEntityDomainMapper : EntityDomainMapper<TemplateEntity, Template> {

    override fun entityToDomain(entity: TemplateEntity): Template {
        return Template(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            content = entity.content,
            diagramType = DiagramType.valueOf(entity.diagramType),
            isBuiltIn = entity.isBuiltIn,
            previewImage = entity.previewImage
        )
    }

    override fun domainToEntity(domain: Template): TemplateEntity {
        return TemplateEntity(
            id = domain.id,
            name = domain.name,
            description = domain.description,
            content = domain.content,
            diagramType = domain.diagramType.name,
            isBuiltIn = domain.isBuiltIn,
            previewImage = domain.previewImage
        )
    }
}