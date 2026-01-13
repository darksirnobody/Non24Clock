package com.non24.clock

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.edit
import java.util.Calendar

class ClockWidget : AppWidgetProvider() {

    companion object {
        const val ACTION_UPDATE = "com.non24.clock.ACTION_UPDATE_WIDGET"
        const val PREFS_NAME = "widget_prefs"
        const val PREF_BACKGROUND = "widget_background"
        const val PREF_SIZE = "widget_size"
        const val PREF_SHOW_LABEL = "widget_show_label"

        const val BG_DARK = "dark"
        const val BG_LIGHT = "light"
        const val BG_TRANSPARENT = "transparent"

        const val SIZE_SMALL = "small"
        const val SIZE_MEDIUM = "medium"
        const val SIZE_LARGE = "large"
        const val SIZE_XLARGE = "xlarge"

        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, ClockWidget::class.java).apply {
                action = ACTION_UPDATE
            }
            context.sendBroadcast(intent)
        }

        fun getBackground(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(PREF_BACKGROUND, BG_DARK) ?: BG_DARK
        }

        fun setBackground(context: Context, background: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit { putString(PREF_BACKGROUND, background) }
            updateAllWidgets(context)
        }

        fun getSize(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(PREF_SIZE, SIZE_MEDIUM) ?: SIZE_MEDIUM
        }

        fun setSize(context: Context, size: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit { putString(PREF_SIZE, size) }
            updateAllWidgets(context)
        }

        fun getShowLabel(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(PREF_SHOW_LABEL, true)
        }

        fun setShowLabel(context: Context, show: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit { putBoolean(PREF_SHOW_LABEL, show) }
            updateAllWidgets(context)
        }

        private fun scheduleNextUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ClockWidget::class.java).apply {
                action = ACTION_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 1)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            alarmManager.setExact(
                AlarmManager.RTC,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        private fun cancelUpdate(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ClockWidget::class.java).apply {
                action = ACTION_UPDATE
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleNextUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, ClockWidget::class.java)
            )
            if (widgetIds.isNotEmpty()) {
                onUpdate(context, appWidgetManager, widgetIds)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleNextUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdate(context)
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val clock = Non24Clock(context)
        val background = getBackground(context)
        val size = getSize(context)
        val showLabel = getShowLabel(context)

        val layoutId = when (background) {
            BG_LIGHT -> R.layout.widget_clock_light
            BG_TRANSPARENT -> R.layout.widget_clock_transparent
            else -> R.layout.widget_clock
        }

        val views = RemoteViews(context.packageName, layoutId)
        views.setTextViewText(R.id.widgetTime, clock.getFormattedTime())
        views.setTextViewText(R.id.widgetLabel, "non-24")

        views.setViewVisibility(R.id.widgetLabel, if (showLabel) View.VISIBLE else View.GONE)

        val (timeSize, labelSize) = when (size) {
            SIZE_SMALL -> Pair(24f, 10f)
            SIZE_LARGE -> Pair(48f, 16f)
            SIZE_XLARGE -> Pair(64f, 20f)
            else -> Pair(32f, 12f)
        }

        views.setTextViewTextSize(R.id.widgetTime, TypedValue.COMPLEX_UNIT_SP, timeSize)
        views.setTextViewTextSize(R.id.widgetLabel, TypedValue.COMPLEX_UNIT_SP, labelSize)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}