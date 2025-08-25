package com.example.mermaidmaker.utils

import android.util.Log

/**
 * Utility class to test API key validation patterns
 */
object ApiKeyTester {
    private const val TAG = "ApiKeyTester"
    

    private val OPENAI_KEY_PATTERN = Regex("^sk-.{20,}$")
    private val GEMINI_KEY_PATTERN = Regex("^.{20,}$")
    
    fun testOpenAiKey(key: String): ValidationResult {
        val sanitized = key.trim()
        Log.d(TAG, "Testing OpenAI key:")
        Log.d(TAG, "  Length: ${sanitized.length}")
        Log.d(TAG, "  First 10 chars: ${sanitized.take(10)}")
        Log.d(TAG, "  Starts with 'sk-': ${sanitized.startsWith("sk-")}")
        
        if (sanitized.startsWith("sk-")) {
            val afterSk = sanitized.substring(3)
            Log.d(TAG, "  After 'sk-' length: ${afterSk.length}")
            Log.d(TAG, "  After 'sk-' chars valid: ${afterSk.all { it.isLetterOrDigit() || it == '_' || it == '-' }}")
            
            // Check for any invalid characters
            val invalidChars = afterSk.filter { !(it.isLetterOrDigit() || it == '_' || it == '-') }
            if (invalidChars.isNotEmpty()) {
                Log.d(TAG, "  Invalid characters found: ${invalidChars.toSet()}")
            }
        }
        
        val matches = OPENAI_KEY_PATTERN.matches(sanitized)
        Log.d(TAG, "  Regex matches: $matches")
        
        return ValidationResult(
            isValid = matches,
            provider = "OpenAI",
            length = sanitized.length,
            format = sanitized.take(10) + "...",
            details = if (matches) "Valid" else "Invalid format"
        )
    }
    
    fun testGeminiKey(key: String): ValidationResult {
        val sanitized = key.trim()
        Log.d(TAG, "Testing Gemini key:")
        Log.d(TAG, "  Length: ${sanitized.length}")
        Log.d(TAG, "  First 10 chars: ${sanitized.take(10)}")
        Log.d(TAG, "  All chars valid: ${sanitized.all { it.isLetterOrDigit() || it == '_' || it == '-' }}")
        
        // Check for any invalid characters
        val invalidChars = sanitized.filter { !(it.isLetterOrDigit() || it == '_' || it == '-') }
        if (invalidChars.isNotEmpty()) {
            Log.d(TAG, "  Invalid characters found: ${invalidChars.toSet()}")
        }
        
        val matches = GEMINI_KEY_PATTERN.matches(sanitized)
        Log.d(TAG, "  Regex matches: $matches")
        
        return ValidationResult(
            isValid = matches,
            provider = "Gemini",
            length = sanitized.length,
            format = sanitized.take(10) + "...",
            details = if (matches) "Valid" else "Invalid format"
        )
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val provider: String,
        val length: Int,
        val format: String,
        val details: String
    )
}
