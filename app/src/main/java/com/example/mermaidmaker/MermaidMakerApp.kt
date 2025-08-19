package com.example.mermaidmaker

import android.app.Application
import com.example.mermaidmaker.di.databaseModule
import com.example.mermaidmaker.di.repositoryModule
import com.example.mermaidmaker.di.useCaseModule
import com.example.mermaidmaker.di.viewModelModule
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
    }
}

