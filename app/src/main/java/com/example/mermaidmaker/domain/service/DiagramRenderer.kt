package com.example.mermaidmaker.domain.service

/**
 * Domain abstraction for rendering diagrams to different formats
 * This interface isolates the domain layer from Android-specific WebView dependencies
 */
interface DiagramRenderer {
    
    /**
     * Render diagram content to PNG format
     * @param content The Mermaid diagram content to render
     * @param isReady Whether the renderer is ready to produce output
     * @return PNG data as ByteArray, or null if rendering failed
     */
    suspend fun renderToPng(content: String, isReady: Boolean): ByteArray?
    
    /**
     * Check if the renderer is currently ready to render content
     * @return true if ready, false otherwise
     */
    fun isReady(): Boolean
    
    /**
     * Load content into the renderer for subsequent rendering
     * @param content The Mermaid diagram content to load
     */
    suspend fun loadContent(content: String)
}

/**
 * Domain model representing the result of an export operation
 */
sealed class ExportResult {
    data class Success(val uri: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

/**
 * Domain model representing the result of a share operation
 */
sealed class ShareResult {
    object Success : ShareResult()
    data class Error(val message: String) : ShareResult()
}