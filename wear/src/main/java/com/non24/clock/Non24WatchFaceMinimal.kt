package com.non24.clock

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import androidx.wear.watchface.*
import androidx.wear.watchface.style.CurrentUserStyleRepository
import com.google.android.gms.wearable.*
import java.time.ZonedDateTime

class Non24WatchFaceMinimal : WatchFaceService() {

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = Non24MinimalRenderer(
            context = applicationContext,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            currentUserStyleRepository = currentUserStyleRepository
        )

        Wearable.getDataClient(applicationContext).addListener { dataEvents ->
            dataEvents.forEach { event ->
                if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/non24_config") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val prefs = applicationContext.getSharedPreferences("non24_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putLong("anchor_time", dataMap.getLong("anchor_time"))
                        .putLong("cycle_length_ms", dataMap.getLong("cycle_length_ms"))
                        .putBoolean("swap_clocks", dataMap.getBoolean("swap_clocks"))
                        .apply()
                }
            }
        }

        return WatchFace(
            watchFaceType = WatchFaceType.DIGITAL,
            renderer = renderer
        ).setTapListener(object : WatchFace.TapListener {
            override fun onTapEvent(tapType: Int, tapEvent: TapEvent, complicationSlot: ComplicationSlot?) {
                if (tapType == TapType.UP) {
                    renderer.handleTap(tapEvent.xPos, tapEvent.yPos)
                }
            }
        })
    }
}

class Non24MinimalRenderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    currentUserStyleRepository: CurrentUserStyleRepository
) : Renderer.CanvasRenderer2<Non24MinimalRenderer.SharedAssets>(
    surfaceHolder = surfaceHolder,
    currentUserStyleRepository = currentUserStyleRepository,
    watchState = watchState,
    canvasType = CanvasType.HARDWARE,
    interactiveDrawModeUpdateDelayMillis = 1000L,
    clearWithBackgroundTintBeforeRenderingHighlightLayer = true
) {

    class SharedAssets : Renderer.SharedAssets {
        override fun onDestroy() {}
    }

    override suspend fun createSharedAssets(): SharedAssets = SharedAssets()

    // Main time - large, centered, white
    private val mainTimePaint = Paint().apply {
        color = Color.WHITE
        textSize = 100f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    // Secondary time - small, gray
    private val secondaryTimePaint = Paint().apply {
        color = Color.GRAY
        textSize = 36f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    // Battery - gray, bottom
    private val batteryPaint = Paint().apply {
        color = Color.GRAY
        textSize = 36f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    // Ambient mode
    private val ambientPaint = Paint().apply {
        color = Color.WHITE
        textSize = 100f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    private val iconPaint = Paint().apply {
        isAntiAlias = true
        colorFilter = PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN)
    }

    private val personIcon: Bitmap by lazy {
        val drawable = context.getDrawable(R.drawable.ic_person)!!
        val bitmap = Bitmap.createBitmap(36, 36, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, 36, 36)
        drawable.draw(canvas)
        bitmap
    }

    private val globeIcon: Bitmap by lazy {
        val drawable = context.getDrawable(R.drawable.ic_public)!!
        val bitmap = Bitmap.createBitmap(36, 36, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, 36, 36)
        drawable.draw(canvas)
        bitmap
    }

    private val swapIcon: Bitmap by lazy {
        val drawable = context.getDrawable(R.drawable.ic_swap_vert)!!
        val bitmap = Bitmap.createBitmap(36, 36, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, 36, 36)
        drawable.draw(canvas)
        bitmap
    }

    private var swapButtonBounds = RectF()

    private fun getNon24Time(): Pair<Int, Int> {
        val prefs = context.getSharedPreferences("non24_prefs", Context.MODE_PRIVATE)
        val anchorTime = prefs.getLong("anchor_time", System.currentTimeMillis())
        val cycleLengthMs = prefs.getLong("cycle_length_ms", 25 * 60 * 60 * 1000L)

        val elapsed = System.currentTimeMillis() - anchorTime
        val cyclePosition = elapsed % cycleLengthMs
        val totalMinutes = (cyclePosition / 60000).toInt()

        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60

        return Pair(hours, minutes)
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as android.os.BatteryManager
        return batteryManager.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun isSwapped(): Boolean {
        val prefs = context.getSharedPreferences("non24_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("swap_clocks", false)
    }

    private fun toggleSwap() {
        val prefs = context.getSharedPreferences("non24_prefs", Context.MODE_PRIVATE)
        val current = prefs.getBoolean("swap_clocks", false)
        prefs.edit().putBoolean("swap_clocks", !current).apply()
    }

    fun handleTap(x: Int, y: Int) {
        if (swapButtonBounds.contains(x.toFloat(), y.toFloat())) {
            toggleSwap()
            invalidate()
        }
    }

    override fun render(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        canvas.drawColor(Color.BLACK)

        val centerX = bounds.exactCenterX()
        val centerY = bounds.exactCenterY()

        val isAmbient = renderParameters.drawMode == DrawMode.AMBIENT

        val (non24Hour, non24Minute) = getNon24Time()
        val systemHour = zonedDateTime.hour
        val systemMinute = zonedDateTime.minute

        val swapped = isSwapped()

        // Determine which time is main (big) and which is secondary (small)
        val (mainHour, mainMinute) = if (swapped) Pair(systemHour, systemMinute) else Pair(non24Hour, non24Minute)
        val (secondaryHour, secondaryMinute) = if (swapped) Pair(non24Hour, non24Minute) else Pair(systemHour, systemMinute)
        val secondaryIcon = if (swapped) personIcon else globeIcon

        val mainTimeText = String.format("%02d:%02d", mainHour, mainMinute)
        val secondaryTimeText = String.format("%02d:%02d", secondaryHour, secondaryMinute)

        if (isAmbient) {
            // Ambient: just main time centered
            canvas.drawText(mainTimeText, centerX, centerY + 20f, ambientPaint)
        } else {
            // === TOP ROW: icon + secondary time + swap icon ===
            val topY = centerY - 90f
            val iconSize = 32f
            val spacing = 5f

            val timeWidth = secondaryTimePaint.measureText(secondaryTimeText)
            val totalTopWidth = iconSize + spacing + timeWidth + spacing + iconSize
            val topStartX = centerX - totalTopWidth / 2

            // Secondary icon (globe or person)
            canvas.drawBitmap(
                secondaryIcon,
                null,
                RectF(topStartX, topY - iconSize / 2, topStartX + iconSize, topY + iconSize / 2),
                iconPaint
            )

            // Secondary time
            val timeX = topStartX + iconSize + spacing
            canvas.drawText(secondaryTimeText, timeX, topY + 12f, secondaryTimePaint)

            // Swap icon (with tap area)
            val swapX = timeX + timeWidth + spacing
            canvas.drawBitmap(
                swapIcon,
                null,
                RectF(swapX, topY - iconSize / 2, swapX + iconSize, topY + iconSize / 2),
                iconPaint
            )

            // Tap bounds for swap (slightly larger for easier tapping)
            swapButtonBounds = RectF(swapX - 15f, topY - iconSize / 2 - 15f, swapX + iconSize + 15f, topY + iconSize / 2 + 15f)

            // === CENTER: Main time, large, no icon ===
            canvas.drawText(mainTimeText, centerX, centerY + 35f, mainTimePaint)

            // === BOTTOM: Battery ===
            val batteryText = "${getBatteryLevel()}%"
            canvas.drawText(batteryText, centerX, centerY + 115f, batteryPaint)
        }
    }

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        canvas.drawColor(Color.BLACK)
    }
}