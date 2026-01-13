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
fun SetTimeScreen(
    clock: Non24Clock,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTime = clock.getInternalTime()
    var selectedHour by remember { mutableIntStateOf(currentTime.hours) }
    var selectedMinute by remember { mutableIntStateOf(currentTime.minutes) }
    
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
        
        // Title
        Text(
            text = stringResource(R.string.set_your_time),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        )
        
        // Time picker
        WheelTimePicker(
            initialHour = currentTime.hours,
            initialMinute = currentTime.minutes,
            maxHours = 25,  // Non-24 can have >24h days
            onTimeSelected = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
            },
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp)
        )
        
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
                    clock.setCurrentInternalTime(selectedHour, selectedMinute)
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
