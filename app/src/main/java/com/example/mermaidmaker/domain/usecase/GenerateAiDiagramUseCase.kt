package com.example.mermaidmaker.domain.usecase

import android.util.Log
import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.repository.ApiKeyRepository
import com.example.mermaidmaker.domain.service.AiDiagramService

/**
 * Use case for generating Mermaid diagrams from natural language using AI
 */
class GenerateAiDiagramUseCase(
    private val aiDiagramService: AiDiagramService,
    private val apiKeyRepository: ApiKeyRepository
) {

    /**
     * Generates a Mermaid diagram from natural language description
     *
     * @param prompt User's natural language description
     * @param diagramType Type of diagram to generate
     * @param provider AI provider to use (optional, will use first available if not specified)
     * @return Result containing generated Mermaid code or error
     */
    suspend operator fun invoke(
        prompt: String,
        diagramType: String,
        provider: AiProvider? = null
    ): Result<String> {

        // Validate input
        if (prompt.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter a description for your diagram"))
        }

        // Determine which provider to use
        val selectedProvider = provider ?: getAvailableProvider()
        ?: return Result.failure(Exception("No AI provider configured. Please add an API key in Settings."))

        // Get API key for the selected provider
        val apiKeyResult = apiKeyRepository.getApiKey(selectedProvider)
        if (apiKeyResult.isFailure) {
            return Result.failure(Exception("Failed to retrieve API key: ${apiKeyResult.exceptionOrNull()?.message}"))
        }

        val apiKey = apiKeyResult.getOrNull()
            ?: return Result.failure(Exception("No API key found for ${selectedProvider.displayName}. Please add one in Settings."))

        // Validate API key before making request
        val isValidKey = try {
            aiDiagramService.validateApiKey(selectedProvider, apiKey)
        } catch (e: Exception) {
            false
        }

        if (!isValidKey) {
            return Result.failure(Exception("Invalid ${selectedProvider.displayName} API key. Please check your key in Settings."))
        }

        // Generate the diagram
        return aiDiagramService.generateDiagram(
            prompt = prompt.trim(),
            diagramType = diagramType.uppercase(),
            provider = selectedProvider,
            apiKey = apiKey
        )
    }

    /**
     * Gets the first available provider that has an API key configured
     * Prioritizes OpenAI, then falls back to Gemini
     */
    private suspend fun getAvailableProvider(): AiProvider? {
        // Check OpenAI first (generally more reliable)
        val openAiResult = apiKeyRepository.getApiKey(AiProvider.OPENAI)
        Log.d(
            "GenerateAiDiagramUseCase",
            "OpenAI key result: success=${openAiResult.isSuccess}, value=${
                openAiResult.getOrNull()?.take(10)
            }..."
        )
        if (openAiResult.isSuccess && !openAiResult.getOrNull().isNullOrBlank()) {
            Log.d("GenerateAiDiagramUseCase", "Using OpenAI provider")
            return AiProvider.OPENAI
        }

        // Check Gemini second
        val geminiResult = apiKeyRepository.getApiKey(AiProvider.GEMINI)
        Log.d(
            "GenerateAiDiagramUseCase",
            "Gemini key result: success=${geminiResult.isSuccess}, value=${
                geminiResult.getOrNull()?.take(10)
            }..."
        )
        if (geminiResult.isSuccess && !geminiResult.getOrNull().isNullOrBlank()) {
            Log.d("GenerateAiDiagramUseCase", "Using Gemini provider")
            return AiProvider.GEMINI
        }

        Log.d("GenerateAiDiagramUseCase", "No available provider found")
        return null
    }

    /**
     * Checks if AI generation is available (at least one provider has an API key)
     */
    suspend fun isAiGenerationAvailable(): Boolean {
        val provider = getAvailableProvider()
        return provider != null
    }

    /**
     * Gets list of configured providers
     */
    suspend fun getConfiguredProviders(): List<AiProvider> {
        val providers = mutableListOf<AiProvider>()

        val openAiResult = apiKeyRepository.getApiKey(AiProvider.OPENAI)
        if (openAiResult.isSuccess && !openAiResult.getOrNull().isNullOrBlank()) {
            providers.add(AiProvider.OPENAI)
        }

        val geminiResult = apiKeyRepository.getApiKey(AiProvider.GEMINI)
        if (geminiResult.isSuccess && !geminiResult.getOrNull().isNullOrBlank()) {
            providers.add(AiProvider.GEMINI)
        }

        return providers
    }
}