package com.non24.clock.data.database

import androidx.room.*
import com.non24.clock.data.model.Alarm
import com.non24.clock.data.model.AlarmGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    
    // Alarm Groups
    @Query("SELECT * FROM alarm_groups ORDER BY `order` ASC")
    fun getAllGroups(): Flow<List<AlarmGroup>>
    
    @Query("SELECT * FROM alarm_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): AlarmGroup?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: AlarmGroup): Long
    
    @Update
    suspend fun updateGroup(group: AlarmGroup)
    
    @Delete
    suspend fun deleteGroup(group: AlarmGroup)
    
    @Query("SELECT COUNT(*) FROM alarm_groups")
    suspend fun getGroupCount(): Int
    
    // Alarms
    @Query("SELECT * FROM alarms WHERE groupId = :groupId ORDER BY hour ASC, minute ASC")
    fun getAlarmsByGroup(groupId: Long): Flow<List<Alarm>>
    
    @Query("SELECT * FROM alarms WHERE enabled = 1")
    fun getEnabledAlarms(): Flow<List<Alarm>>
    
    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): Alarm?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: Alarm): Long
    
    @Update
    suspend fun updateAlarm(alarm: Alarm)
    
    @Delete
    suspend fun deleteAlarm(alarm: Alarm)
    
    @Query("UPDATE alarms SET enabled = :enabled WHERE id = :alarmId")
    suspend fun setAlarmEnabled(alarmId: Long, enabled: Boolean)
    
    // Get all alarms with group names
    @Query("""
        SELECT a.*, g.name as groupName 
        FROM alarms a 
        INNER JOIN alarm_groups g ON a.groupId = g.id 
        ORDER BY g.`order` ASC, a.hour ASC, a.minute ASC
    """)
    fun getAllAlarmsWithGroups(): Flow<List<AlarmWithGroupTuple>>
}

data class AlarmWithGroupTuple(
    val id: Long,
    val groupId: Long,
    val hour: Int,
    val minute: Int,
    val label: String,
    val enabled: Boolean,
    val repeating: Boolean,
    val soundUri: String?,
    val vibrate: Boolean,
    val snoozeEnabled: Boolean,
    val snoozeDurationMinutes: Int,
    val groupName: String
)
