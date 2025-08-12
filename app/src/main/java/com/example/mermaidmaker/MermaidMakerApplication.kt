package com.example.mermaidmaker

import android.app.Application
import com.example.mermaidmaker.data.database.DatabaseSeeder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MermaidMakerApplication : Application() {
    
    @Inject
    lateinit var databaseSeeder: DatabaseSeeder
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database with built-in templates on app start
        databaseSeeder.seedDatabaseIfNeeded()
    }
}