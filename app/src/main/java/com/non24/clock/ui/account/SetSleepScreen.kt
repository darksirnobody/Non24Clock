package com.non24.clock.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.non24.clock.Non24Clock
import com.non24.clock.R
import com.non24.clock.ui.components.ClockDisplay
import com.non24.clock.ui.components.WheelTimePicker

@Composable
fun SetSleepScreen(
    clock: Non24Clock,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var wakeHour by remember { mutableIntStateOf(clock.wakeHour) }
    var wakeMinute by remember { mutableIntStateOf(clock.wakeMinute) }
    var sleepHours by remember { mutableIntStateOf(clock.sleepHours) }
    var sleepMinutes by remember { mutableIntStateOf(clock.sleepMinutes) }

    var showWakePicker by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Live clock display at top
        ClockDisplay(
            clock = clock,
            showSeconds = true,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Toggle between wake time and sleep duration
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                selected = showWakePicker,
                onClick = { showWakePicker = true },
                label = { Text(stringResource(R.string.wake_time)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = !showWakePicker,
                onClick = { showWakePicker = false },
                label = { Text(stringResource(R.string.sleep_duration)) }
            )
        }

        // Title
        Text(
            text = if (showWakePicker) stringResource(R.string.wake_time) else stringResource(R.string.sleep_duration),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        )

        if (showWakePicker) {
            // Wake time picker (0-23 hours)
            WheelTimePicker(
                initialHour = wakeHour,
                initialMinute = wakeMinute,
                maxHours = 23,
                onTimeSelected = { hour, minute ->
                    wakeHour = hour
                    wakeMinute = minute
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
            )
        } else {
            // Sleep duration picker (1-16 hours)
            WheelTimePicker(
                initialHour = sleepHours,
                initialMinute = sleepMinutes,
                maxHours = 16,
                onTimeSelected = { hour, minute ->
                    sleepHours = hour.coerceAtLeast(1) // Minimum 1 hour sleep
                    sleepMinutes = minute
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 16.dp)
            )
        }

        // Bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            TextButton(
                onClick = {
                    clock.wakeHour = wakeHour
                    clock.wakeMinute = wakeMinute
                    clock.sleepHours = sleepHours
                    clock.sleepMinutes = sleepMinutes
                    onSave()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}