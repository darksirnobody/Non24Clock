package com.non24.clock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return

        Log.d("BootReceiver", "========================================")
        Log.d("BootReceiver", "onReceive: $action")
        Log.d("BootReceiver", "========================================")

        when (action) {
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                // BEFORE unlock - schedule from backup file!
                Log.d("BootReceiver", "Device LOCKED - scheduling from backup")
                scheduleAlarmsFromBackup(context)
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                // AFTER unlock - schedule from database
                Log.d("BootReceiver", "Device UNLOCKED - scheduling from database")
                scheduleAlarmsFromDatabase(context)
            }
        }
    }

    /**
     * Schedule alarms from backup file (works BEFORE device unlock)
     */
    private fun scheduleAlarmsFromBackup(context: Context) {
        try {
            Log.d("BootReceiver", "Loading alarms from backup file...")
            val alarms = AlarmBackup.loadAlarms(context)

            Log.d("BootReceiver", "Found ${alarms.size} enabled alarms in backup")

            if (alarms.isEmpty()) {
                Log.d("BootReceiver", "No alarms to schedule")
                return
            }

            // Schedule each alarm directly (no service needed!)
            alarms.forEachIndexed { index, alarm ->
                try {
                    Log.d("BootReceiver", "Scheduling alarm ${index + 1}/${alarms.size}: ID=${alarm.id}")
                    AlarmScheduler.scheduleAlarm(context, alarm)
                    Log.d("BootReceiver", "  ✓ Alarm ${alarm.id} scheduled successfully")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "  ✗ Failed to schedule alarm ${alarm.id}", e)
                }
            }

            Log.d("BootReceiver", "========================================")
            Log.d("BootReceiver", "LOCKED_BOOT scheduling COMPLETE!")
            Log.d("BootReceiver", "========================================")

        } catch (e: Exception) {
            Log.e("BootReceiver", "FATAL ERROR during LOCKED_BOOT scheduling", e)
        }
    }

    /**
     * Schedule alarms from database (works AFTER device unlock)
     * Uses background coroutine to avoid blocking
     */
    private fun scheduleAlarmsFromDatabase(context: Context) {
        // Use goAsync() to allow background work
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("BootReceiver", "Loading alarms from database...")

                val database = com.non24.clock.data.database.Non24Database.getDatabase(context)
                val allAlarms = database.alarmDao().getAllAlarmsOnce()
                val enabledAlarms = allAlarms.filter { it.enabled }

                Log.d("BootReceiver", "Found ${enabledAlarms.size} enabled alarms in database")

                if (enabledAlarms.isEmpty()) {
                    Log.d("BootReceiver", "No alarms to schedule")
                    pendingResult.finish()
                    return@launch
                }

                // Schedule each alarm
                enabledAlarms.forEachIndexed { index, alarm ->
                    try {
                        Log.d("BootReceiver", "Scheduling alarm ${index + 1}/${enabledAlarms.size}: ID=${alarm.id}")
                        AlarmScheduler.scheduleAlarm(context, alarm)
                        Log.d("BootReceiver", "  ✓ Alarm ${alarm.id} scheduled successfully")
                    } catch (e: Exception) {
                        Log.e("BootReceiver", "  ✗ Failed to schedule alarm ${alarm.id}", e)
                    }
                }

                Log.d("BootReceiver", "========================================")
                Log.d("BootReceiver", "BOOT_COMPLETED scheduling COMPLETE!")
                Log.d("BootReceiver", "========================================")

            } catch (e: Exception) {
                Log.e("BootReceiver", "FATAL ERROR during BOOT_COMPLETED scheduling", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}