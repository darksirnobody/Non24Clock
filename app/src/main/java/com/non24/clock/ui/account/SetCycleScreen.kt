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
fun SetCycleScreen(
    clock: Non24Clock,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember { mutableIntStateOf(clock.cycleHours) }
    var selectedMinute by remember { mutableIntStateOf(clock.cycleMinutes) }
    
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
            text = stringResource(R.string.set_cycle_length),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 16.dp)
        )
        
        // Cycle picker - hours 20-30
        WheelTimePicker(
            initialHour = clock.cycleHours,
            initialMinute = clock.cycleMinutes,
            maxHours = 30,  // Max 30h cycle
            onTimeSelected = { hour, minute ->
                // Clamp hours between 20 and 30 for realistic cycles
                selectedHour = hour.coerceIn(20, 30)
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
                    clock.cycleHours = selectedHour
                    clock.cycleMinutes = selectedMinute
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
