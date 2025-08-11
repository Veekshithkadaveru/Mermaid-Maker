package com.example.mermaidmaker.data.repository

import com.example.mermaidmaker.data.local.dao.DiagramDao
import com.example.mermaidmaker.data.mapper.toDomain
import com.example.mermaidmaker.data.mapper.toEntity
import com.example.mermaidmaker.domain.model.MermaidDiagram
import com.example.mermaidmaker.domain.repository.DiagramRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DiagramRepositoryImpl @Inject constructor(
    private val diagramDao: DiagramDao
) : DiagramRepository {

    override suspend fun getAllDiagrams(): Flow<List<MermaidDiagram>> {
        return diagramDao.getAllDiagrams().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDiagramById(id: String): MermaidDiagram? {
        return diagramDao.getDiagramById(id)?.toDomain()
    }

    override suspend fun insertDiagram(diagram: MermaidDiagram) {
        diagramDao.insertDiagram(diagram.toEntity())
    }

    override suspend fun updateDiagram(diagram: MermaidDiagram) {
        diagramDao.updateDiagram(diagram.toEntity())
    }

    override suspend fun deleteDiagram(id: String) {
        diagramDao.deleteDiagram(id)
    }

    override suspend fun getFavoriteDiagrams(): Flow<List<MermaidDiagram>> {
        return diagramDao.getFavoriteDiagrams().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun toggleFavorite(id: String) {
        diagramDao.toggleFavorite(id)
    }
}