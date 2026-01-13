package com.non24.clock.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.non24.clock.Non24Clock
import com.non24.clock.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable

@Composable
fun CalendarScreen(
    clock: Non24Clock,
    modifier: Modifier = Modifier
) {
    var isWeekView by remember { mutableStateOf(false) }
    var dayOffset by remember { mutableIntStateOf(0) }
    var weekOffset by remember { mutableIntStateOf(0) }

    val today = LocalDate.now()
    val selectedDate = today.plusDays(dayOffset.toLong())
    val startOfWeek = today.plusWeeks(weekOffset.toLong()).minusDays(today.dayOfWeek.value.toLong() - 1)

    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        // View toggle
        ViewToggle(
            isWeekView = isWeekView,
            onToggle = { isWeekView = it }
        )

        // Navigation header
        if (isWeekView) {
            WeekNavigationHeader(
                startOfWeek = startOfWeek,
                onPrevious = { weekOffset-- },
                onNext = { weekOffset++ },
                onToday = { weekOffset = 0 }
            )
        } else {
            DayNavigationHeader(
                date = selectedDate,
                onPrevious = { dayOffset-- },
                onNext = { dayOffset++ },
                onToday = { dayOffset = 0 }
            )
        }

        // Calendar content
        if (isWeekView) {
            WeekView(
                clock = clock,
                startOfWeek = startOfWeek,
                today = today,
                onDayClick = { clickedDay ->
                    // Oblicz offset od dziś
                    dayOffset = (clickedDay.toEpochDay() - today.toEpochDay()).toInt()
                    isWeekView = false  // Przełącz na widok dnia
                }
            )
        } else {
            DayView(
                clock = clock,
                date = selectedDate,
                isToday = selectedDate == today
            )
        }
    }
}

@Composable
private fun ViewToggle(
    isWeekView: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        FilterChip(
            selected = !isWeekView,
            onClick = { onToggle(false) },
            label = { Text(stringResource(R.string.calendar_day)) },
            leadingIcon = {
                Icon(
                    Icons.Filled.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilterChip(
            selected = isWeekView,
            onClick = { onToggle(true) },
            label = { Text(stringResource(R.string.calendar_week)) },
            leadingIcon = {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
private fun DayNavigationHeader(
    date: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous day")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = date.format(formatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (date != LocalDate.now()) {
                TextButton(onClick = onToday) {
                    Text(stringResource(R.string.calendar_today))
                }
            }
        }

        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next day")
        }
    }
}

@Composable
private fun WeekNavigationHeader(
    startOfWeek: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit
) {
    val endOfWeek = startOfWeek.plusDays(6)
    val formatter = DateTimeFormatter.ofPattern("d-", Locale.getDefault())
    val formatterEnd = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous week")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${startOfWeek.format(formatter)}${endOfWeek.format(formatterEnd)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onToday) {
                Text(stringResource(R.string.calendar_today))
            }
        }

        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronRight, contentDescription = "Next week")
        }
    }
}

// ==================== DAY VIEW ====================

@Composable
private fun DayView(
    clock: Non24Clock,
    date: LocalDate,
    isToday: Boolean
) {
    val listState = rememberLazyListState()

    // Current time for the line
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    // Update current time every minute
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000)
        }
    }

    // Scroll to current hour on first load if today
    LaunchedEffect(isToday) {
        if (isToday) {
            listState.scrollToItem(maxOf(0, currentTime.hour - 2))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(24) { hour ->
                DayHourRow(
                    clock = clock,
                    date = date,
                    hour = hour,
                    isToday = isToday,
                    currentTime = currentTime
                )
            }
            // Extra space at bottom
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun DayHourRow(
    clock: Non24Clock,
    date: LocalDate,
    hour: Int,
    isToday: Boolean,
    currentTime: LocalTime
) {
    val hourHeight = 60.dp
    val non24Time = calculateNon24Time(clock, date, hour)
    val isSleepHour = isInSleepWindow(clock, non24Time.first, non24Time.second)

    val isCurrentHour = isToday && currentTime.hour == hour
    val minuteProgress = if (isCurrentHour) currentTime.minute / 60f else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(hourHeight)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left axis - system time
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = String.format("%02d:00", hour),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .offset(y = (-6).dp)
                        .padding(end = 4.dp)
                )
            }

            // Hour block - rozciąga się na całą szerokość
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        if (isSleepHour) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else Color.Transparent
                    )
            ) {
                // Top border line
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.TopStart),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Sleep/Wake icon in top-right corner
                Icon(
                    imageVector = if (isSleepHour) Icons.Filled.Bedtime else Icons.Filled.WbSunny,
                    contentDescription = if (isSleepHour) "Sleep" else "Awake",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp),
                    tint = if (isSleepHour)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )

                // Current time line inside the block
                if (isCurrentHour) {
                    CurrentTimeLine(
                        progress = minuteProgress,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Right axis - non-24 time
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = String.format("%02d:%02d", non24Time.first, non24Time.second),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .offset(y = (-6).dp)
                        .padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CurrentTimeLine(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.error

    Canvas(modifier = modifier) {
        val y = size.height * progress

        // Draw line first
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 2.dp.toPx()
        )

        // Draw dot on the left edge
        drawCircle(
            color = lineColor,
            radius = 5.dp.toPx(),
            center = Offset(0f, y)
        )
    }
}

// ==================== WEEK VIEW ====================

@Composable
private fun WeekView(
    clock: Non24Clock,
    startOfWeek: LocalDate,
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    val days = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    val listState = rememberLazyListState()

    // Current time
    var currentTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            delay(60_000)
        }
    }

    // Scroll to current hour
    val todayInWeek = days.contains(today)
    LaunchedEffect(todayInWeek) {
        if (todayInWeek) {
            listState.scrollToItem(maxOf(0, currentTime.hour - 2))
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
// Day headers
        item {
            WeekDayHeaders(
                days = days,
                today = today,
                onDayClick = onDayClick
            )
        }

        // Hour rows
        items(24) { hour ->
            WeekHourRow(
                clock = clock,
                hour = hour,
                days = days,
                today = today,
                currentTime = currentTime
            )
        }

        // Extra space
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun WeekDayHeaders(
    days: List<LocalDate>,
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp)
    ) {
        days.forEach { day ->
            val isToday = day == today
            val dayName = day.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isToday) MaterialTheme.colorScheme.primaryContainer
                        else Color.Transparent
                    )
                    .clickable { onDayClick(day) }
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = dayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = day.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun WeekHourRow(
    clock: Non24Clock,
    hour: Int,
    days: List<LocalDate>,
    today: LocalDate,
    currentTime: LocalTime
) {
    val rowHeight = 40.dp
    val isCurrentHour = today in days && currentTime.hour == hour
    val todayIndex = days.indexOf(today)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Hour label - pełna godzina
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = String.format("%02d:00", hour),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .offset(y = (-5).dp)
                        .padding(end = 4.dp)
                )
            }

            // Day cells
            days.forEachIndexed { index, day ->
                val non24Time = calculateNon24Time(clock, day, hour)
                val isSleepHour = isInSleepWindow(clock, non24Time.first, non24Time.second)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(0.5.dp)
                        .background(
                            if (isSleepHour) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSleepHour) Icons.Filled.Bedtime else Icons.Filled.WbSunny,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isSleepHour)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Current time line across today's column
        if (isCurrentHour && todayIndex >= 0) {
            val minuteProgress = currentTime.minute / 60f
            WeekCurrentTimeLine(
                progress = minuteProgress,
                dayIndex = todayIndex,
                totalDays = days.size,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp)
            )
        }
    }
}

@Composable
private fun WeekCurrentTimeLine(
    progress: Float,
    dayIndex: Int,
    totalDays: Int,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.error

    Canvas(modifier = modifier) {
        val cellWidth = size.width / totalDays
        val y = size.height * progress
        val startX = dayIndex * cellWidth
        val endX = (dayIndex + 1) * cellWidth

        // Draw dot
        drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = Offset(startX, y)
        )

        // Draw line across today's column
        drawLine(
            color = lineColor,
            start = Offset(startX, y),
            end = Offset(endX, y),
            strokeWidth = 2.dp.toPx()
        )
    }
}

// ==================== HELPER FUNCTIONS ====================

private fun calculateNon24Time(clock: Non24Clock, date: LocalDate, systemHour: Int): Pair<Int, Int> {
    val dateTime = date.atTime(systemHour, 0, 0)
    val timestamp = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val elapsed = timestamp - clock.epochTime
    val cyclePosition = ((elapsed % clock.cycleLengthMillis) + clock.cycleLengthMillis) % clock.cycleLengthMillis
    val totalMinutes = ((cyclePosition + 30000) / 60000).toInt()  // +30s dla zaokrąglenia

    var hours = (totalMinutes / 60) % 24
    val minutes = totalMinutes % 60

    if (hours < 0) hours += 24

    return Pair(hours, minutes)
}

private fun isInSleepWindow(clock: Non24Clock, hour: Int, minute: Int): Boolean {
    val wakeHour = clock.wakeHour
    val wakeMinute = clock.wakeMinute
    val sleepDurationMinutes = clock.sleepHours * 60 + clock.sleepMinutes

    // Calculate sleep start time (wake time - sleep duration)
    val wakeTimeMinutes = wakeHour * 60 + wakeMinute
    var sleepStartMinutes = wakeTimeMinutes - sleepDurationMinutes
    if (sleepStartMinutes < 0) sleepStartMinutes += 24 * 60

    val currentMinutes = hour * 60 + minute

    // Check if current time is in sleep window
    return if (sleepStartMinutes < wakeTimeMinutes) {
        // Sleep window doesn't cross midnight
        currentMinutes >= sleepStartMinutes && currentMinutes < wakeTimeMinutes
    } else {
        // Sleep window crosses midnight
        currentMinutes >= sleepStartMinutes || currentMinutes < wakeTimeMinutes
    }
}