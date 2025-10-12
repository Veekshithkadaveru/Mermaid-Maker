package com.example.mermaidmaker.data.repository

import com.example.mermaidmaker.data.local.dao.TemplateDao
import com.example.mermaidmaker.data.local.entity.TemplateEntity
import com.example.mermaidmaker.data.mapper.TemplateEntityDomainMapper
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template
import com.example.mermaidmaker.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow

class TemplateRepositoryImpl(
    private val templateDao: TemplateDao,
    mapper: TemplateEntityDomainMapper = TemplateEntityDomainMapper()
) : BaseRepositoryImpl<TemplateEntity, Template>(mapper), TemplateRepository {

    override suspend fun getAllTemplates(): Flow<List<Template>> {
        return mapEntitiesToDomains(templateDao.getAllTemplates())
    }

    override suspend fun getTemplatesByType(type: DiagramType): Flow<List<Template>> {
        return mapEntitiesToDomains(templateDao.getTemplatesByType(type.name))
    }

    override suspend fun getTemplateById(id: String): Template? {
        return mapEntityToDomain(templateDao.getTemplateById(id))
    }

    override suspend fun insertTemplate(template: Template) {
        templateDao.insertTemplate(mapDomainToEntity(template))
    }

    override suspend fun updateTemplate(template: Template) {
        templateDao.updateTemplate(mapDomainToEntity(template))
    }

    override suspend fun deleteTemplate(id: String) {
        templateDao.deleteTemplate(id)
    }
}