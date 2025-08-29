package com.example.mermaidmaker.domain.service

import android.net.Uri

/**
 * Service for exporting files using Storage Access Framework
 */
interface FileExportService {
    /**
     * Export SVG content to a file using Storage Access Framework
     * @param svgContent The SVG content to export
     * @param fileName The suggested filename
     * @param onResult Callback with result (Uri on success, null on failure)
     */
    suspend fun exportSvg(
        svgContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    )

    /**
     * Export diagram source content to a file
     * @param sourceContent The diagram source content
     * @param fileName The suggested filename
     * @param onResult Callback with result (Uri on success, null on failure)
     */
    suspend fun exportSource(
        sourceContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    )

    /**
     * Share SVG content using system share intent
     * @param svgContent The SVG content to share
     * @param fileName The filename for sharing
     */
    suspend fun shareSvg(
        svgContent: String,
        fileName: String
    )
}