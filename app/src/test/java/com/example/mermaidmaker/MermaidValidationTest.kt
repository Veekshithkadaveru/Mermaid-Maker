package com.example.mermaidmaker

import com.example.mermaidmaker.ui.editor.validation.MermaidSyntaxValidator
import com.example.mermaidmaker.ui.editor.validation.ValidationSeverity
import org.junit.Test
import org.junit.Assert.*

class MermaidValidationTest {
    
    private val validator = MermaidSyntaxValidator()
    
    @Test
    fun `valid flowchart with edge labels should not have bracket errors`() {
        val validFlowchart = """
            graph TD
                A[Start] --> B[Process]
                B --> C{Decision}
                C -->|Yes| D[Action A]
                C -->|No| E[Action B]
                D --> F[End]
                E --> F
        """.trimIndent()
        
        val result = validator.validate(validFlowchart)
        
        // Should not have any bracket-related errors
        val bracketErrors = result.errors.filter { 
            it.type.category == "Syntax" && 
            it.message.contains("bracket", ignoreCase = true) 
        }
        
        assertTrue("Valid Mermaid syntax should not have bracket errors. Found: ${bracketErrors.map { it.message }}", 
                  bracketErrors.isEmpty())
    }
    
    @Test
    fun `should detect actual bracket errors`() {
        val invalidBrackets = """
            graph TD
                A[Unclosed bracket --> B
                C{Missing close --> D]
        """.trimIndent()
        
        val result = validator.validate(invalidBrackets)
        
        // Should have bracket errors for genuinely unbalanced brackets
        val bracketErrors = result.errors.filter { 
            it.type.category == "Syntax" && 
            it.message.contains("bracket", ignoreCase = true) 
        }
        
        assertTrue("Should detect actual bracket errors", bracketErrors.isNotEmpty())
    }
    
    @Test
    fun `should handle edge labels correctly`() {
        val edgeLabels = """
            graph TD
                A --> B
                B -->|Success| C
                B -->|Failure| D
                C -->|Retry| B
        """.trimIndent()
        
        val result = validator.validate(edgeLabels)
        
        // Should not flag |Success|, |Failure|, |Retry| as bracket errors
        val bracketErrors = result.errors.filter { 
            it.message.contains("bracket", ignoreCase = true) 
        }
        
        assertTrue("Edge labels should not be flagged as bracket errors. Found: ${bracketErrors.map { it.message }}", 
                  bracketErrors.isEmpty())
    }
    
    @Test
    fun `should validate sequence diagram arrows correctly`() {
        val sequenceDiagram = """
            sequenceDiagram
                participant User
                participant System
                participant Database
                
                User->>System: Request data
                System->>Database: Query
                Database-->>System: Result
                System-->>User: Response
        """.trimIndent()
        
        val result = validator.validate(sequenceDiagram)
        
        // Should not flag ->> and -->> as invalid arrow syntax
        val arrowErrors = result.errors.filter { 
            it.type.category == "Arrow" && 
            it.message.contains("arrow", ignoreCase = true) 
        }
        
        assertTrue("Valid sequence diagram arrows should not be flagged as invalid. Found: ${arrowErrors.map { it.message }}", 
                  arrowErrors.isEmpty())
    }
}