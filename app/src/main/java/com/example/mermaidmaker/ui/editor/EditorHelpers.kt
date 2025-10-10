package com.example.mermaidmaker.ui.editor

data class EditorLintError(val line: Int, val message: String)

fun generateAutoFilename(): String {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.getDefault())
    val timestamp = formatter.format(java.util.Date())
    return "mermaid_$timestamp.txt"
}

/**
 * Very fast Mermaid lint: mode keyword presence and basic bracket balance per line
 */
fun analyzeMermaidFast(text: String): List<EditorLintError> {
    val errors = mutableListOf<EditorLintError>()
    if (text.isBlank()) return emptyList()

    val lines = text.split('\n')

    val keywords = setOf(
        "graph", "flowchart", "sequenceDiagram", "classDiagram",
        "stateDiagram", "stateDiagram-v2", "erDiagram", "journey", "gantt", "pie", "gitgraph"
    )
    val firstNonEmptyIndex = lines.indexOfFirst { it.isNotBlank() }
    if (firstNonEmptyIndex >= 0) {
        val first = lines[firstNonEmptyIndex].trimStart()
        if (keywords.none { first.startsWith(it) }) {
            errors.add(
                EditorLintError(
                    firstNonEmptyIndex,
                    "Missing diagram type (e.g., graph TD, sequenceDiagram)"
                )
            )
        }
    }

    val pairs = mapOf('(' to ')', '[' to ']', '{' to '}')
    lines.forEachIndexed { idx, line ->
        val stack = ArrayDeque<Char>()
        line.forEach { ch ->
            if (ch in pairs.keys) stack.addLast(ch)
            else if (ch in pairs.values) {
                if (stack.isEmpty() || pairs[stack.removeLast()] != ch) {
                    errors.add(EditorLintError(idx, "Unbalanced brackets"))
                    return@forEachIndexed
                }
            }
        }
        if (stack.isNotEmpty()) {
            errors.add(EditorLintError(idx, "Unclosed bracket"))
        }
    }

    return errors
}


