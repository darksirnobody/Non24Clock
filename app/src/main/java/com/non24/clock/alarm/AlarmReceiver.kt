package com.non24.clock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.non24.clock.ALARM_TRIGGER") {
            // Acquire wake lock
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "non24clock:alarmreceiver"
            )
            wakeLock.acquire(60 * 1000L)

            // Start foreground service which will launch activity
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra(AlarmActivity.EXTRA_ALARM_ID, intent.getLongExtra(AlarmActivity.EXTRA_ALARM_ID, -1))
                putExtra(AlarmActivity.EXTRA_ALARM_LABEL, intent.getStringExtra(AlarmActivity.EXTRA_ALARM_LABEL))
                putExtra(AlarmActivity.EXTRA_ALARM_HOUR, intent.getIntExtra(AlarmActivity.EXTRA_ALARM_HOUR, 0))
                putExtra(AlarmActivity.EXTRA_ALARM_MINUTE, intent.getIntExtra(AlarmActivity.EXTRA_ALARM_MINUTE, 0))
                putExtra(AlarmActivity.EXTRA_VIBRATE, intent.getBooleanExtra(AlarmActivity.EXTRA_VIBRATE, true))
                putExtra(AlarmActivity.EXTRA_SOUND_URI, intent.getStringExtra(AlarmActivity.EXTRA_SOUND_URI))
                putExtra(AlarmActivity.EXTRA_SNOOZE_ENABLED, intent.getBooleanExtra(AlarmActivity.EXTRA_SNOOZE_ENABLED, true))
            }
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}