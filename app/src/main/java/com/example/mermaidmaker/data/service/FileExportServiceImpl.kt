package com.example.mermaidmaker.data.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.mermaidmaker.domain.service.FileExportService
import com.example.mermaidmaker.domain.error.ApiError
import java.io.File
import java.io.IOException

/**
 * Implementation of FileExportService using Storage Access Framework
 */
class FileExportServiceImpl(
    private val context: Context
) : FileExportService {

    companion object {
        private const val TAG = "FileExportService"
        private const val SVG_MIME_TYPE = "image/svg+xml"
        private const val TEXT_MIME_TYPE = "text/plain"
    }

    override suspend fun exportSvg(
        svgContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = SVG_MIME_TYPE
                putExtra(Intent.EXTRA_TITLE, fileName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            Log.d(TAG, "Started SAF picker for SVG export")

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied starting SVG export", e)
            onResult(null)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid arguments for SVG export", e)
            onResult(null)
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "No activity found to handle SVG create document", e)
            onResult(null)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error starting SVG export", e)
            onResult(null)
        }
    }

    override suspend fun exportSource(
        sourceContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = TEXT_MIME_TYPE
                putExtra(Intent.EXTRA_TITLE, fileName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
            Log.d(TAG, "Started SAF picker for source export")

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied starting source export", e)
            onResult(null)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid arguments for source export", e)
            onResult(null)
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "No activity found to handle source create document", e)
            onResult(null)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error starting source export", e)
            onResult(null)
        }
    }

    override suspend fun shareSvg(svgContent: String, fileName: String) {
        try {
            // Create temporary file in cache directory for sharing
            val cacheDir = File(context.cacheDir, "shared_exports")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val tempFile = File(cacheDir, fileName)
            tempFile.writeText(svgContent)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = SVG_MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Mermaid Diagram: $fileName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share diagram")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied while sharing SVG", e)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid arguments while sharing SVG", e)
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "No activity found to handle SVG share", e)
        } catch (e: IOException) {
            Log.e(TAG, "IO error while preparing SVG share", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while sharing SVG", e)
        }
    }

    private fun writeSvgToUri(uri: Uri, svgContent: String, onResult: (Uri?) -> Unit) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(svgContent.toByteArray())
                outputStream.flush()
                Log.d(TAG, "SVG exported successfully to $uri")
                onResult(uri)
            } ?: run {
                Log.e(TAG, "Failed to open output stream for URI: $uri")
                onResult(null)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error writing SVG to URI: $uri", e)
            onResult(null)
        }
    }

    private fun writeSourceToUri(uri: Uri, sourceContent: String, onResult: (Uri?) -> Unit) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(sourceContent.toByteArray())
                outputStream.flush()
                Log.d(TAG, "Source exported successfully to $uri")
                onResult(uri)
            } ?: run {
                Log.e(TAG, "Failed to open output stream for URI: $uri")
                onResult(null)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error writing source to URI: $uri", e)
            onResult(null)
        }
    }
}