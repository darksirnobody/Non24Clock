package com.non24.clock.ui.alarm

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.non24.clock.Non24Clock
import com.non24.clock.R
import com.non24.clock.data.model.Alarm
import com.non24.clock.ui.components.ClockDisplay
import com.non24.clock.ui.components.WheelTimePicker

@Composable
fun AlarmEditScreen(
    clock: Non24Clock,
    alarm: Alarm?,  // null = new alarm
    groupId: Long,
    onSave: (Alarm) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null,  // null if new alarm
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    var hour by remember { mutableIntStateOf(alarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(alarm?.minute ?: 0) }
    var label by remember { mutableStateOf(alarm?.label ?: "") }
    var repeating by remember { mutableStateOf(alarm?.repeating ?: true) }
    var soundUri by remember { mutableStateOf(alarm?.soundUri) }
    var vibrate by remember { mutableStateOf(alarm?.vibrate ?: true) }
    var snoozeEnabled by remember { mutableStateOf(alarm?.snoozeEnabled ?: true) }
    
    // Ringtone picker launcher
    val ringtonePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        @Suppress("DEPRECATION")
        result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)?.let { uri ->
            soundUri = uri.toString()
        }
    }
    
    // Get ringtone name
    val soundName = remember(soundUri) {
        if (soundUri == null) {
            "Default"
        } else {
            try {
                val ringtone = RingtoneManager.getRingtone(context, Uri.parse(soundUri))
                ringtone?.getTitle(context) ?: "Custom"
            } catch (e: Exception) {
                "Custom"
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Live clock at top
        ClockDisplay(
            clock = clock,
            showSeconds = true,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time picker
        WheelTimePicker(
            initialHour = hour,
            initialMinute = minute,
            maxHours = 25,
            onTimeSelected = { h, m ->
                hour = h
                minute = m
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Settings card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Label
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Daily toggle
                SettingRow(
                    title = stringResource(R.string.daily),
                    trailing = {
                        Switch(
                            checked = repeating,
                            onCheckedChange = { repeating = it }
                        )
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Alarm sound
                SettingRow(
                    title = stringResource(R.string.alarm_sound),
                    subtitle = soundName,
                    onClick = {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.alarm_sound))
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                            putExtra(
                                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                                if (soundUri != null) Uri.parse(soundUri) else Settings.System.DEFAULT_ALARM_ALERT_URI
                            )
                        }
                        ringtonePicker.launch(intent)
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Vibration
                SettingRow(
                    title = stringResource(R.string.vibration),
                    subtitle = if (vibrate) 
                        stringResource(R.string.vibration_on) 
                    else 
                        stringResource(R.string.vibration_off),
                    trailing = {
                        Switch(
                            checked = vibrate,
                            onCheckedChange = { vibrate = it }
                        )
                    }
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Snooze
                SettingRow(
                    title = stringResource(R.string.snooze),
                    subtitle = if (snoozeEnabled) "5 min" else stringResource(R.string.snooze_off),
                    trailing = {
                        Switch(
                            checked = snoozeEnabled,
                            onCheckedChange = { snoozeEnabled = it }
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Delete button (only for existing alarms)
        if (onDelete != null) {
            TextButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(R.string.delete_alarm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
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
                    val newAlarm = Alarm(
                        id = alarm?.id ?: 0,
                        groupId = groupId,
                        hour = hour,
                        minute = minute,
                        label = label,
                        enabled = true,
                        repeating = repeating,
                        soundUri = soundUri,
                        vibrate = vibrate,
                        snoozeEnabled = snoozeEnabled,
                        snoozeDurationMinutes = 5
                    )
                    onSave(newAlarm)
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

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        trailing?.invoke()
    }
}
