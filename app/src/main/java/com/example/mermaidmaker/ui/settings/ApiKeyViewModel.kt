package com.example.mermaidmaker.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mermaidmaker.domain.model.AiProvider
import com.example.mermaidmaker.domain.model.ApiKeyConfiguration
import com.example.mermaidmaker.domain.repository.ApiKeyRepository
import com.example.mermaidmaker.domain.service.ApiKeyValidationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ApiKeyViewModel(
    private val apiKeyRepository: ApiKeyRepository,
    private val validationService: ApiKeyValidationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ApiKeyUiState())
    val uiState: StateFlow<ApiKeyUiState> = _uiState.asStateFlow()

    private val _configurations = MutableStateFlow<List<ApiKeyConfiguration>>(emptyList())
    val configurations: StateFlow<List<ApiKeyConfiguration>> = _configurations.asStateFlow()

    init {
        loadConfigurations()
    }

    private fun loadConfigurations() {
        viewModelScope.launch {
            apiKeyRepository.getApiKeyConfigurations().collect { configs ->
                _configurations.value = configs
            }
        }
    }

    fun saveApiKey(provider: AiProvider, apiKey: String) {
        val sanitizedKey = apiKey.trim()
        if (sanitizedKey.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "API key cannot be empty"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            apiKeyRepository.storeApiKey(provider, sanitizedKey)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "API key saved successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to save API key: ${error.message}"
                    )
                }
        }
    }

    fun removeApiKey(provider: AiProvider) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            apiKeyRepository.removeApiKey(provider)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "API key removed successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to remove API key: ${error.message}"
                    )
                }
        }
    }

    fun validateApiKey(provider: AiProvider) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isValidating = true, error = null)
            
            // Get the API key first
            apiKeyRepository.getApiKey(provider)
                .onSuccess { apiKey ->
                    if (apiKey != null) {
                        // Validate the API key
                        validationService.validateApiKey(provider, apiKey)
                            .onSuccess { isValid ->
                                apiKeyRepository.updateKeyValidationStatus(provider, isValid)
                                    .onSuccess {
                                        _uiState.value = _uiState.value.copy(
                                            isValidating = false,
                                            successMessage = if (isValid) "API key validated successfully" else "API key validation failed"
                                        )
                                    }
                                    .onFailure { error ->
                                        _uiState.value = _uiState.value.copy(
                                            isValidating = false,
                                            error = "Failed to update validation status: ${error.message}"
                                        )
                                    }
                            }
                            .onFailure { error ->
                                _uiState.value = _uiState.value.copy(
                                    isValidating = false,
                                    error = "Validation failed: ${error.message}"
                                )
                            }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isValidating = false,
                            error = "No API key found for ${provider.displayName}"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isValidating = false,
                        error = "Failed to retrieve API key: ${error.message}"
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            successMessage = null
        )
    }

    fun clearAllApiKeys() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            apiKeyRepository.clearAllApiKeys()
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "All API keys cleared successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to clear API keys: ${error.message}"
                    )
                }
        }
    }
}

data class ApiKeyUiState(
    val isLoading: Boolean = false,
    val isValidating: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)