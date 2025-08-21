package com.example.mermaidmaker

import android.app.Application
import com.example.mermaidmaker.di.databaseModule
import com.example.mermaidmaker.di.repositoryModule
import com.example.mermaidmaker.di.useCaseModule
import com.example.mermaidmaker.di.viewModelModule
import com.example.mermaidmaker.domain.usecase.InitializeTemplatesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MermaidMakerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@MermaidMakerApp)
            modules(
                databaseModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )
        }
        
        // Initialize templates on app startup
        val koin = org.koin.core.context.GlobalContext.get()
        val initializeTemplatesUseCase = koin.get<InitializeTemplatesUseCase>()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                initializeTemplatesUseCase()
            } catch (e: Exception) {
                // Log error but don't crash the app
                android.util.Log.e("MermaidMakerApp", "Failed to initialize templates", e)
            }
        }
    }
}

