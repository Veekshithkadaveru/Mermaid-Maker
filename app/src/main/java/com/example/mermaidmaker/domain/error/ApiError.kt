package com.example.mermaidmaker.domain.error

/**
 * Domain-specific API error types for networking and validation flows.
 * These extend Exception so they can be used with Result.failure while preserving type.
 */
sealed class ApiError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidKey(message: String = "Invalid API key format") : ApiError(message)
    class Network(message: String, cause: Throwable? = null) : ApiError(message, cause)
    class Timeout(message: String = "Request timeout. Please check your internet connection.") :
        ApiError(message)

    class Http(val code: Int, message: String = "HTTP error $code", cause: Throwable? = null) :
        ApiError(message, cause)

    class Unexpected(message: String, cause: Throwable? = null) : ApiError(message, cause)
}


