package com.example.mermaidmaker.domain.repository

import com.example.mermaidmaker.domain.model.MermaidDiagram
import kotlinx.coroutines.flow.Flow

interface DiagramRepository {
    suspend fun getAllDiagrams(): Flow<List<MermaidDiagram>>
    suspend fun getDiagramById(id: String): MermaidDiagram?
    suspend fun insertDiagram(diagram: MermaidDiagram)
    suspend fun updateDiagram(diagram: MermaidDiagram)
    suspend fun deleteDiagram(id: String)
    suspend fun getFavoriteDiagrams(): Flow<List<MermaidDiagram>>
    suspend fun toggleFavorite(id: String)
}