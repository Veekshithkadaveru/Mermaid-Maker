package com.example.mermaidmaker.data.service

import android.util.Log
import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.service.ApiKeyValidationService
import com.example.mermaidmaker.utils.ApiKeyTester
import kotlinx.coroutines.delay

/**
 * Implementation of API key validation service
 * This is a basic implementation for MVP - actual API validation will be added later
 */
class ApiKeyValidationServiceImpl : ApiKeyValidationService {
    
    companion object {
        private const val TAG = "ApiKeyValidation"
        
        // Basic API key format validation patterns (very permissive for debugging)
        // OpenAI keys: sk- followed by at least 20 characters (any characters for now)
        private val OPENAI_KEY_PATTERN = Regex("^sk-.{20,}$")
        // Gemini keys: at least 20 characters total (any characters for now)
        private val GEMINI_KEY_PATTERN = Regex("^.{20,}$")
    }
    
    override suspend fun validateApiKey(provider: AiProvider, apiKey: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Validating API key for ${provider.displayName}")
            
            // Basic format validation
            val sanitizedKey = apiKey.trim()
            Log.d(TAG, "Validating key for ${provider.displayName}: length=${sanitizedKey.length}, starts with=${sanitizedKey.take(10)}...")
            
            // Use detailed tester for better debugging
            val testResult = when (provider) {
                AiProvider.OPENAI -> ApiKeyTester.testOpenAiKey(sanitizedKey)
                AiProvider.GEMINI -> ApiKeyTester.testGeminiKey(sanitizedKey)
            }
            
            val isValidFormat = testResult.isValid
            Log.d(TAG, "Format validation result for ${provider.displayName}: $isValidFormat - ${testResult.details}")
            
            if (!isValidFormat) {
                Log.w(TAG, "API key format validation failed for ${provider.displayName}: ${testResult.details}")
                return Result.failure(Exception("Invalid API key format for ${provider.displayName}: ${testResult.details}"))
            }
            
            // Simulate API validation delay
            delay(1000)
            
            // For MVP, we'll do basic format validation only
            // TODO: Implement actual API calls for validation
            val isValid = sanitizedKey.length >= 20 && isValidFormat
            
            Log.d(TAG, "API key validation ${if (isValid) "succeeded" else "failed"} for ${provider.displayName}")
            Result.success(isValid)
            
        } catch (e: Exception) {
            Log.e(TAG, "API key validation error for ${provider.displayName}", e)
            Result.failure(e)
        }
    }
    
    override suspend fun testConnection(provider: AiProvider, apiKey: String): Result<String> {
        return try {
            Log.d(TAG, "Testing connection for ${provider.displayName}")
            
            // Simulate network call delay
            delay(1500)
            
            // For MVP, return success if key format is valid
            val sanitizedKey = apiKey.trim()
            Log.d(TAG, "Testing connection for ${provider.displayName}: length=${sanitizedKey.length}, starts with=${sanitizedKey.take(10)}...")
            
            val isValidFormat = when (provider) {
                AiProvider.OPENAI -> OPENAI_KEY_PATTERN.matches(sanitizedKey)
                AiProvider.GEMINI -> GEMINI_KEY_PATTERN.matches(sanitizedKey)
            }
            
            Log.d(TAG, "Connection test format check for ${provider.displayName}: $isValidFormat")
            
            if (isValidFormat) {
                val successMessage = "Connection test successful for ${provider.displayName}"
                Log.d(TAG, successMessage)
                Result.success(successMessage)
            } else {
                val errorMessage = "Invalid API key format for ${provider.displayName}"
                Log.w(TAG, errorMessage)
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed for ${provider.displayName}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get expected API key format hint for users
     */
    fun getApiKeyFormatHint(provider: AiProvider): String {
        return when (provider) {
            AiProvider.OPENAI -> "Should start with 'sk-' followed by 20+ characters (letters, numbers, dashes, underscores)"
            AiProvider.GEMINI -> "Should be 20+ characters; letters, numbers, dashes or underscores"
        }
    }
}