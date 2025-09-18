package com.example.mermaidmaker.data.service

import android.util.Log
import com.example.mermaidmaker.data.ai.InvalidMermaidException
import com.example.mermaidmaker.data.ai.MermaidPrompts
import com.example.mermaidmaker.data.ai.MermaidResponseProcessor
import com.example.mermaidmaker.data.network.ChatCompletionRequest
import com.example.mermaidmaker.data.network.ChatMessage
import com.example.mermaidmaker.data.network.GeminiApiService
import com.example.mermaidmaker.data.network.GeminiContent
import com.example.mermaidmaker.data.network.GeminiGenerateRequest
import com.example.mermaidmaker.data.network.GeminiGenerationConfig
import com.example.mermaidmaker.data.network.GeminiPart
import com.example.mermaidmaker.data.network.OpenAiApiService
import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.service.AiDiagramService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Implementation of AI diagram generation service
 */
class AiDiagramServiceImpl(
    private val openAiApiService: OpenAiApiService,
    private val geminiApiService: GeminiApiService,
    private val responseProcessor: MermaidResponseProcessor = MermaidResponseProcessor()
) : AiDiagramService {

    override suspend fun generateDiagram(
        prompt: String,
        diagramType: String,
        provider: AiProvider,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {

        try {
            // Validate input
            if (prompt.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("Prompt cannot be empty"))
            }

            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("API key cannot be empty"))
            }

            val rawResponse = when (provider) {
                AiProvider.OPENAI -> generateWithOpenAI(prompt, diagramType, apiKey)
                AiProvider.GEMINI -> generateWithGemini(prompt, diagramType, apiKey)
            }

            // Process the response to extract clean Mermaid code
            val mermaidCode = responseProcessor.extractMermaidCode(rawResponse)

            Result.success(mermaidCode)

        } catch (e: InvalidMermaidException) {
            Result.failure(Exception("Generated diagram has invalid syntax: ${e.message}"))
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> Result.failure(Exception("Invalid API key. Please check your ${provider.displayName} API key."))
                429 -> Result.failure(Exception("Rate limit exceeded. Please try again later."))
                402 -> Result.failure(Exception("Insufficient credits or quota exceeded."))
                503 -> Result.failure(Exception("Service temporarily unavailable. Please try again in a few minutes."))
                else -> Result.failure(Exception("API error (${e.code()}): ${e.message()}"))
            }
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Request timed out. Please try again."))
        } catch (e: IOException) {
            Result.failure(Exception("Network error. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(Exception("Unexpected error: ${e.message}"))
        }
    }

    override suspend fun validateApiKey(
        provider: AiProvider,
        apiKey: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            when (provider) {
                AiProvider.OPENAI -> {
                    val response = openAiApiService.validateApiKey("Bearer $apiKey")
                    response.isSuccessful
                }

                AiProvider.GEMINI -> {
                    val response = geminiApiService.validateApiKey(apiKey)
                    response.isSuccessful
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Generate diagram using OpenAI's GPT API
     */
    private suspend fun generateWithOpenAI(
        prompt: String,
        diagramType: String,
        apiKey: String
    ): String {
        val systemPrompt = MermaidPrompts.getSystemPrompt(diagramType)

        val request = ChatCompletionRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                ChatMessage("system", systemPrompt),
                ChatMessage("user", prompt)
            ),
            max_tokens = 1500,
            temperature = 0.3 // Low temperature for more consistent output
        )

        val response = openAiApiService.generateDiagram("Bearer $apiKey", request)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val chatResponse = response.body()
            ?: throw Exception("Empty response from OpenAI")

        if (chatResponse.choices.isEmpty()) {
            throw Exception("No response choices from OpenAI")
        }

        return chatResponse.choices.first().message.content
    }

    /**
     * Generate diagram using Google Gemini API with retry logic
     */
    private suspend fun generateWithGemini(
        prompt: String,
        diagramType: String,
        apiKey: String
    ): String {
        return retryWithExponentialBackoff {
            generateGeminiContent(prompt, diagramType, apiKey)
        }
    }

    /**
     * Core Gemini content generation
     */
    private suspend fun generateGeminiContent(
        prompt: String,
        diagramType: String,
        apiKey: String
    ): String {
        val systemPrompt = MermaidPrompts.getSystemPrompt(diagramType)

        // Combine system prompt and user prompt for Gemini
        val combinedPrompt = "$systemPrompt\n\nUser request: $prompt"

        val request = GeminiGenerateRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = combinedPrompt)
                    )
                )
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.3,
                topK = 40,
                topP = 0.95,
                maxOutputTokens = 1500
            )
        )

        val response = geminiApiService.generateContent(apiKey, request)

        if (!response.isSuccessful) {
            throw HttpException(response)
        }

        val geminiResponse = response.body()
            ?: throw Exception("Empty response from Gemini")

        Log.d(TAG, "Gemini response: $geminiResponse")

        val candidates = geminiResponse.candidates
        if (candidates.isNullOrEmpty()) {
            Log.e(TAG, "No response candidates from Gemini")
            throw Exception("No response candidates from Gemini")
        }

        val firstCandidate = candidates.first()
        Log.d(TAG, "First candidate: $firstCandidate")

        val content = firstCandidate.content
            ?: throw Exception("No content in Gemini response")

        val parts = content.parts
        if (parts.isEmpty()) {
            throw Exception("No parts in Gemini response content")
        }

        val generatedText = parts.first().text
        Log.d(TAG, "Generated text from Gemini: $generatedText")

        return generatedText
    }

    /**
     * Retry logic with exponential backoff for handling temporary service unavailability
     */
    private suspend fun <T> retryWithExponentialBackoff(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 10000,
        factor: Double = 2.0,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxRetries - 1) { attempt ->
            try {
                return block()
            } catch (e: HttpException) {
                if (e.code() == 503) {
                    Log.d(
                        TAG,
                        "Attempt ${attempt + 1} failed with 503, retrying in ${currentDelay}ms"
                    )
                    delay(currentDelay)
                    currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                } else {
                    throw e
                }
            }
        }
        return block()
    }

    companion object {
        private const val TAG = "AiDiagramService"
    }
}