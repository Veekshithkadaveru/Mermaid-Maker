package com.example.mermaidmaker.domain.usecase

import android.net.Uri
import android.webkit.WebView
import com.example.mermaidmaker.domain.service.FileExportService
import com.example.mermaidmaker.util.WebViewPngGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for exporting diagrams as PNG files
 */
class ExportPngUseCase(
    private val pngGenerator: WebViewPngGenerator,
    private val fileExportService: FileExportService
) {
    
    /**
     * Export diagram as PNG file
     * @param webView The WebView containing the rendered diagram
     * @param isWebViewReady Whether the WebView has finished loading
     * @param fileName The desired filename for the export
     * @return Uri of the saved file, or null if export failed
     */
    suspend fun exportPng(
        webView: WebView,
        isWebViewReady: Boolean,
        fileName: String
    ): Uri? = withContext(Dispatchers.IO) {
        // Generate PNG from WebView
        val pngData = pngGenerator.generatePng(webView, isWebViewReady)
            ?: return@withContext null
        
        // Export PNG to file system
        var resultUri: Uri? = null
        fileExportService.exportPng(
            pngData = pngData,
            fileName = fileName
        ) { uri ->
            resultUri = uri
        }
        
        resultUri
    }
    
    /**
     * Share diagram as PNG
     * @param webView The WebView containing the rendered diagram
     * @param isWebViewReady Whether the WebView has finished loading
     * @param fileName The filename for sharing
     * @return true if share was initiated successfully, false otherwise
     */
    suspend fun sharePng(
        webView: WebView,
        isWebViewReady: Boolean,
        fileName: String
    ): Boolean = withContext(Dispatchers.IO) {
        // Generate PNG from WebView
        val pngData = pngGenerator.generatePng(webView, isWebViewReady)
            ?: return@withContext false
        
        // Share PNG using system share intent
        fileExportService.sharePng(pngData, fileName)
    }
}