package com.example.schoolschedulepro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.schoolschedulepro.data.model.ScheduleItem
import com.example.schoolschedulepro.data.model.VkImage

@Database(
    entities = [ScheduleItem::class, VkImage::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun vkImageDao(): VkImageDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "school_schedule_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
