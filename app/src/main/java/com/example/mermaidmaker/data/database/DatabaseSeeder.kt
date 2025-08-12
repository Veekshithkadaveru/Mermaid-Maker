package com.example.mermaidmaker.data.database

import android.content.Context
import android.content.SharedPreferences
import com.example.mermaidmaker.data.template.TemplateLoader
import com.example.mermaidmaker.data.template.TemplateVersionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val templateLoader: TemplateLoader
) {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val PREFS_NAME = "mermaid_maker_db_seeder"
        private const val KEY_DB_VERSION = "database_version"
        private const val KEY_TEMPLATE_VERSION = "template_version"
        private const val KEY_TEMPLATES_SEEDED = "templates_seeded"
        private const val CURRENT_DB_VERSION = 1
    }
    
    fun seedDatabaseIfNeeded() {
        coroutineScope.launch {
            val currentVersion = prefs.getInt(KEY_DB_VERSION, 0)
            val currentTemplateVersion = prefs.getInt(KEY_TEMPLATE_VERSION, 0)
            val templatesSeeded = prefs.getBoolean(KEY_TEMPLATES_SEEDED, false)
            
            when {
                // First time app launch - seed everything
                currentVersion == 0 -> {
                    seedInitialData()
                    updateSeederPreferences()
                }
                
                // App updated but templates not seeded
                !templatesSeeded -> {
                    seedTemplates()
                    updateTemplatePreferences()
                }
                
                // Template version updated - reseed templates
                currentTemplateVersion < TemplateVersionManager.CURRENT_VERSION -> {
                    handleTemplateVersionUpgrade(currentTemplateVersion)
                    updateTemplatePreferences()
                }
                
                // Database version updated - reseed if needed
                currentVersion < CURRENT_DB_VERSION -> {
                    handleDatabaseUpgrade(currentVersion)
                    updateSeederPreferences()
                }
            }
        }
    }
    
    private suspend fun seedInitialData() {
        seedTemplates()
        // Future: Add other initial data seeding here
        // seedSampleDiagrams()
        // seedUserPreferences()
    }
    
    private suspend fun seedTemplates() {
        try {
            templateLoader.loadBuiltInTemplates()
        } catch (e: Exception) {
            // Log error in production
            // For now, silently fail to prevent app crashes
        }
    }
    
    private suspend fun handleDatabaseUpgrade(fromVersion: Int) {
        when (fromVersion) {
            0 -> {
                // Upgrade from initial version
                seedTemplates()
            }
            // Future version upgrades can be handled here
        }
    }
    
    private suspend fun handleTemplateVersionUpgrade(fromVersion: Int) {
        // Get new templates in this version
        val newTemplates = TemplateVersionManager.getNewTemplatesInVersion(
            fromVersion, 
            TemplateVersionManager.CURRENT_VERSION
        )
        
        if (newTemplates.isNotEmpty()) {
            // Reload all built-in templates to ensure consistency
            templateLoader.reloadBuiltInTemplates()
        }
    }
    
    private fun updateSeederPreferences() {
        prefs.edit()
            .putInt(KEY_DB_VERSION, CURRENT_DB_VERSION)
            .putInt(KEY_TEMPLATE_VERSION, TemplateVersionManager.CURRENT_VERSION)
            .putBoolean(KEY_TEMPLATES_SEEDED, true)
            .apply()
    }
    
    private fun updateTemplatePreferences() {
        prefs.edit()
            .putInt(KEY_TEMPLATE_VERSION, TemplateVersionManager.CURRENT_VERSION)
            .putBoolean(KEY_TEMPLATES_SEEDED, true)
            .apply()
    }
    
    suspend fun forceReseedTemplates() {
        templateLoader.reloadBuiltInTemplates()
        updateTemplatePreferences()
    }
    
    suspend fun isDatabaseSeeded(): Boolean {
        return prefs.getBoolean(KEY_TEMPLATES_SEEDED, false) &&
                templateLoader.isTemplateDataInitialized()
    }
    
    suspend fun getDatabaseSeedInfo(): DatabaseSeedInfo {
        return DatabaseSeedInfo(
            isSeeded = isDatabaseSeeded(),
            version = prefs.getInt(KEY_DB_VERSION, 0),
            templateVersion = prefs.getInt(KEY_TEMPLATE_VERSION, 0),
            templateCount = templateLoader.getTemplateCount(),
            builtInTemplateCount = templateLoader.getBuiltInTemplateCount()
        )
    }
}

data class DatabaseSeedInfo(
    val isSeeded: Boolean,
    val version: Int,
    val templateVersion: Int,
    val templateCount: Int,
    val builtInTemplateCount: Int
)