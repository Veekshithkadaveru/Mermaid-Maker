package com.example.mermaidmaker.domain.model

import java.time.LocalDateTime

data class MermaidDiagram(
    val id: String,
    val title: String,
    val content: String,
    val diagramType: DiagramType,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isFavorite: Boolean = false
)

enum class DiagramType {
    FLOWCHART,
    SEQUENCE,
    CLASS,
    STATE,
    ER,
    GANTT,
    PIE,
    JOURNEY
}