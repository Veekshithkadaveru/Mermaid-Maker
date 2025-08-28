package com.example.mermaidmaker.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.model.ApiKeyConfiguration
import com.example.mermaidmaker.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class ApiKeyRepositoryImpl(
    private val context: Context
) : ApiKeyRepository {

    companion object {
        private const val PREFS_NAME = "api_keys_secure_prefs"
        private const val KEY_PREFIX = "api_key_"
        private const val VALIDATION_PREFIX = "validation_"
        private const val TIMESTAMP_PREFIX = "timestamp_"
    }

    private val _configurations = MutableStateFlow<List<ApiKeyConfiguration>>(emptyList())

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    init {
        // Load existing configurations on init
        loadConfigurations()
    }

    override suspend fun storeApiKey(provider: AiProvider, apiKey: String): Result<Unit> {
        return try {
            // Check if this is the same key as before to preserve validation status
            val existingKey = encryptedPrefs.getString(KEY_PREFIX + provider.name, null)
            val shouldResetValidation = existingKey != apiKey
            
            encryptedPrefs.edit().apply {
                putString(KEY_PREFIX + provider.name, apiKey)
                putLong(TIMESTAMP_PREFIX + provider.name, System.currentTimeMillis())
                // Only reset validation status when key actually changes
                if (shouldResetValidation) {
                    putBoolean(VALIDATION_PREFIX + provider.name, false)
                }
                apply()
            }
            loadConfigurations()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getApiKey(provider: AiProvider): Result<String?> {
        return try {
            val apiKey = encryptedPrefs.getString(KEY_PREFIX + provider.name, null)
            Result.success(apiKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getApiKeyConfigurations(): Flow<List<ApiKeyConfiguration>> {
        return _configurations.asStateFlow()
    }

    override suspend fun removeApiKey(provider: AiProvider): Result<Unit> {
        return try {
            encryptedPrefs.edit().apply {
                remove(KEY_PREFIX + provider.name)
                remove(VALIDATION_PREFIX + provider.name)
                remove(TIMESTAMP_PREFIX + provider.name)
                apply()
            }
            loadConfigurations()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun hasApiKey(provider: AiProvider): Boolean {
        return try {
            encryptedPrefs.contains(KEY_PREFIX + provider.name) &&
                    !encryptedPrefs.getString(KEY_PREFIX + provider.name, "").isNullOrBlank()
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateKeyValidationStatus(
        provider: AiProvider,
        isValidated: Boolean
    ): Result<Unit> {
        return try {
            encryptedPrefs.edit().apply {
                putBoolean(VALIDATION_PREFIX + provider.name, isValidated)
                if (isValidated) {
                    putLong(TIMESTAMP_PREFIX + provider.name, System.currentTimeMillis())
                }
                apply()
            }
            loadConfigurations()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearAllApiKeys(): Result<Unit> {
        return try {
            encryptedPrefs.edit().clear().apply()
            loadConfigurations()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadConfigurations() {
        try {
            val configurations = AiProvider.entries.mapNotNull { provider ->
                val apiKey = encryptedPrefs.getString(KEY_PREFIX + provider.name, null)
                if (!apiKey.isNullOrBlank()) {
                    val isValidated = encryptedPrefs.getBoolean(VALIDATION_PREFIX + provider.name, false)
                    val timestamp = encryptedPrefs.getLong(TIMESTAMP_PREFIX + provider.name, 0L)
                    
                    ApiKeyConfiguration(
                        provider = provider,
                        apiKey = "***${apiKey.takeLast(4)}", // Mask the key for display
                        isValidated = isValidated,
                        lastValidated = if (timestamp > 0L) timestamp else null
                    )
                } else null
            }
            _configurations.value = configurations
        } catch (e: Exception) {
            // Log error but don't crash
            _configurations.value = emptyList()
        }
    }
}