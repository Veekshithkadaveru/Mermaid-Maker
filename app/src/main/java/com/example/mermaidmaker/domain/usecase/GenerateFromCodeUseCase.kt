package com.example.mermaidmaker.domain.usecase

import android.util.Log
import com.example.mermaidmaker.data.ai.ContextualAnalyzer
import com.example.mermaidmaker.data.ai.CodeLanguage
import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.repository.ApiKeyRepository
import com.example.mermaidmaker.domain.service.AiDiagramService

/**
 * Use case for generating Mermaid diagrams from source code using AI
 */
class GenerateFromCodeUseCase(
    private val aiDiagramService: AiDiagramService,
    private val apiKeyRepository: ApiKeyRepository
) {

    /**
     * Generates a Mermaid diagram from source code
     *
     * @param code Source code to analyze
     * @param language Programming language of the code
     * @param provider AI provider to use (optional, will use first available if not specified)
     * @return Result containing generated Mermaid code or error
     */
    suspend operator fun invoke(
        code: String,
        language: CodeLanguage,
        provider: AiProvider? = null
    ): Result<String> {

        // Validate input
        if (code.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter some code to analyze"))
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

        // Analyze the code to infer structures and suggested diagram type
        val analysis = try {
            ContextualAnalyzer.analyzeCode(code, language)
        } catch (e: Exception) {
            Log.w("GenerateFromCodeUseCase", "Failed to analyze code", e)
            return Result.failure(Exception("Failed to analyze code structure: ${e.message}"))
        }

        val suggestedTypeUpper = when (analysis.suggestedDiagramType.lowercase()) {
            "class" -> "CLASS"
            "er" -> "ER"
            "flowchart" -> "FLOWCHART"
            "sequence" -> "SEQUENCE"
            "state" -> "STATE"
            "gantt" -> "GANTT"
            "pie" -> "PIE"
            else -> "CLASS"
        }

        // Build a concise user prompt derived from analysis
        val promptBuilder = StringBuilder()
        promptBuilder.appendLine("Analyze the following ${analysis.language.name.lowercase()} code and output ONLY valid Mermaid for a ${suggestedTypeUpper.lowercase()} diagram. No markdown fences, no explanations.")
        promptBuilder.appendLine()
        if (analysis.structures.isNotEmpty()) {
            promptBuilder.appendLine("Context summary (extracted):")
            analysis.structures.take(12).forEach { s ->
                val props = if (s.properties.isNotEmpty()) s.properties.joinToString() else ""
                val methods = if (s.methods.isNotEmpty()) s.methods.joinToString() else ""
                promptBuilder.append("- ${s.type}: ${s.name}")
                if (props.isNotEmpty()) promptBuilder.append(" | props: ${props}")
                if (methods.isNotEmpty()) promptBuilder.append(" | methods: ${methods}")
                promptBuilder.appendLine()
            }
            if (analysis.relationships.isNotEmpty()) {
                promptBuilder.appendLine("Relationships:")
                analysis.relationships.take(20).forEach { r ->
                    val label = r.label?.let { " (${it})" } ?: ""
                    promptBuilder.appendLine("- ${r.from} -> ${r.to} : ${r.type}${label}")
                }
            }
            promptBuilder.appendLine()
        }
        promptBuilder.appendLine("Code:")
        promptBuilder.appendLine("""")
        promptBuilder.appendLine(code.trim())
        promptBuilder.appendLine("""")

        val userPrompt = promptBuilder.toString()

        return aiDiagramService.generateDiagram(
            prompt = userPrompt,
            diagramType = suggestedTypeUpper,
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
            "GenerateFromCodeUseCase",
            "OpenAI key result: success=${openAiResult.isSuccess}, value=${
                openAiResult.getOrNull()?.take(10)
            }..."
        )
        if (openAiResult.isSuccess && !openAiResult.getOrNull().isNullOrBlank()) {
            Log.d("GenerateFromCodeUseCase", "Using OpenAI provider")
            return AiProvider.OPENAI
        }

        // Check Gemini second
        val geminiResult = apiKeyRepository.getApiKey(AiProvider.GEMINI)
        Log.d(
            "GenerateFromCodeUseCase",
            "Gemini key result: success=${geminiResult.isSuccess}, value=${
                geminiResult.getOrNull()?.take(10)
            }..."
        )
        if (geminiResult.isSuccess && !geminiResult.getOrNull().isNullOrBlank()) {
            Log.d("GenerateFromCodeUseCase", "Using Gemini provider")
            return AiProvider.GEMINI
        }

        Log.d("GenerateFromCodeUseCase", "No available provider found")
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