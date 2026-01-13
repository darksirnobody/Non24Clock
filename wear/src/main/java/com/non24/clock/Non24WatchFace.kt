package com.non24.clock

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import androidx.wear.watchface.*
import androidx.wear.watchface.style.CurrentUserStyleRepository
import com.google.android.gms.wearable.*
import java.time.ZonedDateTime

class Non24WatchFace : WatchFaceService() {

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = Non24Renderer(
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

class Non24Renderer(
    private val context: Context,
    surfaceHolder: SurfaceHolder,
    watchState: WatchState,
    currentUserStyleRepository: CurrentUserStyleRepository
) : Renderer.CanvasRenderer2<Non24Renderer.SharedAssets>(
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

    private val batteryPaint = Paint().apply {
        color = Color.GRAY
        textSize = 32f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    private val systemTimePaint = Paint().apply {
        color = Color.argb(128, 255, 255, 255)
        textSize = 80f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val non24TimePaint = Paint().apply {
        color = Color.WHITE
        textSize = 80f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val ambientPaint = Paint().apply {
        color = Color.WHITE
        textSize = 96f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
        isAntiAlias = true
    }

    private val iconPaint = Paint().apply {
        isAntiAlias = true
        colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
    }

    private val iconPaintDim = Paint().apply {
        isAntiAlias = true
        colorFilter = PorterDuffColorFilter(Color.argb(128, 255, 255, 255), PorterDuff.Mode.SRC_IN)
    }

    private val swapIconPaint = Paint().apply {
        isAntiAlias = true
        colorFilter = PorterDuffColorFilter(Color.argb(180, 255, 255, 255), PorterDuff.Mode.SRC_IN)
    }

    private val personIcon: Bitmap by lazy {
        val drawable = context.getDrawable(R.drawable.ic_person)!!
        val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, 48, 48)
        drawable.draw(canvas)
        bitmap
    }

    private val globeIcon: Bitmap by lazy {
        val drawable = context.getDrawable(R.drawable.ic_public)!!
        val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, 48, 48)
        drawable.draw(canvas)
        bitmap
    }

    private val swapIcon: Bitmap by lazy {
        val drawable = context.getDrawable(R.drawable.ic_swap_vert)!!
        val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, 48, 48)
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

        val (bigHour, bigMinute) = if (swapped) Pair(systemHour, systemMinute) else Pair(non24Hour, non24Minute)
        val (smallHour, smallMinute) = if (swapped) Pair(non24Hour, non24Minute) else Pair(systemHour, systemMinute)

        val bigIcon = if (swapped) globeIcon else personIcon
        val smallIcon = if (swapped) personIcon else globeIcon

        val bigTimeText = String.format("%02d:%02d", bigHour, bigMinute)
        val smallTimeText = String.format("%02d:%02d", smallHour, smallMinute)

        if (isAmbient) {
            canvas.drawText(bigTimeText, centerX, centerY + 20f, ambientPaint)
        } else {
            // Battery at top
            val batteryText = "${getBatteryLevel()}%"
            canvas.drawText(batteryText, centerX, centerY - 80f, batteryPaint)

            val smallTextWidth = systemTimePaint.measureText(smallTimeText)
            val bigTextWidth = non24TimePaint.measureText(bigTimeText)
            val iconSize = 40f
            val iconPadding = 8f

            // Small time (upper) with icon - bardziej w lewo + ikona wyżej
            val smallTotalWidth = iconSize + iconPadding + smallTextWidth
            val smallStartX = centerX - smallTotalWidth / 2 - 25f  // było -15f

            canvas.drawBitmap(
                smallIcon,
                null,
                RectF(smallStartX, centerY - 50f, smallStartX + iconSize, centerY - 10f),  // było -45f i -5f
                iconPaintDim
            )
            canvas.drawText(smallTimeText, smallStartX + iconSize + iconPadding, centerY - 10f, systemTimePaint)

// Big time (lower) with icon - bardziej w lewo
            val bigTotalWidth = iconSize + iconPadding + bigTextWidth
            val bigStartX = centerX - bigTotalWidth / 2 - 25f  // było -15f

            canvas.drawBitmap(
                bigIcon,
                null,
                RectF(bigStartX, centerY + 25f, bigStartX + iconSize, centerY + 65f),
                iconPaint
            )
            canvas.drawText(bigTimeText, bigStartX + iconSize + iconPadding, centerY + 70f, non24TimePaint)
            // Swap button on the right
            val swapSize = 36f
            val swapX = bounds.right - 50f
            val swapY = centerY - swapSize / 2
            swapButtonBounds = RectF(swapX - 10f, swapY - 10f, swapX + swapSize + 10f, swapY + swapSize + 10f)

            canvas.drawBitmap(
                swapIcon,
                null,
                RectF(swapX, swapY, swapX + swapSize, swapY + swapSize),
                swapIconPaint
            )
        }
    }

    override fun renderHighlightLayer(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, sharedAssets: SharedAssets) {
        canvas.drawColor(Color.BLACK)
    }
}