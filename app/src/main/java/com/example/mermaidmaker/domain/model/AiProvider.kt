package com.example.mermaidmaker.domain.model

enum class AiProvider(
    val displayName: String,
    val apiKeyLabel: String,
    val baseUrl: String,
    val supportedFeatures: Set<AiFeature>
) {
    OPENAI(
        displayName = "OpenAI (GPT)",
        apiKeyLabel = "OpenAI API Key",
        baseUrl = "https://api.openai.com/v1/",
        supportedFeatures = setOf(
            AiFeature.TEXT_TO_DIAGRAM,
            AiFeature.DIAGRAM_IMPROVEMENT,
            AiFeature.TEMPLATE_GENERATION
        )
    ),
    GEMINI(
        displayName = "Google Gemini",
        apiKeyLabel = "Gemini API Key",
        baseUrl = "https://generativelanguage.googleapis.com/v1/",
        supportedFeatures = setOf(
            AiFeature.TEXT_TO_DIAGRAM,
            AiFeature.TEMPLATE_GENERATION
        )
    );

    val isAvailable: Boolean
        get() = true // All providers are available in MVP
}

enum class AiFeature(val displayName: String) {
    TEXT_TO_DIAGRAM("Generate diagrams from text"),
    DIAGRAM_IMPROVEMENT("Improve existing diagrams"),
    TEMPLATE_GENERATION("Generate custom templates")
}

data class ApiKeyConfiguration(
    val provider: AiProvider,
    val apiKey: String,
    val isValidated: Boolean = false,
    val lastValidated: Long? = null
)