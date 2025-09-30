package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.repository.ApiKeyRepository
import com.example.mermaidmaker.domain.service.AiDiagramService

/**
 * Use case to fix/auto-correct Mermaid syntax using AI.
 */
class FixMermaidCodeUseCase(
    private val aiDiagramService: AiDiagramService,
    private val apiKeyRepository: ApiKeyRepository
) {

    /**
     * Attempts to fix invalid Mermaid code. If the input is plain text, the AI should shape it
     * into a valid diagram. Returns corrected Mermaid code on success.
     */
    suspend operator fun invoke(
        invalidOrRawMermaid: String,
        provider: AiProvider? = null
    ): Result<String> {
        if (invalidOrRawMermaid.isBlank()) {
            return Result.failure(IllegalArgumentException("Nothing to fix. Paste your Mermaid or text."))
        }

        val selectedProvider = provider ?: getAvailableProvider()
            ?: return Result.failure(Exception("No AI provider configured. Please add an API key in Settings."))

        val apiKeyResult = apiKeyRepository.getApiKey(selectedProvider)
        if (apiKeyResult.isFailure) {
            return Result.failure(Exception("Failed to retrieve API key: ${apiKeyResult.exceptionOrNull()?.message}"))
        }
        val apiKey = apiKeyResult.getOrNull()
            ?: return Result.failure(Exception("No API key found for ${selectedProvider.displayName}. Please add one in Settings."))

        // Best-effort validation removed for fix flow to avoid blocking on provider quirks.
        // We'll attempt AI fix directly and surface any real errors.

        val fixPrompt = "Fix the following into valid Mermaid code, or if it's plain text, convert it into a clear, minimal Mermaid diagram. Return ONLY Mermaid code with no markdown fences or explanations.\n\nINPUT:\n" + invalidOrRawMermaid

        return aiDiagramService.fixMermaidCode(
            source = fixPrompt,
            provider = selectedProvider,
            apiKey = apiKey
        )
    }

    private suspend fun getAvailableProvider(): AiProvider? {
        val openAi = apiKeyRepository.getApiKey(AiProvider.OPENAI)
        if (openAi.isSuccess && !openAi.getOrNull().isNullOrBlank()) return AiProvider.OPENAI

        val gemini = apiKeyRepository.getApiKey(AiProvider.GEMINI)
        if (gemini.isSuccess && !gemini.getOrNull().isNullOrBlank()) return AiProvider.GEMINI

        return null
    }
}


