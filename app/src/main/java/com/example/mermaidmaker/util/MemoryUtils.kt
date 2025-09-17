package com.example.mermaidmaker.util

import android.util.Log

/**
 * Utility class for memory management operations
 */
object MemoryUtils {
    private const val TAG = "MemoryUtils"
    private const val MEMORY_WARNING_THRESHOLD = 100 * 1024 * 1024 // 100MB

    /**
     * Check if there's sufficient memory available for bitmap operations
     */
    fun checkMemoryAvailability(): Boolean {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val availableMemory = maxMemory - usedMemory
        
        Log.d(TAG, "Memory check - Available: ${availableMemory / (1024 * 1024)}MB, Threshold: ${MEMORY_WARNING_THRESHOLD / (1024 * 1024)}MB")
        
        return availableMemory > MEMORY_WARNING_THRESHOLD
    }

    /**
     * Get formatted memory information for logging
     */
    fun getMemoryInfo(): String {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        val totalMemory = runtime.totalMemory() / (1024 * 1024)
        val freeMemory = runtime.freeMemory() / (1024 * 1024)
        val usedMemory = totalMemory - freeMemory
        
        return "Memory: ${usedMemory}MB used, ${freeMemory}MB free, ${maxMemory}MB max"
    }
}