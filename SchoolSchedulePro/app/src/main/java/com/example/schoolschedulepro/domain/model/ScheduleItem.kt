package com.example.schoolschedulepro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 1 = Monday, 7 = Sunday
    val pairNumber: Int, // 1-5
    val numeratorSubject: String? = null,
    val denominatorSubject: String? = null,
    val numeratorRoom: String? = null,
    val denominatorRoom: String? = null,
    val numeratorHomework: String? = null,
    val denominatorHomework: String? = null
)
