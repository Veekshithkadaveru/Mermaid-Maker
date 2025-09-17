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

                Log.w(TAG, "Direct diagram SVG export requires WebView rendering. Using source export instead.")
                fileExportService.exportSource(diagram.content, "${diagram.title}.mmd", onResult)
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
                val fileName = "${diagram.title}.mmd"
                
                Log.w(TAG, "Direct diagram SVG sharing requires WebView rendering. Sharing source instead.")

                val sourceContent = "Mermaid Diagram: ${diagram.title}\n\n${diagram.content}"
                fileExportService.exportSource(sourceContent, fileName) { /* ignore result for sharing */ }
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

}