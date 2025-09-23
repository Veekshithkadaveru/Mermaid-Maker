package com.example.mermaidmaker.ui.editor.validation

import androidx.compose.ui.graphics.Color

/**
 * Represents different types of validation errors with severity levels
 */
enum class ValidationSeverity(
    val displayName: String,
    val color: Color,
    val priority: Int
) {
    ERROR("Error", Color(0xFFD32F2F), 3),
    WARNING("Warning", Color(0xFFF57C00), 2),
    INFO("Info", Color(0xFF1976D2), 1)
}

/**
 * Represents different categories of Mermaid validation errors
 */
enum class ValidationErrorType(
    val category: String,
    val defaultSeverity: ValidationSeverity
) {
    // Syntax errors
    MISSING_DIAGRAM_TYPE("Syntax", ValidationSeverity.ERROR),
    INVALID_DIAGRAM_TYPE("Syntax", ValidationSeverity.ERROR),
    UNBALANCED_BRACKETS("Syntax", ValidationSeverity.ERROR),
    UNCLOSED_BRACKETS("Syntax", ValidationSeverity.ERROR),
    INVALID_CHARACTER("Syntax", ValidationSeverity.ERROR),
    MALFORMED_STATEMENT("Syntax", ValidationSeverity.ERROR),
    
    // Structure errors
    MISSING_NODES("Structure", ValidationSeverity.ERROR),
    DUPLICATE_NODE_ID("Structure", ValidationSeverity.WARNING),
    ORPHANED_NODE("Structure", ValidationSeverity.WARNING),
    INVALID_CONNECTION("Structure", ValidationSeverity.ERROR),
    
    // Diagram-specific errors
    INVALID_ARROW_SYNTAX("Arrow", ValidationSeverity.ERROR),
    INVALID_NODE_SHAPE("Node", ValidationSeverity.ERROR),
    INVALID_LABEL_SYNTAX("Label", ValidationSeverity.ERROR),
    MISSING_PARTICIPANT("Sequence", ValidationSeverity.ERROR),
    
    // Style and formatting
    LONG_LABEL("Style", ValidationSeverity.WARNING),
    INCONSISTENT_NAMING("Style", ValidationSeverity.INFO),
    DEPRECATED_SYNTAX("Style", ValidationSeverity.WARNING),
    
    // Best practices
    TOO_MANY_NODES("Best Practice", ValidationSeverity.INFO),
    COMPLEX_DIAGRAM("Best Practice", ValidationSeverity.INFO),
    MISSING_TITLE("Best Practice", ValidationSeverity.INFO)
}

/**
 * Comprehensive validation error with detailed information
 */
data class MermaidValidationError(
    val type: ValidationErrorType,
    val severity: ValidationSeverity = type.defaultSeverity,
    val line: Int,
    val column: Int = 0,
    val length: Int = 1,
    val message: String,
    val suggestion: String? = null,
    val quickFix: String? = null,
    val documentationUrl: String? = null
) {
    /**
     * Get a user-friendly title for the error
     */
    val title: String
        get() = "${severity.displayName}: ${type.category}"
    
    /**
     * Get the full error description including suggestion
     */
    val fullDescription: String
        get() = buildString {
            append(message)
            suggestion?.let {
                append("\n\nSuggestion: $it")
            }
            quickFix?.let {
                append("\n\nQuick fix: $it")
            }
        }
    
    /**
     * Get a short summary for display in compact spaces
     */
    val summary: String
        get() = "${type.category}: $message"
}

/**
 * Container for validation results
 */
data class ValidationResult(
    val errors: List<MermaidValidationError> = emptyList(),
    val isValid: Boolean = errors.none { it.severity == ValidationSeverity.ERROR }
) {
    val errorCount: Int = errors.count { it.severity == ValidationSeverity.ERROR }
    val warningCount: Int = errors.count { it.severity == ValidationSeverity.WARNING }
    val infoCount: Int = errors.count { it.severity == ValidationSeverity.INFO }
    val totalCount: Int = errors.size
    
    /**
     * Get errors for a specific line
     */
    fun getErrorsForLine(line: Int): List<MermaidValidationError> {
        return errors.filter { it.line == line }
    }
    
    /**
     * Get the most severe error for a line
     */
    fun getMostSevereErrorForLine(line: Int): MermaidValidationError? {
        return getErrorsForLine(line).maxByOrNull { it.severity.priority }
    }
    
    /**
     * Get summary text for display
     */
    fun getSummaryText(): String {
        return when {
            totalCount == 0 -> "No issues"
            errorCount > 0 -> "$errorCount error${if (errorCount != 1) "s" else ""}" +
                    if (warningCount > 0) ", $warningCount warning${if (warningCount != 1) "s" else ""}" else ""
            warningCount > 0 -> "$warningCount warning${if (warningCount != 1) "s" else ""}" +
                    if (infoCount > 0) ", $infoCount info" else ""
            else -> "$infoCount info"
        }
    }
}