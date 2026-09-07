package com.example.schoolschedulepro.domain.usecase

import com.example.schoolschedulepro.data.model.ScheduleItem
import com.example.schoolschedulepro.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow

class GetDayScheduleUseCase(private val repository: ScheduleRepository) {
    operator fun invoke(day: Int): Flow<List<ScheduleItem>> {
        return repository.getScheduleByDay(day)
    }
}

class GetAllScheduleUseCase(private val repository: ScheduleRepository) {
    operator fun invoke(): Flow<List<ScheduleItem>> {
        return repository.getAllSchedule()
    }
}

class InsertScheduleItemUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(item: ScheduleItem) {
        repository.insertScheduleItem(item)
    }
}

class InsertScheduleItemsUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(items: List<ScheduleItem>) {
        repository.insertScheduleItems(items)
    }
}

class DeletePairUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(day: Int, pairNumber: Int) {
        repository.deletePair(day, pairNumber)
    }
}

class UpdateNumeratorHomeworkUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(id: Long, homework: String?) {
        repository.updateNumeratorHomework(id, homework)
    }
}

class UpdateDenominatorHomeworkUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(id: Long, homework: String?) {
        repository.updateDenominatorHomework(id, homework)
    }
}

class ParsePdfUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(inputStream: java.io.InputStream) = 
        repository.parsePdf(inputStream)
}

class ExportToJsonUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke(): String {
        return repository.exportToJson()
    }
}

class ClearScheduleUseCase(private val repository: ScheduleRepository) {
    suspend operator fun invoke() {
        repository.clearAll()
    }
}
