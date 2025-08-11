package com.example.mermaidmaker.data.repository

import com.example.mermaidmaker.data.local.dao.TemplateDao
import com.example.mermaidmaker.data.mapper.toDomain
import com.example.mermaidmaker.data.mapper.toEntity
import com.example.mermaidmaker.domain.model.DiagramType
import com.example.mermaidmaker.domain.model.Template
import com.example.mermaidmaker.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao
) : TemplateRepository {

    override suspend fun getAllTemplates(): Flow<List<Template>> {
        return templateDao.getAllTemplates().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTemplatesByType(type: DiagramType): Flow<List<Template>> {
        return templateDao.getTemplatesByType(type.name).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTemplateById(id: String): Template? {
        return templateDao.getTemplateById(id)?.toDomain()
    }

    override suspend fun insertTemplate(template: Template) {
        templateDao.insertTemplate(template.toEntity())
    }

    override suspend fun deleteTemplate(id: String) {
        templateDao.deleteTemplate(id)
    }
}