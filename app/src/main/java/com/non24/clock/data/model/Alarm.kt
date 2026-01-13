package com.non24.clock.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_groups")
data class AlarmGroup(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val order: Int = 0
)

@Entity(
    tableName = "alarms",
    foreignKeys = [
        ForeignKey(
            entity = AlarmGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: Long,
    val hour: Int,          // 0-25 (internal time)
    val minute: Int,        // 0-59
    val label: String = "",
    val enabled: Boolean = true,
    val repeating: Boolean = true,  // true = daily, false = one-time
    val soundUri: String? = null,   // null = default system sound
    val vibrate: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val snoozeDurationMinutes: Int = 5
)

// For displaying alarm with group info
data class AlarmWithGroup(
    val alarm: Alarm,
    val groupName: String
)
