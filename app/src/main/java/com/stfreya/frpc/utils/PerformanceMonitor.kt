package com.stfreya.frpc.utils

import android.os.Debug
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

object PerformanceMonitor {
    
    private const val TAG = "PerformanceMonitor"
    private val memoryThreshold = 50 * 1024 * 1024 // 50MB
    private val gcThreshold = 0.8 // 80% memory usage
    
    private val startTimes = ConcurrentHashMap<String, Long>()
    private val memorySnapshots = mutableListOf<MemorySnapshot>()
    
    data class MemorySnapshot(
        val timestamp: Long,
        val usedMemory: Long,
        val freeMemory: Long,
        val totalMemory: Long
    )
    
    fun startTiming(operation: String) {
        startTimes[operation] = System.currentTimeMillis()
    }
    
    fun endTiming(operation: String) {
        val startTime = startTimes.remove(operation)
        if (startTime != null) {
            val duration = System.currentTimeMillis() - startTime
            Log.d(TAG, "Operation '$operation' took ${duration}ms")
            
            if (duration > 1000) { // Log slow operations
                Log.w(TAG, "Slow operation detected: '$operation' took ${duration}ms")
            }
        }
    }
    
    fun measureMemory(operation: String) {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val freeMemory = runtime.freeMemory()
        val totalMemory = runtime.totalMemory()
        
        val snapshot = MemorySnapshot(
            timestamp = System.currentTimeMillis(),
            usedMemory = usedMemory,
            freeMemory = freeMemory,
            totalMemory = totalMemory
        )
        
        memorySnapshots.add(snapshot)
        
        // Keep only last 100 snapshots
        if (memorySnapshots.size > 100) {
            memorySnapshots.removeAt(0)
        }
        
        val memoryUsagePercent = (usedMemory.toDouble() / totalMemory) * 100
        
        Log.d(TAG, "Memory usage for '$operation': ${memoryUsagePercent.toInt()}% (${usedMemory / 1024 / 1024}MB/${totalMemory / 1024 / 1024}MB)")
        
        if (memoryUsagePercent > gcThreshold * 100) {
            Log.w(TAG, "High memory usage detected: ${memoryUsagePercent.toInt()}%")
            suggestGC()
        }
    }
    
    private fun suggestGC() {
        Log.i(TAG, "Suggesting garbage collection due to high memory usage")
        System.gc()
    }
    
    fun getMemoryInfo(): String {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val freeMemory = runtime.freeMemory()
        
        return """
            Memory Info:
            Used: ${usedMemory / 1024 / 1024}MB
            Free: ${freeMemory / 1024 / 1024}MB
            Max: ${maxMemory / 1024 / 1024}MB
            Usage: ${(usedMemory.toDouble() / maxMemory * 100).toInt()}%
        """.trimIndent()
    }
    
    fun getMemoryTrend(): String {
        if (memorySnapshots.size < 2) return "Insufficient data"
        
        val recent = memorySnapshots.takeLast(5)
        val oldest = recent.first()
        val newest = recent.last()
        
        val memoryChange = newest.usedMemory - oldest.usedMemory
        val timeChange = newest.timestamp - oldest.timestamp
        
        val trend = if (memoryChange > 0) "Increasing" else "Decreasing"
        val rate = if (timeChange > 0) memoryChange / timeChange else 0
        
        return "Memory trend: $trend (${rate / 1024}KB/s)"
    }
    
    fun checkMemoryLeak(): Boolean {
        if (memorySnapshots.size < 10) return false
        
        val recent = memorySnapshots.takeLast(10)
        val oldest = recent.first()
        val newest = recent.last()
        
        val memoryIncrease = newest.usedMemory - oldest.usedMemory
        val timeSpan = newest.timestamp - oldest.timestamp
        
        // If memory increased by more than 10MB in 10 snapshots, potential leak
        return memoryIncrease > 10 * 1024 * 1024 && timeSpan > 0
    }
    
    fun logPerformanceReport() {
        Log.i(TAG, "=== Performance Report ===")
        Log.i(TAG, getMemoryInfo())
        Log.i(TAG, getMemoryTrend())
        
        if (checkMemoryLeak()) {
            Log.w(TAG, "Potential memory leak detected!")
        }
        
        Log.i(TAG, "=== End Report ===")
    }
    
    fun clearSnapshots() {
        memorySnapshots.clear()
        startTimes.clear()
    }
}

// Extension functions for easy usage
inline fun <T> measureTime(operation: String, block: () -> T): T {
    PerformanceMonitor.startTiming(operation)
    return try {
        block()
    } finally {
        PerformanceMonitor.endTiming(operation)
    }
}

inline fun <T> measureMemory(operation: String, block: () -> T): T {
    PerformanceMonitor.measureMemory(operation)
    return block()
}
