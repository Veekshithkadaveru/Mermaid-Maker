package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.data.database.DatabaseSeeder
import javax.inject.Inject

class InitializeTemplatesUseCase @Inject constructor(
    private val databaseSeeder: DatabaseSeeder
) {
    suspend operator fun invoke() {
        databaseSeeder.seedDatabaseIfNeeded()
    }
    
    suspend fun forceReload() {
        databaseSeeder.forceReseedTemplates()
    }
    
    suspend fun isInitialized(): Boolean {
        return databaseSeeder.isDatabaseSeeded()
    }
    
    suspend fun getInitializationInfo() = databaseSeeder.getDatabaseSeedInfo()
}