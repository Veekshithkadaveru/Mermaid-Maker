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
    viewModel { MermaidEditorViewModel(get(), get(), get(), get()) }
    viewModel { ApiKeyViewModel(get(), get()) }
}