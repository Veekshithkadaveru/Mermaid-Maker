package com.example.mermaidmaker.domain.usecase

import com.example.mermaidmaker.domain.service.DiagramRenderer
import com.example.mermaidmaker.domain.service.ExportResult
import com.example.mermaidmaker.domain.service.FileExportService
import com.example.mermaidmaker.domain.service.ShareResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Use case for exporting diagrams as PNG files
 */
class ExportPngUseCase(
    private val diagramRenderer: DiagramRenderer,
    private val fileExportService: FileExportService
) {
    
    /**
     * Export diagram as PNG file
     * @param content The Mermaid diagram content to export
     * @param fileName The desired filename for the export
     * @return ExportResult indicating success with URI or failure with error message
     */
    suspend fun exportPng(
        content: String,
        fileName: String
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            // Render diagram to PNG
            val pngData = diagramRenderer.renderToPng(content, diagramRenderer.isReady())
                ?: return@withContext ExportResult.Error("Failed to render diagram to PNG")
            
            // Export PNG to file system
            var resultUri: String? = null
            fileExportService.exportPng(
                pngData = pngData,
                fileName = fileName
            ) { uri ->
                resultUri = uri.toString()
            }
            
            resultUri?.let { ExportResult.Success(it) }
                ?: ExportResult.Error("Failed to save PNG file")
        } catch (e: Exception) {
            ExportResult.Error("Export failed: ${e.message}")
        }
    }
    
    /**
     * Share diagram as PNG
     * @param content The Mermaid diagram content to share
     * @param fileName The filename for sharing
     * @return ShareResult indicating success or failure with error message
     */
    suspend fun sharePng(
        content: String,
        fileName: String
    ): ShareResult = withContext(Dispatchers.IO) {
        try {
            // Render diagram to PNG
            val pngData = diagramRenderer.renderToPng(content, diagramRenderer.isReady())
                ?: return@withContext ShareResult.Error("Failed to render diagram to PNG")
            
            // Share PNG using system share intent
            val success = fileExportService.sharePng(pngData, fileName)
            if (success) {
                ShareResult.Success
            } else {
                ShareResult.Error("Failed to initiate share")
            }
        } catch (e: Exception) {
            ShareResult.Error("Share failed: ${e.message}")
        }
    }
}