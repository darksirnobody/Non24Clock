package com.non24.clock.ui.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.non24.clock.Non24Clock
import com.non24.clock.R
import com.non24.clock.ui.components.ClockDisplay

@Composable
fun AccountScreen(
    clock: Non24Clock,
    onNavigateToSetTime: () -> Unit,
    onNavigateToSetCycle: () -> Unit,
    onNavigateToSetSleep: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Main clock display
        ClockDisplay(
            clock = clock,
            showSeconds = true,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Settings card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column {
                // Set your time
                SettingsItem(
                    title = stringResource(R.string.set_your_time),
                    subtitle = stringResource(R.string.set_your_time_desc),
                    onClick = onNavigateToSetTime
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // Set cycle length
                SettingsItem(
                    title = stringResource(R.string.set_cycle_length),
                    subtitle = stringResource(R.string.set_cycle_length_desc),
                    onClick = onNavigateToSetCycle
                )

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )

                // Set sleep window
                SettingsItem(
                    title = stringResource(R.string.set_sleep_window),
                    subtitle = stringResource(R.string.set_sleep_window_desc),
                    onClick = onNavigateToSetSleep
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { role = Role.Button },
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}