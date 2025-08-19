package com.example.mermaidmaker.di

import com.example.mermaidmaker.data.repository.DiagramRepositoryImpl
import com.example.mermaidmaker.data.repository.TemplateRepositoryImpl
import com.example.mermaidmaker.domain.repository.DiagramRepository
import com.example.mermaidmaker.domain.repository.TemplateRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<DiagramRepository> { DiagramRepositoryImpl(get()) }
    single<TemplateRepository> { TemplateRepositoryImpl(get()) }
}