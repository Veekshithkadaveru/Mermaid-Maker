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
            val errorBody = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            val suffix = if (!errorBody.isNullOrBlank()) " - ${errorBody.take(500)}" else ""
            when (e.code()) {
                401 -> Result.failure(Exception("Invalid API key. Please check your ${provider.displayName} API key."))
                429 -> Result.failure(Exception("Rate limit exceeded. Please try again later."))
                402 -> Result.failure(Exception("Insufficient credits or quota exceeded."))
                503 -> Result.failure(Exception("Service temporarily unavailable. Please try again in a few minutes."))
                else -> Result.failure(Exception("API error (${e.code()}): ${e.message()}$suffix"))
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
            model = "gpt-4o-mini",
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
                maxOutputTokens = 600
            )
        )
        // Discover available models that support generateContent
        val availableModels = try {
            getGeminiModelsSupportingGenerateContent(apiKey)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to list Gemini models, falling back to defaults: ${'$'}{e.message}")
            emptyList()
        }

        // Prefer lighter/cheaper models first
        val preferenceOrder = listOf(
            "gemini-2.5-flash",
            "gemini-2.0-flash-001",
            "gemini-2.0-flash",
            "gemini-1.5-flash-8b",
            "gemini-1.5-flash-002",
            "gemini-1.5-flash-001",
            "gemini-1.5-flash",
            "gemini-2.5-pro",
            "gemini-2.5-pro-preview-06-05",
            "gemini-2.5-pro-preview-05-06",
            "gemini-2.5-pro-preview-03-25",
            "gemini-1.5-pro-002",
            "gemini-1.5-pro-001",
            "gemini-1.5-pro"
        )

        val modelsToTry = if (availableModels.isNotEmpty()) {
            val preferred = preferenceOrder.filter { it in availableModels }
            val nonPreferred = availableModels.filterNot { it in preferenceOrder }
            (preferred + nonPreferred).distinct()
        } else {
            preferenceOrder
        }

        var lastError: HttpException? = null
        for (model in modelsToTry) {
            Log.d(TAG, "Trying Gemini model: $model")
            val response = geminiApiService.generateContent(apiKey, model, request)

            if (!response.isSuccessful) {
                val httpEx = HttpException(response)
                if (response.code() == 404) {
                    lastError = httpEx
                    Log.w(TAG, "Model not found (404) for $model, trying next model")
                    continue
                } else if (response.code() == 429 || response.code() == 503) {
                    val retryAfterHeader = response.headers()["Retry-After"]
                    val retryMs = parseRetryAfterMs(retryAfterHeader) ?: 1500L
                    Log.w(TAG, "Received ${response.code()} for $model. Retrying after ${retryMs}ms once, then moving to next model")
                    delay(retryMs)
                    val retryResponse = geminiApiService.generateContent(apiKey, model, request)
                    if (!retryResponse.isSuccessful) {
                        Log.w(TAG, "Retry for $model returned ${retryResponse.code()}, moving to next model")
                        continue
                    }
                    val retryBody = retryResponse.body() ?: continue
                    val candidates = retryBody.candidates
                    if (candidates.isNullOrEmpty()) continue
                    val first = candidates.first()
                    val parts = first.content?.parts
                    if (parts.isNullOrEmpty()) continue
                    val text = parts.first().text
                    Log.d(TAG, "Generated text from Gemini: $text")
                    return text
                } else {
                    throw httpEx
                }
            }

            val geminiResponse = response.body() ?: continue
            Log.d(TAG, "Gemini response: $geminiResponse")
            val candidates = geminiResponse.candidates
            if (candidates.isNullOrEmpty()) continue
            val firstCandidate = candidates.first()
            Log.d(TAG, "First candidate: $firstCandidate")
            val parts = firstCandidate.content?.parts
            if (parts.isNullOrEmpty()) continue
            val generatedText = parts.first().text
            Log.d(TAG, "Generated text from Gemini: $generatedText")
            return generatedText
        }

        lastError?.let { throw it }
        throw Exception("Gemini model not found or returned no usable content")
    }

    private suspend fun getGeminiModelsSupportingGenerateContent(apiKey: String): List<String> {
        val response = geminiApiService.validateApiKey(apiKey)
        if (!response.isSuccessful) return emptyList()
        val body = response.body() ?: return emptyList()
        val result = mutableListOf<String>()
        body.models?.forEach { model ->
            val name = model.name ?: return@forEach
            val shortName = name.removePrefix("models/")
            val methods = model.supportedGenerationMethods ?: emptyList()
            if (methods.contains("generateContent")) {
                result.add(shortName)
            }
        }
        return result
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
        private fun parseRetryAfterMs(header: String?): Long? {
            if (header.isNullOrBlank()) return null
            return header.trim().toLongOrNull()?.let { it * 1000 }
        }
    }
}