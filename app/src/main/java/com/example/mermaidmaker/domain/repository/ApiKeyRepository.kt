package com.example.mermaidmaker.domain.repository

import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.model.ApiKeyConfiguration
import kotlinx.coroutines.flow.Flow

interface ApiKeyRepository {
    
    /**
     * Store an API key for the specified provider
     */
    suspend fun storeApiKey(provider: AiProvider, apiKey: String): Result<Unit>
    
    /**
     * Get the API key for the specified provider
     */
    suspend fun getApiKey(provider: AiProvider): Result<String?>
    
    /**
     * Get all stored API key configurations
     */
    fun getApiKeyConfigurations(): Flow<List<ApiKeyConfiguration>>
    
    /**
     * Remove the API key for the specified provider
     */
    suspend fun removeApiKey(provider: AiProvider): Result<Unit>
    
    /**
     * Check if an API key exists for the specified provider
     */
    suspend fun hasApiKey(provider: AiProvider): Boolean
    
    /**
     * Mark an API key as validated/invalidated
     */
    suspend fun updateKeyValidationStatus(
        provider: AiProvider, 
        isValidated: Boolean
    ): Result<Unit>
    
    /**
     * Clear all stored API keys
     */
    suspend fun clearAllApiKeys(): Result<Unit>
}