package com.example.mermaidmaker.domain.usecase

/**
 * Use case for exporting diagram content in various formats
 */
interface ExportDiagramUseCase {
    /**
     * Export diagram as SVG
     * @param diagramId The ID of the diagram to export
     * @param onResult Callback with result (URI string on success, null on failure)
     */
    suspend fun exportAsSvg(diagramId: String, onResult: (String?) -> Unit)

    /**
     * Export diagram source content
     * @param diagramId The ID of the diagram to export
     * @param onResult Callback with result (URI string on success, null on failure)
     */
    suspend fun exportAsSource(diagramId: String, onResult: (String?) -> Unit)

    /**
     * Share diagram as SVG
     * @param diagramId The ID of the diagram to share
     */
    suspend fun shareAsSvg(diagramId: String)

    /**
     * Export SVG content directly (for preview use)
     * @param svgContent The SVG content to export
     * @param fileName Suggested filename
     * @param onResult Callback with result (URI string on success, null on failure)
     */
    suspend fun exportSvgContent(
        svgContent: String,
        fileName: String,
        onResult: (String?) -> Unit
    )

    /**
     * Share SVG content directly (for preview use)
     * @param svgContent The SVG content to share
     * @param fileName Filename for sharing
     */
    suspend fun shareSvgContent(svgContent: String, fileName: String)
}