package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.model.DiagramExplanation
import com.example.mermaidmaker.domain.repository.ApiKeyRepository
import com.example.mermaidmaker.domain.service.AiDiagramService

/**
 * Use case to explain an existing Mermaid diagram and suggest improvements.
 */
class ExplainDiagramUseCase(
    private val aiDiagramService: AiDiagramService,
    private val apiKeyRepository: ApiKeyRepository
) {
    suspend operator fun invoke(
        source: String,
        provider: AiProvider? = null
    ): Result<DiagramExplanation> {
        if (source.isBlank()) return Result.failure(IllegalArgumentException("Diagram is empty"))

        val selected = provider ?: getAvailableProvider()
            ?: return Result.failure(Exception("No AI provider configured. Add an API key in Settings."))

        val apiKey = apiKeyRepository.getApiKey(selected).getOrNull()
            ?: return Result.failure(Exception("No API key found for ${selected.displayName}."))

        return aiDiagramService.explainDiagram(source, selected, apiKey)
    }

    private suspend fun getAvailableProvider(): AiProvider? {
        val openAi = apiKeyRepository.getApiKey(AiProvider.OPENAI)
        if (openAi.isSuccess && !openAi.getOrNull().isNullOrBlank()) return AiProvider.OPENAI

        val gemini = apiKeyRepository.getApiKey(AiProvider.GEMINI)
        if (gemini.isSuccess && !gemini.getOrNull().isNullOrBlank()) return AiProvider.GEMINI

        return null
    }
}



