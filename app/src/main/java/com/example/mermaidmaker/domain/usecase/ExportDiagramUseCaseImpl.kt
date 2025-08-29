package com.example.mermaidmaker.domain.usecase

import android.net.Uri
import android.util.Log
import com.example.mermaidmaker.domain.repository.DiagramRepository
import com.example.mermaidmaker.domain.service.FileExportService

/**
 * Implementation of ExportDiagramUseCase
 */
class ExportDiagramUseCaseImpl(
    private val diagramRepository: DiagramRepository,
    private val fileExportService: FileExportService
) : ExportDiagramUseCase {

    companion object {
        private const val TAG = "ExportDiagramUseCase"
    }

    override suspend fun exportAsSvg(diagramId: String, onResult: (Uri?) -> Unit) {
        try {
            val diagram = diagramRepository.getDiagramById(diagramId)
            if (diagram != null) {
                val fileName = "${diagram.title}.svg"

                val svgContent = generateSvgPlaceholder(diagram.title)
                fileExportService.exportSvg(svgContent, fileName, onResult)
            } else {
                Log.e(TAG, "Diagram not found: $diagramId")
                onResult(null)
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid diagram ID or content for SVG export", e)
            onResult(null)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied during SVG export", e)
            onResult(null)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during SVG export", e)
            onResult(null)
        }
    }

    override suspend fun exportAsSource(diagramId: String, onResult: (Uri?) -> Unit) {
        try {
            val diagram = diagramRepository.getDiagramById(diagramId)
            if (diagram != null) {
                val fileName = "${diagram.title}.mmd"
                fileExportService.exportSource(diagram.content, fileName, onResult)
            } else {
                Log.e(TAG, "Diagram not found: $diagramId")
                onResult(null)
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid diagram ID or content for source export", e)
            onResult(null)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied during source export", e)
            onResult(null)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during source export", e)
            onResult(null)
        }
    }

    override suspend fun shareAsSvg(diagramId: String) {
        try {
            val diagram = diagramRepository.getDiagramById(diagramId)
            if (diagram != null) {
                val fileName = "${diagram.title}.svg"
                val svgContent = generateSvgPlaceholder(diagram.title)
                fileExportService.shareSvg(svgContent, fileName)
            } else {
                Log.e(TAG, "Diagram not found: $diagramId")
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid diagram ID or content for SVG sharing", e)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied during SVG sharing", e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during SVG sharing", e)
        }
    }

    override suspend fun exportSvgContent(
        svgContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        try {
            fileExportService.exportSvg(svgContent, fileName, onResult)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid SVG content for export", e)
            onResult(null)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied during SVG content export", e)
            onResult(null)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during SVG content export", e)
            onResult(null)
        }
    }

    override suspend fun shareSvgContent(svgContent: String, fileName: String) {
        try {
            fileExportService.shareSvg(svgContent, fileName)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid SVG content for sharing", e)
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied during SVG content sharing", e)
        } catch (e: java.io.IOException) {
            Log.e(TAG, "IO error during SVG content sharing", e)
        }
    }

    private fun generateSvgPlaceholder(title: String): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" width="400" height="200" viewBox="0 0 400 200">
                <rect width="400" height="200" fill="#f9f9f9" stroke="#333" stroke-width="2"/>
                <text x="200" y="100" text-anchor="middle" font-family="Arial, sans-serif" font-size="16" fill="#333">
                    $title
                </text>
                <text x="200" y="130" text-anchor="middle" font-family="Arial, sans-serif" font-size="12" fill="#666">
                    (Diagram content would be rendered here)
                </text>
            </svg>
        """.trimIndent()
    }
}