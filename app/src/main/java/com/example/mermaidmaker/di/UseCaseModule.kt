package com.example.mermaidmaker.di

import com.example.mermaidmaker.data.database.DatabaseSeeder
import com.example.mermaidmaker.data.service.AiDiagramServiceImpl
import com.example.mermaidmaker.data.service.FileExportServiceImpl
import com.example.mermaidmaker.data.template.TemplateLoader
import com.example.mermaidmaker.util.WebViewPngGenerator
import com.example.mermaidmaker.domain.service.AiDiagramService
import com.example.mermaidmaker.domain.service.FileExportService
import com.example.mermaidmaker.domain.usecase.CreateDiagramUseCase
import com.example.mermaidmaker.domain.usecase.CreateTemplateUseCase
import com.example.mermaidmaker.domain.usecase.DeleteDiagramUseCase
import com.example.mermaidmaker.domain.usecase.DeleteTemplateUseCase
import com.example.mermaidmaker.domain.usecase.ExportDiagramUseCase
import com.example.mermaidmaker.domain.usecase.ExportDiagramUseCaseImpl
import com.example.mermaidmaker.domain.usecase.ExportPngUseCase
import com.example.mermaidmaker.domain.usecase.GenerateAiDiagramUseCase
import com.example.mermaidmaker.domain.usecase.GenerateFromCodeUseCase
import com.example.mermaidmaker.domain.usecase.FixMermaidCodeUseCase
import com.example.mermaidmaker.domain.usecase.GetAllDiagramsUseCase
import com.example.mermaidmaker.domain.usecase.GetAllTemplatesUseCase
import com.example.mermaidmaker.domain.usecase.GetBuiltInTemplatesUseCase
import com.example.mermaidmaker.domain.usecase.GetTemplatesByTypeUseCase
import com.example.mermaidmaker.domain.usecase.InitializeTemplatesUseCase
import com.example.mermaidmaker.domain.usecase.UpdateDiagramUseCase
import com.example.mermaidmaker.domain.usecase.UpdateTemplateUseCase
import com.example.mermaidmaker.domain.usecase.ExplainDiagramUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val useCaseModule = module {
    // Services
    single<FileExportService> { FileExportServiceImpl(androidContext()) }
    single<AiDiagramService> { AiDiagramServiceImpl(get(), get()) }

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
    factory { GenerateAiDiagramUseCase(get(), get()) }
    factory { GenerateFromCodeUseCase(get(), get()) }
    factory { FixMermaidCodeUseCase(get(), get()) }
    factory { ExportPngUseCase(get(), get()) }
    factory { ExplainDiagramUseCase(get(), get()) }

    // Infrastructure
    single { TemplateLoader(get()) }
    single { DatabaseSeeder(androidContext(), get(), get()) }
    single { WebViewPngGenerator() }
}