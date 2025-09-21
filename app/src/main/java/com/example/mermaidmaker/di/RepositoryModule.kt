package com.example.mermaidmaker.di

import com.example.mermaidmaker.data.local.prefs.EditorPreferences
import com.example.mermaidmaker.data.repository.ApiKeyRepositoryImpl
import com.example.mermaidmaker.data.repository.DiagramRepositoryImpl
import com.example.mermaidmaker.data.repository.TemplateRepositoryImpl
import com.example.mermaidmaker.data.service.ApiKeyValidationServiceImpl
import com.example.mermaidmaker.domain.repository.ApiKeyRepository
import com.example.mermaidmaker.domain.repository.DiagramRepository
import com.example.mermaidmaker.domain.repository.TemplateRepository
import com.example.mermaidmaker.domain.service.ApiKeyValidationService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single<DiagramRepository> { DiagramRepositoryImpl(get()) }
    single<TemplateRepository> { TemplateRepositoryImpl(get()) }
    single<ApiKeyRepository> { ApiKeyRepositoryImpl(get()) }
    single { EditorPreferences(androidContext()) }
    single<ApiKeyValidationService> { ApiKeyValidationServiceImpl(get(), get()) }
}