package com.example.mermaidmaker.data.service

import android.util.Log
import com.example.mermaidmaker.data.network.GeminiApiService
import com.example.mermaidmaker.data.network.OpenAiApiService
import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.service.ApiKeyValidationService
import com.example.mermaidmaker.domain.error.ApiError
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
        

        private val OPENAI_KEY_PATTERN = Regex("^sk-[a-zA-Z0-9][a-zA-Z0-9-_]{15,}$")

        private val GEMINI_KEY_PATTERN = Regex("^[\\x21-\\x7E]{10,}$")
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
            
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error during validation for ${provider.displayName}", e)
            Result.failure(ApiError.Http(e.code(), cause = e))
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout during validation for ${provider.displayName}", e)
            Result.failure(ApiError.Timeout())
        } catch (e: IOException) {
            Log.e(TAG, "Network error during validation for ${provider.displayName}", e)
            Result.failure(ApiError.Network("Network error: ${e.message}", e))
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid argument during validation for ${provider.displayName}", e)
            Result.failure(ApiError.InvalidKey("Invalid API key format"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during validation for ${provider.displayName}", e)
            Result.failure(ApiError.Unexpected("Validation error: ${e.message}", e))
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
                401, 403 -> false
                else -> throw ApiError.Http(e.code(), cause = e)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error during OpenAI validation", e)
            throw ApiError.Network("Network error: ${e.message}", e)
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout during OpenAI validation", e)
            throw ApiError.Timeout()
        }
    }
    
    private suspend fun validateGeminiKey(apiKey: String): Boolean {
        return try {
            Log.d(TAG, "Making HTTP request to Gemini API...")
            val response = geminiApiService.validateApiKey(apiKey)
            val isSuccessful = response.isSuccessful
            
            Log.d(TAG, "Gemini response received - Code: ${response.code()}, Success: $isSuccessful")
            
            if (isSuccessful) {
                // Verify the response contains valid data
                val body = response.body()
                Log.d(TAG, "Gemini response body: $body")
                
                if (body?.models != null) {
                    Log.d(TAG, "Gemini API key validation successful - found ${body.models.size} models")
                    true
                } else {
                    Log.d(TAG, "Gemini API key validation successful - response received but no models listed")
                    true
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.w(TAG, "Gemini API key validation failed with code: ${response.code()}, error: $errorBody")
                false
            }
        } catch (e: HttpException) {
            Log.w(TAG, "Gemini validation failed with HTTP error: ${e.code()}", e)
            when (e.code()) {
                400, 401, 403, 404 -> false // invalid key or not found
                else -> throw ApiError.Http(e.code(), cause = e)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Network error during Gemini validation", e)
            throw ApiError.Network("Network error: ${e.message}", e)
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout during Gemini validation", e)
            throw ApiError.Timeout()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Gemini validation", e)
            throw ApiError.Unexpected("Validation error: ${e.message}", e)
        }
    }
    
    override suspend fun testConnection(provider: AiProvider, apiKey: String): Result<String> {
        return try {
            Log.d(TAG, "Testing connection for ${provider.displayName}")
            
            val sanitizedKey = apiKey.trim()

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
                Result.failure(ApiError.InvalidKey(errorMessage))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error during connection test for ${provider.displayName}", e)
            Result.failure(ApiError.Http(e.code(), cause = e))
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout during connection test for ${provider.displayName}", e)
            Result.failure(ApiError.Timeout())
        } catch (e: IOException) {
            Log.e(TAG, "Network error during connection test for ${provider.displayName}", e)
            Result.failure(ApiError.Network("Network error: ${e.message}", e))
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid argument during connection test for ${provider.displayName}", e)
            Result.failure(ApiError.InvalidKey("Invalid API key format"))
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during connection test for ${provider.displayName}", e)
            Result.failure(ApiError.Unexpected("Validation error: ${e.message}", e))
        }
    }
    
    /**
     * Get expected API key format hint for users
     */
    fun getApiKeyFormatHint(provider: AiProvider): String {
        return when (provider) {
            AiProvider.OPENAI -> "Should start with 'sk-' followed by letters/numbers/dashes (supports sk-proj-, sk-live-, etc.)"
            AiProvider.GEMINI -> "Should be 10+ printable characters from Google AI Studio"
        }
    }
}