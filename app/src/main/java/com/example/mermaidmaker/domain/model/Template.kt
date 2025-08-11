package com.example.mermaidmaker.domain.model

data class Template(
    val id: String,
    val name: String,
    val description: String,
    val content: String,
    val diagramType: DiagramType,
    val isBuiltIn: Boolean = true,
    val previewImage: String? = null
)