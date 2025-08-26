package com.example.mermaidmaker.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Gemini API service for validation
 */
interface GeminiApiService {
    
    /**
     * Simple API call to validate the API key
     * Uses the models endpoint to check if the key is valid
     */
    @GET("models")
    suspend fun validateApiKey(
        @Query("key") apiKey: String
    ): Response<GeminiModelsResponse>
}

data class GeminiModelsResponse(
    val models: List<GeminiModel>
)

data class GeminiModel(
    val name: String,
    val displayName: String,
    val description: String,
    val inputTokenLimit: Int,
    val outputTokenLimit: Int,
    val supportedGenerationMethods: List<String>
)

