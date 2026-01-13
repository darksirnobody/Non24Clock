package com.non24.clock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.non24.clock.data.database.Non24Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all enabled alarms after boot
            CoroutineScope(Dispatchers.IO).launch {
                val database = Non24Database.getDatabase(context)
                val alarms = database.alarmDao().getEnabledAlarms().first()
                
                alarms.forEach { alarm ->
                    AlarmScheduler.scheduleAlarm(context, alarm)
                }
            }
        }
    }
}
