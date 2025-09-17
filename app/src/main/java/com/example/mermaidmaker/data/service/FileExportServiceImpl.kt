package com.example.mermaidmaker.data.service

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
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
        private const val PNG_MIME_TYPE = "image/png"
        private const val TEXT_MIME_TYPE = "text/plain"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun exportSvg(
        svgContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        try {
            // Use MediaStore API for Android 10+ (no permissions required for Downloads)
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, SVG_MIME_TYPE)
                put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(svgContent.toByteArray())
                    outputStream.flush()
                }
                
                Log.d(TAG, "SVG exported successfully using MediaStore: $uri")
                onResult(uri)
            } else {
                Log.e(TAG, "Failed to create MediaStore entry for SVG")
                onResult(null)
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied during SVG export", e)
            onResult(null)
        } catch (e: IOException) {
            Log.e(TAG, "IO error during SVG export", e)
            onResult(null)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during SVG export", e)
            onResult(null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override suspend fun exportSource(
        sourceContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        try {
            // Use MediaStore API for Android 10+ (no permissions required for Downloads)
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, TEXT_MIME_TYPE)
                put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(sourceContent.toByteArray())
                    outputStream.flush()
                }
                
                Log.d(TAG, "Source exported successfully using MediaStore: $uri")
                onResult(uri)
            } else {
                Log.e(TAG, "Failed to create MediaStore entry for source")
                onResult(null)
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied during source export", e)
            onResult(null)
        } catch (e: IOException) {
            Log.e(TAG, "IO error during source export", e)
            onResult(null)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during source export", e)
            onResult(null)
        }
    }

    override suspend fun shareSvg(svgContent: String, fileName: String): Boolean {
        return try {
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
            
            Log.d(TAG, "SVG share initiated successfully")
            true

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied while sharing SVG", e)
            false
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid arguments while sharing SVG", e)
            false
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "No activity found to handle SVG share", e)
            false
        } catch (e: IOException) {
            Log.e(TAG, "IO error while preparing SVG share", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while sharing SVG", e)
            false
        }
    }

    override suspend fun exportPng(
        pngData: ByteArray,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        try {
            // Use MediaStore API for Android 10+ (store under Pictures for better gallery compatibility)
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, PNG_MIME_TYPE)
                put(MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/MermaidMaker")
            }

            val imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val uri = context.contentResolver.insert(imagesUri, contentValues)
            
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(pngData)
                    outputStream.flush()
                }
                
                Log.d(TAG, "PNG exported successfully using MediaStore: $uri")
                onResult(uri)
            } else {
                Log.e(TAG, "Failed to create MediaStore entry for PNG")
                onResult(null)
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied during PNG export", e)
            onResult(null)
        } catch (e: IOException) {
            Log.e(TAG, "IO error during PNG export", e)
            onResult(null)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during PNG export", e)
            onResult(null)
        }
    }

    override suspend fun sharePng(pngData: ByteArray, fileName: String): Boolean {
        return try {

            val cacheDir = File(context.cacheDir, "shared_exports")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val tempFile = File(cacheDir, fileName)
            tempFile.writeBytes(pngData)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = PNG_MIME_TYPE
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Mermaid Diagram: $fileName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share diagram")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            
            Log.d(TAG, "PNG share initiated successfully")
            true

        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied while sharing PNG", e)
            false
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Invalid arguments while sharing PNG", e)
            false
        } catch (e: android.content.ActivityNotFoundException) {
            Log.e(TAG, "No activity found to handle PNG share", e)
            false
        } catch (e: IOException) {
            Log.e(TAG, "IO error while preparing PNG share", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while sharing PNG", e)
            false
        }
    }

}