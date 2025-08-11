package com.example.mermaidmaker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "diagrams")
data class DiagramEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val diagramType: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isFavorite: Boolean = false
)