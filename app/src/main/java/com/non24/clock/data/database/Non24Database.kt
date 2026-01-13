package com.non24.clock.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.non24.clock.data.model.Alarm
import com.non24.clock.data.model.AlarmGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Alarm::class, AlarmGroup::class],
    version = 1,
    exportSchema = false
)
abstract class Non24Database : RoomDatabase() {
    
    abstract fun alarmDao(): AlarmDao
    
    companion object {
        @Volatile
        private var INSTANCE: Non24Database? = null
        
        fun getDatabase(context: Context): Non24Database {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Non24Database::class.java,
                    "non24_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Create default group on first launch
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.alarmDao()?.insertGroup(
                                AlarmGroup(
                                    name = "Alarms", // Will be localized in UI
                                    order = 0
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
