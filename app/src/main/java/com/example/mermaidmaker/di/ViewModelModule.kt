package com.example.mermaidmaker.di

import com.example.mermaidmaker.ui.diagrams.CreateDiagramViewModel
import com.example.mermaidmaker.ui.editor.MermaidEditorViewModel
import com.example.mermaidmaker.ui.home.HomeViewModel
import com.example.mermaidmaker.ui.settings.ApiKeyViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
    viewModel { CreateDiagramViewModel(get()) }
    viewModel {
        val builtIn: com.example.mermaidmaker.domain.usecase.GetBuiltInTemplatesUseCase = get()
        val byType: com.example.mermaidmaker.domain.usecase.GetTemplatesByTypeUseCase = get()
        val repo: com.example.mermaidmaker.domain.repository.DiagramRepository = get()
        val gen: com.example.mermaidmaker.domain.usecase.GenerateAiDiagramUseCase = get()
        val fix: com.example.mermaidmaker.domain.usecase.FixMermaidCodeUseCase = get()
        val prefs: com.example.mermaidmaker.data.local.prefs.EditorPreferences = get()
        val vm: MermaidEditorViewModel = MermaidEditorViewModel(
            builtIn,
            byType,
            repo,
            gen,
            fix,
            prefs
        )
        vm
    }
    viewModel { ApiKeyViewModel(get(), get()) }
}