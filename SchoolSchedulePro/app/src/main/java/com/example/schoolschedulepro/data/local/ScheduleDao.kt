package com.example.schoolschedulepro.data.local

import androidx.room.*
import com.example.schoolschedulepro.data.model.ScheduleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule WHERE dayOfWeek = :day ORDER BY pairNumber")
    fun getScheduleByDay(day: Int): Flow<List<ScheduleItem>>
    
    @Query("SELECT * FROM schedule WHERE dayOfWeek = :day ORDER BY pairNumber")
    suspend fun getScheduleByDaySync(day: Int): List<ScheduleItem>
    
    @Query("SELECT * FROM schedule ORDER BY dayOfWeek, pairNumber")
    fun getAllSchedule(): Flow<List<ScheduleItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ScheduleItem)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ScheduleItem>)
    
    @Delete
    suspend fun delete(item: ScheduleItem)
    
    @Query("DELETE FROM schedule WHERE dayOfWeek = :day AND pairNumber = :pairNumber")
    suspend fun deletePair(day: Int, pairNumber: Int)
    
    @Query("UPDATE schedule SET numeratorHomework = :homework WHERE id = :id")
    suspend fun updateNumeratorHomework(id: Long, homework: String?)
    
    @Query("UPDATE schedule SET denominatorHomework = :homework WHERE id = :id")
    suspend fun updateDenominatorHomework(id: Long, homework: String?)
    
    @Query("DELETE FROM schedule")
    suspend fun deleteAll()
}
