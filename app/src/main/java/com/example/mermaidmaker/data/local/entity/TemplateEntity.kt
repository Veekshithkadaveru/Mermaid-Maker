package com.example.mermaidmaker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val content: String,
    val diagramType: String,
    val isBuiltIn: Boolean = true,
    val previewImage: String? = null
)