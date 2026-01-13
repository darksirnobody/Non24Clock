package com.non24.clock.ui.alarm

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.non24.clock.R
import com.non24.clock.data.model.Alarm
import com.non24.clock.data.model.AlarmGroup

@Composable
fun AlarmScreen(
    groups: List<AlarmGroup>,
    alarms: List<Alarm>,
    selectedGroupId: Long,
    onGroupSelected: (Long) -> Unit,
    onAddGroup: () -> Unit,
    onRenameGroup: (AlarmGroup) -> Unit,
    onDeleteGroup: (AlarmGroup) -> Unit,
    onAddAlarm: () -> Unit,
    onAlarmClick: (Alarm) -> Unit,
    onAlarmToggle: (Alarm, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var groupToRename by remember { mutableStateOf<AlarmGroup?>(null) }
    var newGroupName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Groups row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            groups.forEach { group ->
                val groupAlarmCount = if (group.id == selectedGroupId) alarms.size else 0

                GroupChip(
                    group = group,
                    isSelected = group.id == selectedGroupId,
                    alarmCount = groupAlarmCount,
                    isLastGroup = groups.size == 1,
                    onClick = { onGroupSelected(group.id) },
                    onLongClick = {
                        groupToRename = group
                        newGroupName = group.name
                        showRenameDialog = true
                    },
                    onDelete = { onDeleteGroup(group) }
                )
            }

            // Add group button (only show if less than 5 groups)
            if (groups.size < 5) {
                IconButton(
                    onClick = onAddGroup,
                    modifier = Modifier.semantics {
                        contentDescription = "Add new group"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alarm header with add button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.nav_alarm),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = onAddAlarm,
                modifier = Modifier.semantics {
                    contentDescription = "Add new alarm"
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Alarms list
        if (alarms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No alarms yet",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alarms, key = { it.id }) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onClick = { onAlarmClick(alarm) },
                        onToggle = { enabled -> onAlarmToggle(alarm, enabled) }
                    )
                }
            }
        }
    }

    // Rename/Delete dialog
    if (showRenameDialog && groupToRename != null) {
        val canDelete = groupToRename!!.id != 1L &&
                (if (groupToRename!!.id == selectedGroupId) alarms.isEmpty() else true)

        AlertDialog(
            onDismissRequest = {
                showRenameDialog = false
                groupToRename = null
            },
            title = { Text(groupToRename!!.name) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text(stringResource(R.string.group_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (canDelete) {
                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = {
                                groupToRename?.let { onDeleteGroup(it) }
                                showRenameDialog = false
                                groupToRename = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.delete_group),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        groupToRename?.let {
                            onRenameGroup(it.copy(name = newGroupName))
                        }
                        showRenameDialog = false
                        groupToRename = null
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                    groupToRename = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupChip(
    group: AlarmGroup,
    isSelected: Boolean,
    alarmCount: Int,
    isLastGroup: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val cdSelected = if (isSelected) "selected" else "not selected"

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 0.dp else 1.dp,
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            )
            .semantics { contentDescription = "${group.name}, $cdSelected" }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.Folder else Icons.Outlined.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = group.name,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: Alarm,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val timeText = String.format("%02d:%02d", alarm.hour, alarm.minute)
    val repeatText = if (alarm.repeating) stringResource(R.string.daily) else stringResource(R.string.one_time)
    val toggleDesc = stringResource(R.string.cd_toggle_alarm)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                if (alarm.label.isNotEmpty()) {
                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Text(
                    text = timeText,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Light,
                    color = if (alarm.enabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = repeatText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.semantics {
                        contentDescription = toggleDesc
                    }
                )
            }
        }
    }
}