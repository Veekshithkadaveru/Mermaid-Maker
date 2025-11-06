package com.example.mermaidmaker.domain.model

/**
 * Structured explanation of a Mermaid diagram.
 * Designed to be resilient to partial data; all lists are optional.
 */
data class DiagramExplanation(
    val title: String? = null,
    val summary: String? = null,
    val components: List<ExplainedComponent> = emptyList(),
    val flows: List<ExplainedFlow> = emptyList(),
    val smells: List<ExplainedSmell> = emptyList(),
    val suggestions: List<ExplainedSuggestion> = emptyList(),
    val tags: List<String> = emptyList(),
    val risk: ExplainedRisk? = null,
    val rawText: String? = null
)

data class ExplainedComponent(
    val id: String? = null,
    val role: String? = null
)

data class ExplainedFlow(
    val name: String? = null,
    val steps: List<String> = emptyList()
)

data class ExplainedSmell(
    val type: String? = null,
    val item: String? = null,
    val severity: String? = null,
    val explanation: String? = null
)

data class ExplainedSuggestion(
    val title: String? = null,
    val rationale: String? = null,
    val type: String? = null,
    val diff: String? = null,
    val patch: String? = null,
    val code: String? = null
)

data class ExplainedRisk(
    val complexity: String? = null,
    val maintainability: String? = null,
    val potential_issues: String? = null,
    val explanation: String? = null
)



