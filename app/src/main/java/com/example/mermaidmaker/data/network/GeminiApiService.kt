package com.example.mermaidmaker.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Gemini API service for validation and content generation
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

    /**
     * Generate content using Gemini for the given model
     */
    @POST("models/{model}:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Path("model") model: String,
        @Body request: GeminiGenerateRequest
    ): Response<GeminiGenerateResponse>
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

// Models for content generation
data class GeminiGenerateRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>? = null
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Double = 0.3,
    val topK: Int = 40,
    val topP: Double = 0.95,
    val maxOutputTokens: Int = 1500,
    val stopSequences: List<String>? = null
)

data class GeminiGenerateResponse(
    val candidates: List<GeminiCandidate>? = null,
    val promptFeedback: GeminiPromptFeedback? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null,
    val index: Int? = null,
    val safetyRatings: List<GeminiSafetyRating>? = null
)

data class GeminiPromptFeedback(
    val safetyRatings: List<GeminiSafetyRating>? = null
)

data class GeminiSafetyRating(
    val category: String? = null,
    val probability: String? = null
)

