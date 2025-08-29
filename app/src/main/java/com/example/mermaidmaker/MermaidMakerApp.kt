package com.example.mermaidmaker

import android.app.Application
import com.example.mermaidmaker.di.databaseModule
import com.example.mermaidmaker.di.networkModule
import com.example.mermaidmaker.di.repositoryModule
import com.example.mermaidmaker.di.useCaseModule
import com.example.mermaidmaker.di.viewModelModule
import com.example.mermaidmaker.domain.usecase.InitializeTemplatesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MermaidMakerApp : Application() {
    
    // App-scoped coroutine scope with SupervisorJob for proper lifecycle management
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        val appModule = module {
            single<CoroutineScope> { applicationScope }
        }
        
        startKoin {
            androidContext(this@MermaidMakerApp)
            modules(
                appModule,
                networkModule,
                databaseModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )
        }
        
        // Initialize templates on app startup using managed scope
        val koin = org.koin.core.context.GlobalContext.get()
        val initializeTemplatesUseCase = koin.get<InitializeTemplatesUseCase>()
        
        applicationScope.launch {
            try {
                initializeTemplatesUseCase()
            } catch (e: java.io.IOException) {
                android.util.Log.e("MermaidMakerApp", "IO error during template initialization", e)
            } catch (e: kotlinx.serialization.SerializationException) {
                android.util.Log.e("MermaidMakerApp", "Template parsing error during initialization", e)
            } catch (e: android.database.sqlite.SQLiteException) {
                android.util.Log.e("MermaidMakerApp", "Database error during template initialization", e)
            }
        }
    }
}

