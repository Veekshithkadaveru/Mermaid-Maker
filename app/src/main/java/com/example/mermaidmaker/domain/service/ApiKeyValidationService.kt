package com.example.mermaidmaker.domain.service

import com.example.mermaidmaker.domain.model.AiProvider

interface ApiKeyValidationService {
    
    /**
     * Validate an API key for the specified provider
     * @param provider The AI provider
     * @param apiKey The API key to validate
     * @return Result<Boolean> indicating validation success/failure
     */
    suspend fun validateApiKey(provider: AiProvider, apiKey: String): Result<Boolean>
    
    /**
     * Test connection with a simple API call
     * @param provider The AI provider
     * @param apiKey The API key to test
     * @return Result<String> with response or error message
     */
    suspend fun testConnection(provider: AiProvider, apiKey: String): Result<String>
}