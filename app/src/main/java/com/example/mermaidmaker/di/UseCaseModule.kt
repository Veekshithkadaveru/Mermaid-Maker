package com.example.mermaidmaker.di

import com.example.mermaidmaker.data.database.DatabaseSeeder
import com.example.mermaidmaker.data.service.FileExportServiceImpl
import com.example.mermaidmaker.data.template.TemplateLoader
import com.example.mermaidmaker.domain.service.FileExportService
import com.example.mermaidmaker.domain.usecase.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val useCaseModule = module {
    // Services
    single<FileExportService> { FileExportServiceImpl(androidContext()) }
    
    // Use Cases
    factory { CreateDiagramUseCase(get()) }
    factory { CreateTemplateUseCase(get()) }
    factory { DeleteDiagramUseCase(get()) }
    factory { DeleteTemplateUseCase(get()) }
    factory<ExportDiagramUseCase> { ExportDiagramUseCaseImpl(get(), get()) }
    factory { GetAllDiagramsUseCase(get()) }
    factory { GetAllTemplatesUseCase(get()) }
    factory { GetBuiltInTemplatesUseCase(get()) }
    factory { GetTemplatesByTypeUseCase(get()) }
    factory { InitializeTemplatesUseCase(get()) }
    factory { UpdateDiagramUseCase(get()) }
    factory { UpdateTemplateUseCase(get()) }
    
    // Infrastructure
    single { TemplateLoader(get()) }
    single { DatabaseSeeder(androidContext(), get(), get()) }
}