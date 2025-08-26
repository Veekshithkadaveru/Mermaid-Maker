package com.example.mermaidmaker.data.service

import android.util.Log
import com.example.mermaidmaker.data.network.GeminiApiService
import com.example.mermaidmaker.data.network.OpenAiApiService
import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.service.ApiKeyValidationService
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Implementation of API key validation service with actual server-side validation
 */
class ApiKeyValidationServiceImpl(
    private val openAiApiService: OpenAiApiService,
    private val geminiApiService: GeminiApiService
) : ApiKeyValidationService {
    
    init {
        Log.d(TAG, "ApiKeyValidationServiceImpl initialized with server validation support")
    }
    
    companion object {
        private const val TAG = "ApiKeyValidation"
        
        // Basic API key format validation patterns (relaxed to support newer formats)
        // OpenAI keys now include formats like sk-proj-..., sk-live-..., etc.
        // Updated pattern to be more flexible for various OpenAI key formats
        // Supports sk-, sk-proj-, sk-live-, and other sk-* prefixes
        private val OPENAI_KEY_PATTERN = Regex("^sk-[a-zA-Z0-9][a-zA-Z0-9-_]{15,}$")
        // Gemini keys vary; accept 20+ URL-safe characters (alphanumeric, dash, underscore)
        private val GEMINI_KEY_PATTERN = Regex("^[A-Za-z0-9_-]{20,}$")
    }
    
    override suspend fun validateApiKey(provider: AiProvider, apiKey: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Starting validation for ${provider.displayName} with server validation")
            
            // Basic format validation first
            val sanitizedKey = apiKey.trim()
            val isValidFormat = when (provider) {
                AiProvider.OPENAI -> OPENAI_KEY_PATTERN.matches(sanitizedKey)
                AiProvider.GEMINI -> GEMINI_KEY_PATTERN.matches(sanitizedKey)
            }
            
            if (!isValidFormat) {
                Log.w(TAG, "API key format validation failed for ${provider.displayName}. Key length: ${sanitizedKey.length}")
                val hint = getApiKeyFormatHint(provider)
                return Result.failure(Exception("Invalid API key format for ${provider.displayName}. $hint"))
            }
            
            // Perform actual server-side validation
            Log.d(TAG, "Format check passed, proceeding with server validation for ${provider.displayName}")
            val isValid = when (provider) {
                AiProvider.OPENAI -> {
                    Log.d(TAG, "Calling OpenAI validation...")
                    validateOpenAiKey(sanitizedKey)
                }
                AiProvider.GEMINI -> {
                    Log.d(TAG, "Calling Gemini validation...")
                    validateGeminiKey(sanitizedKey)
                }
            }
            
            Log.d(TAG, "Server validation ${if (isValid) "succeeded" else "failed"} for ${provider.displayName}")
            Result.success(isValid)
            
        } catch (e: Exception) {
            Log.e(TAG, "API key validation error for ${provider.displayName}", e)
            Result.failure(e)
        }
    }
    
    private suspend fun validateOpenAiKey(apiKey: String): Boolean {
        return try {
            Log.d(TAG, "Making HTTP request to OpenAI API...")
            val response = openAiApiService.validateApiKey("Bearer $apiKey")
            val isSuccessful = response.isSuccessful
            
            Log.d(TAG, "OpenAI response received - Code: ${response.code()}, Success: $isSuccessful")
            if (isSuccessful) {
                Log.d(TAG, "OpenAI API key validation successful")
                true
            } else {
                Log.w(TAG, "OpenAI API key validation failed with code: ${response.code()}")
                false
            }
        } catch (e: HttpException) {
            Log.w(TAG, "OpenAI validation failed with HTTP error: ${e.code()}", e)
            when (e.code()) {
                401 -> false // Unauthorized - invalid key
                403 -> false // Forbidden - invalid key or insufficient permissions
                else -> throw e // Other HTTP errors should bubble up
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error during OpenAI validation", e)
            throw Exception("Network error: ${e.message}")
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout during OpenAI validation", e)
            throw Exception("Request timeout. Please check your internet connection.")
        }
    }
    
    private suspend fun validateGeminiKey(apiKey: String): Boolean {
        return try {
            Log.d(TAG, "Making HTTP request to Gemini API...")
            val response = geminiApiService.validateApiKey(apiKey)
            val isSuccessful = response.isSuccessful
            
            Log.d(TAG, "Gemini response received - Code: ${response.code()}, Success: $isSuccessful")
            if (isSuccessful) {
                Log.d(TAG, "Gemini API key validation successful")
                true
            } else {
                Log.w(TAG, "Gemini API key validation failed with code: ${response.code()}")
                false
            }
        } catch (e: HttpException) {
            Log.w(TAG, "Gemini validation failed with HTTP error: ${e.code()}", e)
            when (e.code()) {
                400 -> false // Bad request - often invalid key
                401 -> false // Unauthorized - invalid key
                403 -> false // Forbidden - invalid key or insufficient permissions
                else -> throw e // Other HTTP errors should bubble up
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error during Gemini validation", e)
            throw Exception("Network error: ${e.message}")
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout during Gemini validation", e)
            throw Exception("Request timeout. Please check your internet connection.")
        }
    }
    
    override suspend fun testConnection(provider: AiProvider, apiKey: String): Result<String> {
        return try {
            Log.d(TAG, "Testing connection for ${provider.displayName}")
            
            val sanitizedKey = apiKey.trim()
            
            // Test the actual connection
            val isValid = when (provider) {
                AiProvider.OPENAI -> validateOpenAiKey(sanitizedKey)
                AiProvider.GEMINI -> validateGeminiKey(sanitizedKey)
            }
            
            if (isValid) {
                val successMessage = "Connection test successful for ${provider.displayName}"
                Log.d(TAG, successMessage)
                Result.success(successMessage)
            } else {
                val errorMessage = "Connection test failed - API key is invalid for ${provider.displayName}"
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
            AiProvider.OPENAI -> "Should start with 'sk-' followed by letters/numbers/dashes (supports sk-proj-, sk-live-, etc.)"
            AiProvider.GEMINI -> "Should be 20+ characters: letters, numbers, dashes, or underscores only"
        }
    }
}