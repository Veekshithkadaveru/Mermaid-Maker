package com.example.mermaidmaker.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.webkit.WebView
import com.example.mermaidmaker.util.WebViewPngGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ThumbnailGenerator(private val context: Context) {
    
    companion object {
        private const val TAG = "ThumbnailGenerator"
        private const val THUMBNAIL_WIDTH = 200
        private const val THUMBNAIL_HEIGHT = 150
        private const val THUMBNAIL_QUALITY = 85
        private const val THUMBNAILS_DIR = "thumbnails"
    }
    
    private val webViewPngGenerator = WebViewPngGenerator()
    
    suspend fun generateThumbnail(
        webView: WebView,
        isReady: Boolean,
        diagramId: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val fullSizePng = webViewPngGenerator.generatePng(webView, isReady)
                ?: return@withContext null
            
            val fullSizeBitmap = BitmapFactory.decodeByteArray(fullSizePng, 0, fullSizePng.size)
                ?: return@withContext null
            
            val thumbnail = createThumbnail(fullSizeBitmap)
            fullSizeBitmap.recycle()
            
            val thumbnailPath = saveThumbnail(thumbnail, diagramId)
            thumbnail.recycle()
            
            thumbnailPath
        } catch (e: Exception) {
            Log.e(TAG, "Error generating thumbnail for diagram $diagramId", e)
            null
        }
    }
    
    private fun createThumbnail(originalBitmap: Bitmap): Bitmap {
        val originalWidth = originalBitmap.width
        val originalHeight = originalBitmap.height
        
        val aspectRatio = originalWidth.toFloat() / originalHeight.toFloat()
        val thumbnailAspectRatio = THUMBNAIL_WIDTH.toFloat() / THUMBNAIL_HEIGHT.toFloat()
        
        val (scaledWidth, scaledHeight) = if (aspectRatio > thumbnailAspectRatio) {
            THUMBNAIL_WIDTH to (THUMBNAIL_WIDTH / aspectRatio).toInt()
        } else {
            (THUMBNAIL_HEIGHT * aspectRatio).toInt() to THUMBNAIL_HEIGHT
        }
        
        val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true)
        
        val thumbnail = Bitmap.createBitmap(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(thumbnail)
        
        canvas.drawColor(android.graphics.Color.WHITE)
        
        val left = (THUMBNAIL_WIDTH - scaledWidth) / 2f
        val top = (THUMBNAIL_HEIGHT - scaledHeight) / 2f
        
        canvas.drawBitmap(scaledBitmap, left, top, null)
        
        val paint = Paint().apply {
            color = android.graphics.Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawRect(RectF(0f, 0f, THUMBNAIL_WIDTH.toFloat(), THUMBNAIL_HEIGHT.toFloat()), paint)
        
        scaledBitmap.recycle()
        return thumbnail
    }
    
    private fun saveThumbnail(thumbnail: Bitmap, diagramId: String): String? {
        return try {
            val thumbnailsDir = File(context.filesDir, THUMBNAILS_DIR)
            if (!thumbnailsDir.exists()) {
                thumbnailsDir.mkdirs()
            }
            
            val thumbnailFile = File(thumbnailsDir, "$diagramId.jpg")
            val outputStream = FileOutputStream(thumbnailFile)
            
            thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, outputStream)
            outputStream.flush()
            outputStream.close()
            
            thumbnailFile.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Error saving thumbnail for diagram $diagramId", e)
            null
        }
    }
    
    fun deleteThumbnail(thumbnailPath: String?) {
        if (thumbnailPath == null) return
        
        try {
            val file = File(thumbnailPath)
            if (file.exists()) {
                val deleted = file.delete()
                if (!deleted) {
                    Log.w(TAG, "Failed to delete thumbnail at $thumbnailPath")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting thumbnail at $thumbnailPath", e)
        }
    }
    
    fun getThumbnailFile(thumbnailPath: String?): File? {
        if (thumbnailPath == null) return null
        
        val file = File(thumbnailPath)
        return if (file.exists()) file else null
    }
}