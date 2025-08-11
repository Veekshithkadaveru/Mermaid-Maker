package com.example.mermaidmaker.di

import android.content.Context
import androidx.room.Room
import com.example.mermaidmaker.data.local.MermaidDatabase
import com.example.mermaidmaker.data.local.dao.DiagramDao
import com.example.mermaidmaker.data.local.dao.TemplateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MermaidDatabase {
        return Room.databaseBuilder(
            context,
            MermaidDatabase::class.java,
            MermaidDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideDiagramDao(database: MermaidDatabase): DiagramDao {
        return database.diagramDao()
    }

    @Provides
    fun provideTemplateDao(database: MermaidDatabase): TemplateDao {
        return database.templateDao()
    }
}