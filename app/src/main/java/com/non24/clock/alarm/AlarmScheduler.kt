package com.non24.clock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.non24.clock.Non24Clock
import com.non24.clock.data.model.Alarm

object AlarmScheduler {

    fun scheduleAlarm(context: Context, alarm: Alarm) {
        if (!alarm.enabled) return

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val clock = Non24Clock(context)

        val triggerTime = clock.getNextOccurrence(alarm.hour, alarm.minute)

        // Use BroadcastReceiver instead of Activity directly
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.non24.clock.ALARM_TRIGGER"
            putExtra(AlarmActivity.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmActivity.EXTRA_ALARM_LABEL, alarm.label.ifEmpty { "Alarm" })
            putExtra(AlarmActivity.EXTRA_ALARM_HOUR, alarm.hour)
            putExtra(AlarmActivity.EXTRA_ALARM_MINUTE, alarm.minute)
            putExtra(AlarmActivity.EXTRA_VIBRATE, alarm.vibrate)
            putExtra(AlarmActivity.EXTRA_SOUND_URI, alarm.soundUri)
            putExtra(AlarmActivity.EXTRA_SNOOZE_ENABLED, alarm.snoozeEnabled)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
        }
    }

    fun scheduleSnooze(
        context: Context,
        alarmId: Long,
        label: String,
        hour: Int,
        minute: Int,
        vibrate: Boolean,
        soundUri: String?
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerTime = System.currentTimeMillis() + (5 * 60 * 1000)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.non24.clock.ALARM_TRIGGER"
            putExtra(AlarmActivity.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmActivity.EXTRA_ALARM_LABEL, label)
            putExtra(AlarmActivity.EXTRA_ALARM_HOUR, hour)
            putExtra(AlarmActivity.EXTRA_ALARM_MINUTE, minute)
            putExtra(AlarmActivity.EXTRA_VIBRATE, vibrate)
            putExtra(AlarmActivity.EXTRA_SOUND_URI, soundUri)
            putExtra(AlarmActivity.EXTRA_SNOOZE_ENABLED, true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt() + 10000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                    pendingIntent
                )
            }
        } else {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAllAlarms(context: Context, alarms: List<Alarm>) {
        alarms.filter { it.enabled }.forEach { alarm ->
            scheduleAlarm(context, alarm)
        }
    }
}