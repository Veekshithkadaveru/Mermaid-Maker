package com.example.mermaidmaker.domain.service

import com.example.mermaidmaker.domain.model.AiProvider

/**
 * Service for AI-powered diagram generation
 */
interface AiDiagramService {

    /**
     * Generates a Mermaid diagram from natural language description
     *
     * @param prompt Natural language description of the desired diagram
     * @param diagramType Type of diagram to generate (FLOWCHART, SEQUENCE, etc.)
     * @param provider AI provider to use (OpenAI, Gemini)
     * @param apiKey User's API key for the selected provider
     * @return Generated Mermaid code or throws exception on failure
     */
    suspend fun generateDiagram(
        prompt: String,
        diagramType: String,
        provider: AiProvider,
        apiKey: String
    ): Result<String>

    /**
     * Validates that the user's API key is working
     *
     * @param provider AI provider to validate
     * @param apiKey User's API key
     * @return true if key is valid, false otherwise
     */
    suspend fun validateApiKey(
        provider: AiProvider,
        apiKey: String
    ): Boolean
}