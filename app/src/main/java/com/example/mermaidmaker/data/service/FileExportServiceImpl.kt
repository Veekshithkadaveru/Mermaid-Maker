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

    override suspend fun exportSvg(
        svgContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        // Validate inputs
        if (svgContent.isBlank()) {
            Log.e(TAG, "Empty SVG content")
            onResult(null)
            return
        }
        
        if (!isValidFileName(fileName)) {
            Log.e(TAG, "Invalid file name: $fileName")
            onResult(null)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportToMediaStore(
                    data = svgContent.toByteArray(),
                    fileName = fileName,
                    mimeType = SVG_MIME_TYPE,
                    relativePath = android.os.Environment.DIRECTORY_DOWNLOADS,
                    targetUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    onResult = onResult
                )
            } else {
                // Fallback for Android < 10: Use external files directory
                exportSvgLegacy(svgContent, fileName, onResult)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during SVG export", e)
            onResult(null)
        }
    }

    override suspend fun exportSource(
        sourceContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        if (sourceContent.isBlank()) {
            Log.e(TAG, "Empty source content")
            onResult(null)
            return
        }
        
        if (!isValidFileName(fileName)) {
            Log.e(TAG, "Invalid file name: $fileName")
            onResult(null)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportToMediaStore(
                    data = sourceContent.toByteArray(),
                    fileName = fileName,
                    mimeType = TEXT_MIME_TYPE,
                    relativePath = android.os.Environment.DIRECTORY_DOWNLOADS,
                    targetUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    onResult = onResult
                )
            } else {
                // Fallback for Android < 10: Use external files directory
                exportSourceLegacy(sourceContent, fileName, onResult)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during source export", e)
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
        if (pngData.isEmpty()) {
            Log.e(TAG, "Empty PNG data")
            onResult(null)
            return
        }
        
        if (!isValidFileName(fileName)) {
            Log.e(TAG, "Invalid file name: $fileName")
            onResult(null)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportToMediaStore(
                    data = pngData,
                    fileName = fileName,
                    mimeType = PNG_MIME_TYPE,
                    relativePath = android.os.Environment.DIRECTORY_PICTURES + "/MermaidMaker",
                    targetUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    onResult = onResult
                )
            } else {
                // Fallback for Android < 10: Use external files directory
                exportPngLegacy(pngData, fileName, onResult)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error during PNG export", e)
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

    /**
     * Common method for exporting to MediaStore API (Android 10+)
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun exportToMediaStore(
        data: ByteArray,
        fileName: String,
        mimeType: String,
        relativePath: String,
        targetUri: Uri,
        onResult: (Uri?) -> Unit
    ) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }

        val uri = context.contentResolver.insert(targetUri, contentValues)
        
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(data)
                outputStream.flush()
            }
            
            Log.d(TAG, "File exported successfully using MediaStore: $uri (type: $mimeType)")
            onResult(uri)
        } else {
            Log.e(TAG, "Failed to create MediaStore entry for $mimeType")
            onResult(null)
        }
    }

    /**
     * Validate file name to prevent security issues and file system errors
     */
    private fun isValidFileName(fileName: String): Boolean {
        if (fileName.isBlank() || fileName.length > 255) return false
        
        val invalidChars = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|', '\n', '\r', '\t')
        if (fileName.any { it in invalidChars }) return false
        
        val reservedNames = setOf("CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", 
            "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", 
            "LPT6", "LPT7", "LPT8", "LPT9")
        if (fileName.uppercase() in reservedNames) return false
        
        return true
    }

    /**
     * Legacy export methods for Android < 10
     */
    @Suppress("DEPRECATION")
    private suspend fun exportSvgLegacy(
        svgContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        exportToLegacyStorage(
            data = svgContent.toByteArray(),
            fileName = fileName,
            directory = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
            onResult = onResult
        )
    }

    @Suppress("DEPRECATION")
    private suspend fun exportSourceLegacy(
        sourceContent: String,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        exportToLegacyStorage(
            data = sourceContent.toByteArray(),
            fileName = fileName,
            directory = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
            onResult = onResult
        )
    }

    @Suppress("DEPRECATION")
    private suspend fun exportPngLegacy(
        pngData: ByteArray,
        fileName: String,
        onResult: (Uri?) -> Unit
    ) {
        val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES)
        val mermaidDir = File(picturesDir, "MermaidMaker")
        if (!mermaidDir.exists()) {
            mermaidDir.mkdirs()
        }
        
        exportToLegacyStorage(
            data = pngData,
            fileName = fileName,
            directory = mermaidDir,
            onResult = onResult
        )
    }

    /**
     * Common method for legacy storage export
     */
    private suspend fun exportToLegacyStorage(
        data: ByteArray,
        fileName: String,
        directory: File,
        onResult: (Uri?) -> Unit
    ) {
        try {
            val file = File(directory, fileName)
            file.writeBytes(data)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            Log.d(TAG, "File exported successfully (legacy): $uri")
            onResult(uri)
            
        } catch (e: Exception) {
            Log.e(TAG, "Legacy export failed", e)
            onResult(null)
        }
    }

}