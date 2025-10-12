package com.example.mermaidmaker.data.repository

import com.example.mermaidmaker.data.local.dao.DiagramDao
import com.example.mermaidmaker.data.local.entity.DiagramEntity
import com.example.mermaidmaker.data.mapper.DiagramEntityDomainMapper
import com.example.mermaidmaker.domain.model.MermaidDiagram
import com.example.mermaidmaker.domain.repository.DiagramRepository
import kotlinx.coroutines.flow.Flow

class DiagramRepositoryImpl(
    private val diagramDao: DiagramDao,
    mapper: DiagramEntityDomainMapper = DiagramEntityDomainMapper()
) : BaseRepositoryImpl<DiagramEntity, MermaidDiagram>(mapper), DiagramRepository {

    override suspend fun getAllDiagrams(): Flow<List<MermaidDiagram>> {
        return mapEntitiesToDomains(diagramDao.getAllDiagrams())
    }

    override suspend fun getDiagramById(id: String): MermaidDiagram? {
        return mapEntityToDomain(diagramDao.getDiagramById(id))
    }

    override suspend fun insertDiagram(diagram: MermaidDiagram) {
        diagramDao.insertDiagram(mapDomainToEntity(diagram))
    }

    override suspend fun updateDiagram(diagram: MermaidDiagram) {
        diagramDao.updateDiagram(mapDomainToEntity(diagram))
    }

    override suspend fun deleteDiagram(id: String) {
        diagramDao.deleteDiagram(id)
    }

    override suspend fun getFavoriteDiagrams(): Flow<List<MermaidDiagram>> {
        return mapEntitiesToDomains(diagramDao.getFavoriteDiagrams())
    }

    override suspend fun toggleFavorite(id: String) {
        diagramDao.toggleFavorite(id)
    }

    override suspend fun getMostRecentDiagram(): MermaidDiagram? {
        return mapEntityToDomain(diagramDao.getMostRecentDiagram())
    }
}