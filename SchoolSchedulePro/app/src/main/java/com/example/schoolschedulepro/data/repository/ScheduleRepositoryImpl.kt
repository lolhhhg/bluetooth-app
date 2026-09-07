package com.example.schoolschedulepro.domain.repository

import com.example.schoolschedulepro.data.local.ScheduleDao
import com.example.schoolschedulepro.data.model.ScheduleItem
import com.example.schoolschedulepro.data.parser.ParseResult
import com.example.schoolschedulepro.data.parser.ParsedScheduleItem
import com.example.schoolschedulepro.data.parser.PdfParser
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

class ScheduleRepositoryImpl(
    private val scheduleDao: ScheduleDao,
    private val pdfParser: PdfParser
) : ScheduleRepository {
    
    override fun getScheduleByDay(day: Int): Flow<List<ScheduleItem>> {
        return scheduleDao.getScheduleByDay(day)
    }
    
    override suspend fun getScheduleByDaySync(day: Int): List<ScheduleItem> {
        return scheduleDao.getScheduleByDaySync(day)
    }
    
    override fun getAllSchedule(): Flow<List<ScheduleItem>> {
        return scheduleDao.getAllSchedule()
    }
    
    override suspend fun insertScheduleItem(item: ScheduleItem) {
        scheduleDao.insert(item)
    }
    
    override suspend fun insertScheduleItems(items: List<ScheduleItem>) {
        scheduleDao.insertAll(items)
    }
    
    override suspend fun deleteScheduleItem(item: ScheduleItem) {
        scheduleDao.delete(item)
    }
    
    override suspend fun deletePair(day: Int, pairNumber: Int) {
        scheduleDao.deletePair(day, pairNumber)
    }
    
    override suspend fun updateNumeratorHomework(id: Long, homework: String?) {
        scheduleDao.updateNumeratorHomework(id, homework)
    }
    
    override suspend fun updateDenominatorHomework(id: Long, homework: String?) {
        scheduleDao.updateDenominatorHomework(id, homework)
    }
    
    override suspend fun parsePdf(inputStream: InputStream): ParseResult {
        return pdfParser.parseSchedule(inputStream)
    }
    
    override suspend fun exportToJson(): String {
        // Простая реализация экспорта в JSON
        val allItems = scheduleDao.getAllSchedule()
        // В реальном приложении нужно использовать first() для Flow
        return "[]" // Заглушка, будет реализовано во ViewModel
    }
    
    override suspend fun clearAll() {
        scheduleDao.deleteAll()
    }
}

interface ScheduleRepository {
    fun getScheduleByDay(day: Int): Flow<List<ScheduleItem>>
    suspend fun getScheduleByDaySync(day: Int): List<ScheduleItem>
    fun getAllSchedule(): Flow<List<ScheduleItem>>
    suspend fun insertScheduleItem(item: ScheduleItem)
    suspend fun insertScheduleItems(items: List<ScheduleItem>)
    suspend fun deleteScheduleItem(item: ScheduleItem)
    suspend fun deletePair(day: Int, pairNumber: Int)
    suspend fun updateNumeratorHomework(id: Long, homework: String?)
    suspend fun updateDenominatorHomework(id: Long, homework: String?)
    suspend fun parsePdf(inputStream: InputStream): ParseResult
    suspend fun exportToJson(): String
    suspend fun clearAll()
}
