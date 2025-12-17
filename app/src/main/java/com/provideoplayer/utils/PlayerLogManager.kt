package com.provideoplayer.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import org.json.JSONArray

/**
 * Manages persistent player logs using SharedPreferences.
 * Logs survive player crashes and can be accessed from MainActivity.
 */
object PlayerLogManager {
    
    private const val PREFS_NAME = "player_logs_prefs"
    private const val KEY_LOGS = "debug_logs"
    private const val MAX_LOG_ENTRIES = 500
    
    /**
     * Save logs to SharedPreferences
     */
    fun saveLogs(context: Context, logs: List<String>) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonArray = JSONArray()
            
            // Keep only last MAX_LOG_ENTRIES
            val logsToSave = if (logs.size > MAX_LOG_ENTRIES) {
                logs.takeLast(MAX_LOG_ENTRIES)
            } else {
                logs
            }
            
            logsToSave.forEach { log ->
                jsonArray.put(log)
            }
            
            prefs.edit()
                .putString(KEY_LOGS, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("PlayerLogManager", "Failed to save logs", e)
        }
    }
    
    /**
     * Add a single log entry (appends to existing logs)
     */
    fun addLog(context: Context, message: String) {
        try {
            val timestamp = java.text.SimpleDateFormat(
                "HH:mm:ss.SSS", 
                java.util.Locale.getDefault()
            ).format(java.util.Date())
            
            val logEntry = "[$timestamp] $message"
            
            val existingLogs = getLogs(context).toMutableList()
            existingLogs.add(logEntry)
            
            // Keep only last MAX_LOG_ENTRIES
            while (existingLogs.size > MAX_LOG_ENTRIES) {
                existingLogs.removeAt(0)
            }
            
            saveLogs(context, existingLogs)
        } catch (e: Exception) {
            android.util.Log.e("PlayerLogManager", "Failed to add log", e)
        }
    }
    
    /**
     * Get all saved logs
     */
    fun getLogs(context: Context): List<String> {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = prefs.getString(KEY_LOGS, "[]") ?: "[]"
            val jsonArray = JSONArray(jsonString)
            
            val logs = mutableListOf<String>()
            for (i in 0 until jsonArray.length()) {
                logs.add(jsonArray.getString(i))
            }
            logs
        } catch (e: Exception) {
            android.util.Log.e("PlayerLogManager", "Failed to get logs", e)
            emptyList()
        }
    }
    
    /**
     * Clear all saved logs
     */
    fun clearLogs(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove(KEY_LOGS)
                .apply()
        } catch (e: Exception) {
            android.util.Log.e("PlayerLogManager", "Failed to clear logs", e)
        }
    }
    
    /**
     * Copy logs to clipboard
     * @return true if logs were copied, false if no logs to copy
     */
    fun copyLogsToClipboard(context: Context): Boolean {
        val logs = getLogs(context)
        if (logs.isEmpty()) {
            return false
        }
        
        val logsText = logs.joinToString("\n")
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Player Debug Logs", logsText)
        clipboard.setPrimaryClip(clip)
        return true
    }
    
    /**
     * Get formatted logs string with header info
     */
    fun getFormattedLogs(context: Context): String {
        val logs = getLogs(context)
        
        if (logs.isEmpty()) {
            return "No logs available.\n\nPlay a video and perform actions to generate logs."
        }
        
        val header = buildString {
            appendLine("=== PLAYER LOGS ===")
            appendLine("Total entries: ${logs.size}")
            appendLine("===================")
            appendLine()
        }
        
        return header + logs.joinToString("\n")
    }
}
