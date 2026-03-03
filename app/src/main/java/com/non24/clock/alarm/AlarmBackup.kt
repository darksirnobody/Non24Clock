package com.non24.clock.alarm

import android.content.Context
import android.util.Log
import com.non24.clock.data.model.Alarm
import java.io.File

/**
 * Backup system for alarms in Device Protected Storage
 * Allows alarm rescheduling before device unlock
 */
object AlarmBackup {
    private const val BACKUP_FILE = "alarm_backup.txt"
    private const val TAG = "AlarmBackup"

    /**
     * Save enabled alarms to device protected storage
     * Called whenever alarms are modified
     */
    fun saveAlarms(context: Context, alarms: List<Alarm>) {
        try {
            val deviceContext = getDeviceProtectedContext(context)
            val file = File(deviceContext.filesDir, BACKUP_FILE)

            // Only save enabled alarms
            val enabledAlarms = alarms.filter { it.enabled }

            val backupData = enabledAlarms.joinToString("\n") { alarm ->
                // Format: id,groupId,hour,minute,label,repeating,soundUri,vibrate,snoozeEnabled,snoozeDurationMinutes
                "${alarm.id},${alarm.groupId},${alarm.hour},${alarm.minute}," +
                        "${alarm.label.replace(",", ";")}," + // Replace commas in label
                        "${alarm.repeating},${alarm.soundUri ?: ""},${alarm.vibrate}," +
                        "${alarm.snoozeEnabled},${alarm.snoozeDurationMinutes}"
            }

            file.writeText(backupData)
            Log.d(TAG, "Saved ${enabledAlarms.size} alarms to backup")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save alarm backup", e)
        }
    }

    /**
     * Load alarms from device protected storage
     * Used during boot before device unlock
     */
    fun loadAlarms(context: Context): List<Alarm> {
        return try {
            val deviceContext = getDeviceProtectedContext(context)
            val file = File(deviceContext.filesDir, BACKUP_FILE)

            if (!file.exists()) {
                Log.d(TAG, "No backup file found")
                return emptyList()
            }

            val alarms = file.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    try {
                        parseAlarmLine(line)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse alarm line: $line", e)
                        null
                    }
                }

            Log.d(TAG, "Loaded ${alarms.size} alarms from backup")
            alarms
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load alarm backup", e)
            emptyList()
        }
    }

    /**
     * Parse a single alarm line from backup file
     */
    private fun parseAlarmLine(line: String): Alarm? {
        val parts = line.split(",")
        if (parts.size < 10) return null

        return Alarm(
            id = parts[0].toLongOrNull() ?: return null,
            groupId = parts[1].toLongOrNull() ?: return null,
            hour = parts[2].toIntOrNull() ?: return null,
            minute = parts[3].toIntOrNull() ?: return null,
            label = parts[4].replace(";", ","), // Restore commas in label
            enabled = true, // Only enabled alarms are in backup
            repeating = parts[5].toBoolean(),
            soundUri = parts[6].ifBlank { null },
            vibrate = parts[7].toBoolean(),
            snoozeEnabled = parts[8].toBoolean(),
            snoozeDurationMinutes = parts[9].toIntOrNull() ?: 5
        )
    }

    /**
     * Get device protected storage context
     * This storage is accessible before device unlock
     */
    private fun getDeviceProtectedContext(context: Context): Context {
        return if (context.isDeviceProtectedStorage) {
            context
        } else {
            context.createDeviceProtectedStorageContext()
        }
    }

    /**
     * Clear all alarm backups
     * Called when all alarms are deleted
     */
    fun clearBackup(context: Context) {
        try {
            val deviceContext = getDeviceProtectedContext(context)
            val file = File(deviceContext.filesDir, BACKUP_FILE)
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Cleared alarm backup")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear alarm backup", e)
        }
    }
}