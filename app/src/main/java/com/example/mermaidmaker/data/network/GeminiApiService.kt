package com.example.mermaidmaker.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * Gemini API service for validation
 */
interface GeminiApiService {
    
    /**
     * Simple API call to validate the API key
     * Uses the models endpoint to check if the key is valid
     * Security: Using x-goog-api-key header instead of query parameter
     */
    @GET("models")
    suspend fun validateApiKey(
        @Header("x-goog-api-key") apiKey: String
    ): Response<GeminiModelsResponse>
}

data class GeminiModelsResponse(
    val models: List<GeminiModel>? = null
)

data class GeminiModel(
    val name: String? = null,
    val displayName: String? = null,
    val description: String? = null,
    val inputTokenLimit: Int? = null,
    val outputTokenLimit: Int? = null,
    val supportedGenerationMethods: List<String>? = null
)

