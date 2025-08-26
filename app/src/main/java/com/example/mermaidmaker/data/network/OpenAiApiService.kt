package com.example.mermaidmaker.data.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * OpenAI API service for validation
 */
interface OpenAiApiService {
    
    /**
     * Simple API call to validate the API key
     * Uses the models endpoint as it's lightweight and requires authentication
     */
    @GET("models")
    suspend fun validateApiKey(
        @Header("Authorization") authorization: String
    ): Response<OpenAiModelsResponse>
}

data class OpenAiModelsResponse(
    val `object`: String,
    val data: List<OpenAiModel>
)

data class OpenAiModel(
    val id: String,
    val `object`: String,
    val created: Long,
    val owned_by: String
)

