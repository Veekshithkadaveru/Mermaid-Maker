package com.example.mermaidmaker.di

import com.example.mermaidmaker.data.repository.DiagramRepositoryImpl
import com.example.mermaidmaker.data.repository.TemplateRepositoryImpl
import com.example.mermaidmaker.domain.repository.DiagramRepository
import com.example.mermaidmaker.domain.repository.TemplateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDiagramRepository(
        diagramRepositoryImpl: DiagramRepositoryImpl
    ): DiagramRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(
        templateRepositoryImpl: TemplateRepositoryImpl
    ): TemplateRepository
}