package com.non24.clock

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.non24.clock.sync.WearSyncService

class Non24Clock(private val context: Context) {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        const val PREF_EPOCH_TIME = "epoch_time"
        const val PREF_CYCLE_HOURS = "cycle_hours"
        const val PREF_CYCLE_MINUTES = "cycle_minutes"
        const val PREF_WAKE_HOUR = "wake_hour"
        const val PREF_WAKE_MINUTE = "wake_minute"
        const val PREF_SLEEP_HOURS = "sleep_hours"
        const val PREF_SLEEP_MINUTES = "sleep_minutes"

        const val DEFAULT_CYCLE_HOURS = 25
        const val DEFAULT_CYCLE_MINUTES = 0
        const val DEFAULT_WAKE_HOUR = 8
        const val DEFAULT_WAKE_MINUTE = 0
        const val DEFAULT_SLEEP_HOURS = 8
        const val DEFAULT_SLEEP_MINUTES = 0
    }

    var epochTime: Long
        get() = prefs.getLong(PREF_EPOCH_TIME, System.currentTimeMillis())
        set(value) {
            prefs.edit().putLong(PREF_EPOCH_TIME, value).apply()
            syncToWatch()
        }

    var cycleHours: Int
        get() = prefs.getInt(PREF_CYCLE_HOURS, DEFAULT_CYCLE_HOURS)
        set(value) {
            prefs.edit().putInt(PREF_CYCLE_HOURS, value).apply()
            syncToWatch()
        }

    var cycleMinutes: Int
        get() = prefs.getInt(PREF_CYCLE_MINUTES, DEFAULT_CYCLE_MINUTES)
        set(value) {
            prefs.edit().putInt(PREF_CYCLE_MINUTES, value).apply()
            syncToWatch()
        }

    var wakeHour: Int
        get() = prefs.getInt(PREF_WAKE_HOUR, DEFAULT_WAKE_HOUR)
        set(value) = prefs.edit().putInt(PREF_WAKE_HOUR, value).apply()

    var wakeMinute: Int
        get() = prefs.getInt(PREF_WAKE_MINUTE, DEFAULT_WAKE_MINUTE)
        set(value) = prefs.edit().putInt(PREF_WAKE_MINUTE, value).apply()

    var sleepHours: Int
        get() = prefs.getInt(PREF_SLEEP_HOURS, DEFAULT_SLEEP_HOURS)
        set(value) = prefs.edit().putInt(PREF_SLEEP_HOURS, value).apply()

    var sleepMinutes: Int
        get() = prefs.getInt(PREF_SLEEP_MINUTES, DEFAULT_SLEEP_MINUTES)
        set(value) = prefs.edit().putInt(PREF_SLEEP_MINUTES, value).apply()

    val cycleLengthMillis: Long
        get() = (cycleHours * 3600L + cycleMinutes * 60L) * 1000L

    private fun syncToWatch() {
        WearSyncService.syncToWatch(context, epochTime, cycleLengthMillis, false)
    }

    fun setCurrentInternalTime(hours: Int, minutes: Int) {
        // Sync seconds with system time so CB seconds always match CS seconds
        val systemSeconds = (System.currentTimeMillis() / 1000) % 60
        val internalMillis = (hours * 3600L + minutes * 60L + systemSeconds) * 1000L
        epochTime = System.currentTimeMillis() - internalMillis
    }

    fun getInternalTime(): InternalTime {
        val now = System.currentTimeMillis()
        val elapsed = now - epochTime
        val cycleMillis = elapsed % cycleLengthMillis

        val totalSeconds = cycleMillis / 1000
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()

        return InternalTime(hours, minutes, seconds)
    }

    fun getFormattedTime(showSeconds: Boolean = false): String {
        val time = getInternalTime()
        return if (showSeconds) {
            String.format("%02d:%02d:%02d", time.hours, time.minutes, time.seconds)
        } else {
            String.format("%02d:%02d", time.hours, time.minutes)
        }
    }

    fun getCycleString(): String {
        return "${cycleHours}h ${cycleMinutes}m"
    }

    fun getSleepWindowString(): String {
        return "Wake ${wakeHour}:${String.format("%02d", wakeMinute)}, Sleep ${sleepHours}h ${sleepMinutes}m"
    }

    fun getNextOccurrence(targetHours: Int, targetMinutes: Int): Long {
        val now = System.currentTimeMillis()
        val elapsed = now - epochTime
        val currentCycleMillis = elapsed % cycleLengthMillis

        val targetMillis = (targetHours * 3600L + targetMinutes * 60L) * 1000L

        return if (targetMillis > currentCycleMillis) {
            now + (targetMillis - currentCycleMillis)
        } else {
            now + (cycleLengthMillis - currentCycleMillis) + targetMillis
        }
    }

    data class InternalTime(val hours: Int, val minutes: Int, val seconds: Int)
}