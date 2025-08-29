package com.example.mermaidmaker.domain.usecase

import android.net.Uri

/**
 * Use case for exporting diagram content in various formats
 */
interface ExportDiagramUseCase {
    /**
     * Export diagram as SVG
     * @param diagramId The ID of the diagram to export
     * @param onResult Callback with result (Uri on success, null on failure)
     */
    suspend fun exportAsSvg(diagramId: String, onResult: (Uri?) -> Unit)

    /**
     * Export diagram source content
     * @param diagramId The ID of the diagram to export
     * @param onResult Callback with result (Uri on success, null on failure)
     */
    suspend fun exportAsSource(diagramId: String, onResult: (Uri?) -> Unit)

    /**
     * Share diagram as SVG
     * @param diagramId The ID of the diagram to share
     */
    suspend fun shareAsSvg(diagramId: String)

    /**
     * Export SVG content directly (for preview use)
     * @param svgContent The SVG content to export
     * @param fileName Suggested filename
     * @param onResult Callback with result (Uri on success, null on failure)
     */
    suspend fun exportSvgContent(
        svgContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    )

    /**
     * Share SVG content directly (for preview use)
     * @param svgContent The SVG content to share
     * @param fileName Filename for sharing
     */
    suspend fun shareSvgContent(svgContent: String, fileName: String)
}