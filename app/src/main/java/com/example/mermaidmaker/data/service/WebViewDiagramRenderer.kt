package com.example.mermaidmaker.data.service

import android.webkit.WebView
import com.example.mermaidmaker.domain.service.DiagramRenderer
import com.example.mermaidmaker.util.WebViewPngGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of DiagramRenderer using Android WebView
 * This class bridges the domain abstraction with the Android-specific WebView implementation
 */
class WebViewDiagramRenderer(
    private val webView: WebView,
    private val pngGenerator: WebViewPngGenerator
) : DiagramRenderer {
    
    private var isWebViewReady: Boolean = false
    private var currentContent: String = ""
    
    override suspend fun renderToPng(content: String, isReady: Boolean): ByteArray? {
        return withContext(Dispatchers.Main) {
            // Ensure content is loaded if different from current
            if (content != currentContent) {
                loadContent(content)
            }
            
            // Generate PNG using the WebView PNG generator
            pngGenerator.generatePng(webView, isReady && isWebViewReady)
        }
    }
    
    override fun isReady(): Boolean = isWebViewReady
    
    override suspend fun loadContent(content: String) {
        withContext(Dispatchers.Main) {
            currentContent = content
            // Note: The actual WebView loading logic would be implemented here
            // This is a simplified version - the real implementation would need
            // to load the content into the WebView and wait for completion
            isWebViewReady = true
        }
    }
    
    /**
     * Set the ready state - typically called by the WebView client when loading completes
     */
    fun setReady(ready: Boolean) {
        isWebViewReady = ready
    }
    
    /**
     * Get the current content loaded in the renderer
     */
    fun getCurrentContent(): String = currentContent
}