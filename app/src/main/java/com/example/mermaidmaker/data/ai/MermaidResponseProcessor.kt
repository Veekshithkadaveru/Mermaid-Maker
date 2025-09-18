package com.example.mermaidmaker.data.ai

import java.util.regex.Pattern

/**
 * Processes AI responses to extract and validate Mermaid diagram code
 */
class MermaidResponseProcessor {

    /**
     * Extracts clean Mermaid code from AI response
     * Handles markdown code blocks, extra text, and formatting issues
     */
    fun extractMermaidCode(aiResponse: String): String {
        // Remove markdown code blocks
        var cleaned = aiResponse
            .replace("```mermaid", "")
            .replace("```", "")
            .trim()

        // Remove common AI response prefixes/suffixes
        cleaned = removeCommonPrefixes(cleaned)

        // Extract only the diagram content
        val lines = cleaned.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("//") }

        if (lines.isEmpty()) {
            throw InvalidMermaidException("No diagram content found in AI response")
        }

        // Find the diagram start
        val diagramStartIndex = findDiagramStart(lines)
        if (diagramStartIndex == -1) {
            throw InvalidMermaidException("No valid diagram type found in response")
        }

        // Extract from diagram start to end
        val diagramLines = lines.subList(diagramStartIndex, lines.size)
        val result = diagramLines.joinToString("\n")

        // Validate the extracted code
        validateMermaidSyntax(result)

        return result
    }

    /**
     * Validates that the extracted code contains valid Mermaid syntax
     */
    private fun validateMermaidSyntax(mermaidCode: String) {
        val trimmed = mermaidCode.trim()

        if (trimmed.isEmpty()) {
            throw InvalidMermaidException("Empty diagram code")
        }

        // Check for valid diagram type at the start
        val validStarts = listOf(
            "graph ", "flowchart ", "sequenceDiagram", "classDiagram",
            "stateDiagram", "erDiagram", "gantt", "pie ", "gitGraph",
            "journey", "quadrantChart", "requirementDiagram", "c4Context"
        )

        val hasValidStart = validStarts.any { start ->
            trimmed.startsWith(start, ignoreCase = true)
        }

        if (!hasValidStart) {
            throw InvalidMermaidException("Diagram must start with a valid Mermaid diagram type")
        }

        validateBasicSyntax(trimmed)
    }

    /**
     * Performs basic syntax validation for common Mermaid issues
     */
    private fun validateBasicSyntax(code: String) {
        val lines = code.lines()

        // Check for balanced brackets
        validateBalancedBrackets(code)

        // Check for valid arrow syntax
        validateArrows(code)

        // Check for invalid characters that break Mermaid
        validateCharacters(code)

        // Warn about potentially problematic lines
        for ((index, line) in lines.withIndex()) {
            val lineNumber = index + 1

            // Check for lines that might cause issues
            if (line.contains("\"\"") && !line.contains("\"\" :")) {
                // Empty quotes might cause issues
                continue // Warning, not error
            }

            if (line.trim().endsWith("-->") || line.trim().endsWith("->")) {
                throw InvalidMermaidException("Line $lineNumber: Arrow without destination")
            }
        }
    }

    /**
     * Validates balanced brackets in the Mermaid code
     * Excludes arrow symbols like --> from bracket validation
     */
    private fun validateBalancedBrackets(code: String) {
        val brackets = mapOf('[' to ']', '(' to ')', '{' to '}')
        val stack = mutableListOf<Char>()

        val codeWithoutArrows = code
            .replace("-->", "   ")
            .replace("->", "  ")
            .replace("==>", "   ")
            .replace("==>>", "    ")
            .replace("->>", "   ")
            .replace("-->>", "    ")
            .replace("-x", "  ")
            .replace("--x", "   ")
            .replace("-.->", "    ")
            .replace("-.->>", "     ")
            .replace("=x", "  ")
            .replace("==x", "   ")
            .replace("~~>", "   ")
            .replace("~>>", "   ")
            .replace("<-->", "    ")

        for (char in codeWithoutArrows) {
            when {
                brackets.containsKey(char) -> stack.add(char)
                brackets.containsValue(char) -> {
                    if (stack.isEmpty()) {
                        throw InvalidMermaidException("Unmatched closing bracket: $char")
                    }
                    val last = stack.removeLastOrNull()
                    if (brackets[last] != char) {
                        throw InvalidMermaidException("Mismatched brackets: $last and $char")
                    }
                }
            }
        }

        if (stack.isNotEmpty()) {
            throw InvalidMermaidException("Unmatched opening brackets: ${stack.joinToString()}")
        }
    }

    /**
     * Validates arrow syntax in Mermaid diagrams
     */
    private fun validateArrows(code: String) {
        // Common arrow patterns in Mermaid
        val validArrowPatterns = listOf(
            "-->", "->", "->>", "-->>", "-x", "--x", "-.->", "-.->>",
            "==>", "==>>", "=x", "==x", "~~>", "~>>", "o--o", "<-->",
            "||--||", "||--o{", "}o--||", "}o--o{", "||..|{", "}|..|{"
        )

        // Look for potential arrow typos
        val lines = code.lines()
        for ((index, line) in lines.withIndex()) {
            val lineNumber = index + 1

            // Check for common arrow typos
            if (line.contains("---") && !validArrowPatterns.any { line.contains(it) }) {

                continue // Warning, not error
            }

            // Check for isolated arrows (arrows not connecting anything)
            val arrowPattern = Pattern.compile("\\s(-->|->|==>)\\s*$")
            if (arrowPattern.matcher(line).find()) {
                throw InvalidMermaidException("Line $lineNumber: Arrow without proper connection")
            }
        }
    }

    /**
     * Validates characters that might break Mermaid rendering
     */
    private fun validateCharacters(code: String) {
        // Characters that often cause issues in Mermaid
        val problematicChars = listOf(
            '`', // Backticks can break syntax
            '\\', // Backslashes need escaping
            '"' // Unescaped quotes in wrong context
        )

        for (char in problematicChars) {
            if (code.contains(char)) {
                // Check if it's in a valid context (like quoted strings)
                val inQuotes = isCharacterInQuotes(code, char)
                if (!inQuotes && char != '"') {
                    throw InvalidMermaidException("Potentially problematic character found: $char")
                }
            }
        }
    }

    /**
     * Checks if a character appears in a quoted context
     */
    private fun isCharacterInQuotes(code: String, char: Char): Boolean {
        val lines = code.lines()
        for (line in lines) {
            if (line.contains(char)) {
                // Simple check: if line has quotes and the char, assume it's quoted
                if (line.contains("\"") || line.contains("'")) {
                    continue // Assume it's properly quoted
                }
                return false
            }
        }
        return true
    }

    /**
     * Removes common AI response prefixes and suffixes
     */
    private fun removeCommonPrefixes(text: String): String {
        val prefixesToRemove = listOf(
            "Here's the Mermaid diagram:",
            "Here is the Mermaid code:",
            "The Mermaid diagram is:",
            "```mermaid",
            "```",
            "Response:",
            "Mermaid code:"
        )

        val suffixesToRemove = listOf(
            "```",
            "Hope this helps!",
            "Let me know if you need any changes.",
            "This diagram shows"
        )

        var cleaned = text

        // Remove prefixes
        for (prefix in prefixesToRemove) {
            if (cleaned.startsWith(prefix, ignoreCase = true)) {
                cleaned = cleaned.substring(prefix.length).trim()
            }
        }

        // Remove suffixes
        for (suffix in suffixesToRemove) {
            if (cleaned.endsWith(suffix, ignoreCase = true)) {
                cleaned = cleaned.substring(0, cleaned.length - suffix.length).trim()
            }
        }

        return cleaned
    }

    /**
     * Finds the index of the first line that starts a valid Mermaid diagram
     */
    private fun findDiagramStart(lines: List<String>): Int {
        val validStarts = listOf(
            "graph", "flowchart", "sequenceDiagram", "classDiagram",
            "stateDiagram", "erDiagram", "gantt", "pie", "gitGraph",
            "journey", "quadrantChart", "requirementDiagram", "c4Context"
        )

        for ((index, line) in lines.withIndex()) {
            val trimmedLine = line.trim().lowercase()
            for (start in validStarts) {
                if (trimmedLine.startsWith(start.lowercase())) {
                    return index
                }
            }
        }

        return -1
    }
}

/**
 * Exception thrown when Mermaid code is invalid or cannot be processed
 */
class InvalidMermaidException(message: String) : Exception(message)