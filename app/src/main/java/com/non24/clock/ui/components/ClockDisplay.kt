package com.non24.clock.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.non24.clock.Non24Clock
import com.non24.clock.R
import kotlinx.coroutines.delay

@Composable
fun ClockDisplay(
    clock: Non24Clock,
    showSeconds: Boolean = true,
    modifier: Modifier = Modifier
) {
    var timeText by remember { mutableStateOf(clock.getFormattedTime(showSeconds)) }
    val cycleText = clock.getCycleString()
    
    // Update every second
    LaunchedEffect(showSeconds) {
        while (true) {
            timeText = clock.getFormattedTime(showSeconds)
            delay(1000)
        }
    }
    
    val accessibilityDescription = stringResource(R.string.cd_current_time, timeText)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityDescription },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timeText,
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = stringResource(R.string.cycle_info, cycleText),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}
