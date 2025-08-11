package com.example.mermaidmaker.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.mermaidmaker.data.local.converter.DateTimeConverter
import com.example.mermaidmaker.data.local.dao.DiagramDao
import com.example.mermaidmaker.data.local.dao.TemplateDao
import com.example.mermaidmaker.data.local.entity.DiagramEntity
import com.example.mermaidmaker.data.local.entity.TemplateEntity

@Database(
    entities = [DiagramEntity::class, TemplateEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverter::class)
abstract class MermaidDatabase : RoomDatabase() {
    abstract fun diagramDao(): DiagramDao
    abstract fun templateDao(): TemplateDao

    companion object {
        const val DATABASE_NAME = "mermaid_database"
    }
}