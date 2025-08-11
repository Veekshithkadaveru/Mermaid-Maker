package com.example.mermaidmaker.data.local.dao

import androidx.room.*
import com.example.mermaidmaker.data.local.entity.DiagramEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagramDao {
    @Query("SELECT * FROM diagrams ORDER BY updatedAt DESC")
    fun getAllDiagrams(): Flow<List<DiagramEntity>>

    @Query("SELECT * FROM diagrams WHERE id = :id")
    suspend fun getDiagramById(id: String): DiagramEntity?

    @Query("SELECT * FROM diagrams WHERE isFavorite = 1 ORDER BY updatedAt DESC")
    fun getFavoriteDiagrams(): Flow<List<DiagramEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagram(diagram: DiagramEntity)

    @Update
    suspend fun updateDiagram(diagram: DiagramEntity)

    @Query("DELETE FROM diagrams WHERE id = :id")
    suspend fun deleteDiagram(id: String)

    @Query("UPDATE diagrams SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)
}