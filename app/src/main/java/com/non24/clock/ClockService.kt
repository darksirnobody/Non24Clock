package com.non24.clock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class ClockService : Service() {
    
    companion object {
        const val CHANNEL_ID = "non24_clock_channel"
        const val NOTIFICATION_ID = 1
        var isRunning = false
    }
    
    private lateinit var clock: Non24Clock
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val handler = Handler(Looper.getMainLooper())
    
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateNotificationTime()
            ClockWidget.updateAllWidgets(this@ClockService)
            handler.postDelayed(this, 60000) // Update every minute
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        clock = Non24Clock(this)
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
        createNotificationBuilder()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        startForeground(NOTIFICATION_ID, buildNotification())
        handler.post(updateRunnable)
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        handler.removeCallbacks(updateRunnable)
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Non-24 Clock",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows your internal time"
            setShowBadge(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun createNotificationBuilder() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
    }
    
    private fun buildNotification(): Notification {
        return notificationBuilder
            .setContentTitle(clock.getFormattedTime())
            .setContentText("non-24 (${clock.getCycleString()} cycle)")
            .build()
    }
    
    private fun updateNotificationTime() {
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }
}
