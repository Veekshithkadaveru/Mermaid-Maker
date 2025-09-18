package com.example.mermaidmaker.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * OpenAI API service for validation and text-to-diagram generation
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

    /**
     * Generate Mermaid diagram from natural language description
     */
    @POST("chat/completions")
    suspend fun generateDiagram(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>
}

// Models for validation endpoint
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

// Models for chat completion endpoint
data class ChatCompletionRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatMessage>,
    val max_tokens: Int = 1000,
    val temperature: Double = 0.3
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class ChatCompletionResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChatChoice>,
    val usage: Usage?
)

data class ChatChoice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String?
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

