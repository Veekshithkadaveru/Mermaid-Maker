package com.example.mermaidmaker.ui.editor.validation

import com.example.mermaidmaker.domain.model.DiagramType
import java.util.regex.Pattern

/**
 * Professional Mermaid syntax validator with comprehensive error detection
 */
class MermaidSyntaxValidator {
    
    companion object {
        // Diagram type patterns
        private val DIAGRAM_TYPES = mapOf(
            "graph" to listOf("TD", "TB", "BT", "RL", "LR"),
            "flowchart" to listOf("TD", "TB", "BT", "RL", "LR"),
            "sequenceDiagram" to emptyList(),
            "classDiagram" to emptyList(),
            "stateDiagram" to emptyList(),
            "stateDiagram-v2" to emptyList(),
            "erDiagram" to emptyList(),
            "journey" to emptyList(),
            "gantt" to emptyList(),
            "pie" to emptyList(),
            "gitgraph" to emptyList()
        )
        
        // Node shape patterns
        private val NODE_SHAPES = listOf(
            "\\[.*?\\]", // Rectangle
            "\\(.*?\\)", // Round edges
            "\\{.*?\\}", // Rhombus
            "\\[\\[.*?\\]\\]", // Subroutine
            "\\[\\(.*?\\)\\]", // Cylinder
            "\\(\\(.*?\\)\\)", // Circle
            "\\>.*?\\]", // Asymmetric
            "\\[.*?\\>", // Asymmetric reverse
            "\\{\\{.*?\\}\\}", // Hexagon
            "\\[\\|.*?\\|\\]" // Double rectangle
        )
        
        // Arrow patterns for different diagram types
        private val FLOWCHART_ARROWS = listOf(
            "-->", "---", "-.->", "-.-", "==>", "===", "--.", "-.)"
        )
        
        private val SEQUENCE_ARROWS = listOf(
            "->>", "-->>", "->", "-)", "--)", 
            "-x", "--x", "->>+", "-->>+"
        )
        
        private val ALL_ARROW_PATTERNS = (FLOWCHART_ARROWS + SEQUENCE_ARROWS).distinct()
        
        // Common Mermaid keywords
        private val KEYWORDS = setOf(
            "title", "participant", "note", "class", "state", "dateFormat",
            "section", "activate", "deactivate", "loop", "alt", "else",
            "opt", "par", "and", "critical", "break", "commit", "branch",
            "checkout", "merge"
        )
    }
    
    /**
     * Validate Mermaid diagram content and return detailed errors
     */
    fun validate(content: String): ValidationResult {
        if (content.isBlank()) {
            return ValidationResult()
        }
        
        val errors = mutableListOf<MermaidValidationError>()
        val lines = content.split('\n')
        
        // 1. Check diagram type
        errors.addAll(validateDiagramType(lines))
        
        // 2. Validate syntax for each line
        lines.forEachIndexed { index, line ->
            errors.addAll(validateLine(line, index))
        }
        
        // 3. Validate overall structure
        errors.addAll(validateStructure(lines))
        
        // 4. Best practice checks
        errors.addAll(validateBestPractices(lines))
        
        return ValidationResult(errors.sortedBy { it.line })
    }
    
    /**
     * Validate diagram type declaration
     */
    private fun validateDiagramType(lines: List<String>): List<MermaidValidationError> {
        val errors = mutableListOf<MermaidValidationError>()
        
        val firstNonEmptyIndex = lines.indexOfFirst { it.isNotBlank() && !it.trim().startsWith("%%") }
        
        if (firstNonEmptyIndex == -1) {
            return errors
        }
        
        val firstLine = lines[firstNonEmptyIndex].trim()
        val diagramTypeFound = DIAGRAM_TYPES.keys.any { firstLine.startsWith(it) }
        
        if (!diagramTypeFound) {
            errors.add(
                MermaidValidationError(
                    type = ValidationErrorType.MISSING_DIAGRAM_TYPE,
                    line = firstNonEmptyIndex,
                    message = "Missing diagram type declaration",
                    suggestion = "Start with a diagram type like 'graph TD', 'sequenceDiagram', 'classDiagram', etc.",
                    quickFix = "Add 'graph TD' at the beginning",
                    documentationUrl = "https://mermaid.js.org/syntax/"
                )
            )
        } else {
            // Validate specific diagram type syntax
            errors.addAll(validateSpecificDiagramType(firstLine, firstNonEmptyIndex))
        }
        
        return errors
    }
    
    /**
     * Validate specific diagram type syntax
     */
    private fun validateSpecificDiagramType(line: String, lineIndex: Int): List<MermaidValidationError> {
        val errors = mutableListOf<MermaidValidationError>()
        
        when {
            line.startsWith("graph") || line.startsWith("flowchart") -> {
                val parts = line.split("\\s+".toRegex())
                if (parts.size > 1) {
                    val direction = parts[1]
                    val validDirections = DIAGRAM_TYPES["graph"] ?: emptyList()
                    if (direction !in validDirections) {
                        errors.add(
                            MermaidValidationError(
                                type = ValidationErrorType.INVALID_DIAGRAM_TYPE,
                                line = lineIndex,
                                column = line.indexOf(direction),
                                length = direction.length,
                                message = "Invalid graph direction: '$direction'",
                                suggestion = "Use one of: ${validDirections.joinToString(", ")}",
                                quickFix = "Replace with 'TD' (Top Down)"
                            )
                        )
                    }
                }
            }
            line.startsWith("pie") -> {
                if (!line.contains("title")) {
                    errors.add(
                        MermaidValidationError(
                            type = ValidationErrorType.MISSING_TITLE,
                            line = lineIndex,
                            severity = ValidationSeverity.WARNING,
                            message = "Pie chart should have a title",
                            suggestion = "Add 'title Your Chart Title' after 'pie'",
                            quickFix = "pie title Chart Title"
                        )
                    )
                }
            }
        }
        
        return errors
    }
    
    /**
     * Validate individual line syntax
     */
    private fun validateLine(line: String, lineIndex: Int): List<MermaidValidationError> {
        val errors = mutableListOf<MermaidValidationError>()
        val trimmed = line.trim()
        
        // Skip empty lines and comments
        if (trimmed.isEmpty() || trimmed.startsWith("%%")) {
            return errors
        }
        
        // Check bracket balance
        errors.addAll(validateBrackets(line, lineIndex))
        
        // Check node syntax
        errors.addAll(validateNodeSyntax(line, lineIndex))
        
        // Check arrow syntax
        errors.addAll(validateArrowSyntax(line, lineIndex))
        
        // Check for common syntax errors
        errors.addAll(validateCommonSyntaxErrors(line, lineIndex))
        
        return errors
    }
    
    /**
     * Validate bracket balance and proper nesting (Mermaid-aware)
     */
    private fun validateBrackets(line: String, lineIndex: Int): List<MermaidValidationError> {
        val errors = mutableListOf<MermaidValidationError>()
        val brackets = mapOf('(' to ')', '[' to ']', '{' to '}')
        val stack = ArrayDeque<Pair<Char, Int>>()
        
        // Handle Mermaid-specific patterns that should be ignored
        var processedLine = line
        
        // Remove edge labels like |Yes|, |No|, etc.
        processedLine = processedLine.replace("\\|[^|]*\\|".toRegex(), "")
        
        // Remove strings in quotes to avoid false positives
        processedLine = processedLine.replace("\"[^\"]*\"".toRegex(), "")
        processedLine = processedLine.replace("'[^']*'".toRegex(), "")
        
        // Process character by character
        processedLine.forEachIndexed { index, char ->
            when {
                char in brackets.keys -> {
                    stack.addLast(char to index)
                }
                char in brackets.values -> {
                    if (stack.isEmpty()) {
                        // Only flag as error if it's not part of a valid Mermaid pattern
                        if (!isValidMermaidBracketPattern(line, index, char)) {
                            errors.add(
                                MermaidValidationError(
                                    type = ValidationErrorType.UNBALANCED_BRACKETS,
                                    line = lineIndex,
                                    column = index,
                                    message = "Unmatched closing bracket '$char'",
                                    suggestion = "Add opening bracket or remove this closing bracket"
                                )
                            )
                        }
                    } else {
                        val (lastOpen, _) = stack.removeLast()
                        if (brackets[lastOpen] != char) {
                            errors.add(
                                MermaidValidationError(
                                    type = ValidationErrorType.UNBALANCED_BRACKETS,
                                    line = lineIndex,
                                    column = index,
                                    message = "Mismatched bracket: expected '${brackets[lastOpen]}' but found '$char'",
                                    suggestion = "Use '${brackets[lastOpen]}' instead of '$char'"
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // Check for unclosed brackets
        if (stack.isNotEmpty()) {
            val (unclosedChar, unclosedPos) = stack.last()
            errors.add(
                MermaidValidationError(
                    type = ValidationErrorType.UNCLOSED_BRACKETS,
                    line = lineIndex,
                    column = unclosedPos,
                    message = "Unclosed bracket '$unclosedChar'",
                    suggestion = "Add closing bracket '${brackets[unclosedChar]}'"
                )
            )
        }
        
        return errors
    }
    
    /**
     * Check if a bracket is part of a valid Mermaid pattern
     */
    private fun isValidMermaidBracketPattern(line: String, index: Int, char: Char): Boolean {
        // Check if this bracket is part of a valid Mermaid node shape or pattern
        val context = line.substring(maxOf(0, index - 10), minOf(line.length, index + 10))
        
        // Common valid patterns where brackets might appear standalone
        val validPatterns = listOf(
            "-->", // Arrow patterns
            "--.", 
            "-.->",
            "==>",
            // Add more patterns as needed
        )
        
        return validPatterns.any { pattern -> context.contains(pattern) }
    }
    
    /**
     * Validate node syntax and shapes
     */
    private fun validateNodeSyntax(line: String, lineIndex: Int): List<MermaidValidationError> {
        val errors = mutableListOf<MermaidValidationError>()
        
        // Check for valid node shapes
        val nodeShapeRegex = NODE_SHAPES.joinToString("|").toRegex()
        
        // Find potential nodes (text within brackets)
        val bracketContent = "\\[[^\\]]*\\]|\\([^\\)]*\\)|\\{[^\\}]*\\}".toRegex()
        bracketContent.findAll(line).forEach { match ->
            val nodeText = match.value
            if (!nodeShapeRegex.containsMatchIn(nodeText)) {
                // Check if it's a malformed node
                if (nodeText.length > 2 && !nodeText.matches("\\w+".toRegex())) {
                    errors.add(
                        MermaidValidationError(
                            type = ValidationErrorType.INVALID_NODE_SHAPE,
                            line = lineIndex,
                            column = match.range.first,
                            length = nodeText.length,
                            message = "Invalid node shape syntax: '$nodeText'",
                            suggestion = "Use proper node syntax like [Label], (Label), or {Label}"
                        )
                    )
                }
            }
            
            // Check for very long labels
            val content = nodeText.removeSurrounding("[", "]")
                .removeSurrounding("(", ")")
                .removeSurrounding("{", "}")
            
            if (content.length > 50) {
                errors.add(
                    MermaidValidationError(
                        type = ValidationErrorType.LONG_LABEL,
                        line = lineIndex,
                        column = match.range.first,
                        length = nodeText.length,
                        severity = ValidationSeverity.WARNING,
                        message = "Node label is very long (${content.length} characters)",
                        suggestion = "Consider shortening the label or using line breaks"
                    )
                )
            }
        }
        
        return errors
    }
    
    /**
     * Validate arrow syntax (context-aware for different diagram types)
     */
    private fun validateArrowSyntax(line: String, lineIndex: Int): List<MermaidValidationError> {
        val errors = mutableListOf<MermaidValidationError>()
        
        // Find arrow-like patterns that might be incorrect
        val potentialArrows = "[-=.x]{1,3}[>)x+]".toRegex()
        potentialArrows.findAll(line).forEach { match ->
            val arrow = match.value
            if (arrow !in ALL_ARROW_PATTERNS) {
                errors.add(
                    MermaidValidationError(
                        type = ValidationErrorType.INVALID_ARROW_SYNTAX,
                        line = lineIndex,
                        column = match.range.first,
                        length = arrow.length,
                        message = "Invalid arrow syntax: '$arrow'",
                        suggestion = "Use valid arrow syntax like '-->', '->>',  '==>', etc."
                    )
                )
            }
        }
        
        return errors
    }
    
    /**
     * Check for common syntax errors
     */
    private fun validateCommonSyntaxErrors(line: String, lineIndex: Int): List<MermaidValidationError> {
        val errors = mutableListOf<MermaidValidationError>()
        
        // Check for invalid characters in node IDs
        val nodeIdRegex = "\\b([A-Za-z]\\w*)\\s*[\\[\\(\\{]".toRegex()
        nodeIdRegex.findAll(line).forEach { match ->
            val nodeId = match.groupValues[1]
            if (nodeId.contains("-") || nodeId.contains(".")) {
                errors.add(
                    MermaidValidationError(
                        type = ValidationErrorType.INVALID_CHARACTER,
                        line = lineIndex,
                        column = match.range.first,
                        length = nodeId.length,
                        message = "Node ID '$nodeId' contains invalid characters",
                        suggestion = "Use only letters, numbers, and underscores in node IDs"
                    )
                )
            }
        }
        
        // Check for missing spaces around arrows
        val spacingIssues = "(\\w)(-->)(\\w)".toRegex()
        spacingIssues.findAll(line).forEach { match ->
            errors.add(
                MermaidValidationError(
                    type = ValidationErrorType.MALFORMED_STATEMENT,
                    line = lineIndex,
                    column = match.range.first,
                    length = match.value.length,
                    severity = ValidationSeverity.INFO,
                    message = "Consider adding spaces around arrow for better readability",
                    suggestion = "Use 'A --> B' instead of 'A-->B'"
                )
            )
        }
        
        return errors
    }
    
    /**
     * Validate overall diagram structure
     */
    private fun validateStructure(lines: List<String>): List<MermaidValidationError> {
        val errors = mutableListOf<MermaidValidationError>()
        
        val nodes = mutableSetOf<String>()
        val connections = mutableListOf<Pair<String, String>>()
        
        // Extract nodes and connections
        lines.forEachIndexed { lineIndex, line ->
            val trimmed = line.trim()
            if (trimmed.isNotEmpty() && !trimmed.startsWith("%%")) {
                // Extract node definitions and connections
                extractNodesAndConnections(trimmed, nodes, connections)
            }
        }
        
        // Check for orphaned nodes (nodes with no connections)
        val connectedNodes = connections.flatMap { listOf(it.first, it.second) }.toSet()
        val orphanedNodes = nodes - connectedNodes
        
        if (orphanedNodes.isNotEmpty() && nodes.size > 1) {
            errors.add(
                MermaidValidationError(
                    type = ValidationErrorType.ORPHANED_NODE,
                    line = 0,
                    severity = ValidationSeverity.WARNING,
                    message = "Found ${orphanedNodes.size} orphaned node(s): ${orphanedNodes.take(3).joinToString(", ")}",
                    suggestion = "Connect these nodes to the main diagram or remove them"
                )
            )
        }
        
        return errors
    }
    
    /**
     * Extract nodes and connections from a line
     */
    private fun extractNodesAndConnections(
        line: String,
        nodes: MutableSet<String>,
        connections: MutableList<Pair<String, String>>
    ) {
        // Simple extraction - in a real implementation, this would be more sophisticated
        val arrowPattern = ALL_ARROW_PATTERNS.joinToString("|") { java.util.regex.Pattern.quote(it) }.toRegex()
        val parts = line.split(arrowPattern)
        
        if (parts.size >= 2) {
            val from = parts[0].trim().split("\\s+".toRegex()).lastOrNull()?.let { extractNodeId(it) }
            val to = parts[1].trim().split("\\s+".toRegex()).firstOrNull()?.let { extractNodeId(it) }
            
            from?.let { nodes.add(it) }
            to?.let { nodes.add(it) }
            
            if (from != null && to != null) {
                connections.add(from to to)
            }
        } else {
            // Check for standalone node definitions
            val nodePattern = "\\b([A-Za-z]\\w*)\\s*[\\[\\(\\{]".toRegex()
            nodePattern.findAll(line).forEach { match ->
                nodes.add(match.groupValues[1])
            }
        }
    }
    
    /**
     * Extract node ID from text
     */
    private fun extractNodeId(text: String): String? {
        return "([A-Za-z]\\w*)".toRegex().find(text)?.value
    }
    
    /**
     * Validate best practices
     */
    private fun validateBestPractices(lines: List<String>): List<MermaidValidationError> {
        val errors = mutableListOf<MermaidValidationError>()
        
        val nonEmptyLines = lines.filter { it.isNotBlank() && !it.trim().startsWith("%%") }
        
        // Check for overly complex diagrams
        if (nonEmptyLines.size > 50) {
            errors.add(
                MermaidValidationError(
                    type = ValidationErrorType.COMPLEX_DIAGRAM,
                    line = 0,
                    severity = ValidationSeverity.INFO,
                    message = "Diagram is quite complex (${nonEmptyLines.size} lines)",
                    suggestion = "Consider breaking this into smaller, focused diagrams"
                )
            )
        }
        
        // Check for too many nodes
        val nodeCount = nonEmptyLines.count { line ->
            "\\b[A-Za-z]\\w*\\s*[\\[\\(\\{]".toRegex().containsMatchIn(line)
        }
        
        if (nodeCount > 20) {
            errors.add(
                MermaidValidationError(
                    type = ValidationErrorType.TOO_MANY_NODES,
                    line = 0,
                    severity = ValidationSeverity.INFO,
                    message = "Diagram has many nodes ($nodeCount)",
                    suggestion = "Consider grouping related nodes or splitting the diagram"
                )
            )
        }
        
        return errors
    }
}