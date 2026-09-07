package com.example.schoolschedulepro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolschedulepro.data.model.ScheduleItem
import com.example.schoolschedulepro.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val getDayScheduleUseCase: GetDayScheduleUseCase,
    private val insertScheduleItemUseCase: InsertScheduleItemUseCase,
    private val deletePairUseCase: DeletePairUseCase,
    private val updateNumeratorHomeworkUseCase: UpdateNumeratorHomeworkUseCase,
    private val updateDenominatorHomeworkUseCase: UpdateDenominatorHomeworkUseCase,
    private val parsePdfUseCase: ParsePdfUseCase,
    private val exportToJsonUseCase: ExportToJsonUseCase
) : ViewModel() {
    
    private val _selectedDay = MutableStateFlow(getCurrentDayOfWeek())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()
    
    private val _scheduleItems = MutableStateFlow<List<ScheduleItem>>(emptyList())
    val scheduleItems: StateFlow<List<ScheduleItem>> = _scheduleItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadScheduleForDay(_selectedDay.value)
    }
    
    fun selectDay(day: Int) {
        _selectedDay.value = day
        loadScheduleForDay(day)
    }
    
    fun goToToday() {
        selectDay(getCurrentDayOfWeek())
    }
    
    private fun loadScheduleForDay(day: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getDayScheduleUseCase(day).collect { items ->
                    _scheduleItems.value = items
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }
    
    fun addOrUpdatePair(item: ScheduleItem) {
        viewModelScope.launch {
            try {
                insertScheduleItemUseCase(item)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun deletePair(day: Int, pairNumber: Int) {
        viewModelScope.launch {
            try {
                deletePairUseCase(day, pairNumber)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updateNumeratorHomework(id: Long, homework: String?) {
        viewModelScope.launch {
            try {
                updateNumeratorHomeworkUseCase(id, homework)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updateDenominatorHomework(id: Long, homework: String?) {
        viewModelScope.launch {
            try {
                updateDenominatorHomeworkUseCase(id, homework)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun importPdf(inputStream: java.io.InputStream) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = parsePdfUseCase(inputStream)
                when (result) {
                    is com.example.schoolschedulepro.data.parser.ParseResult.Success -> {
                        // Вставка распарсенных элементов
                        result.items.forEach { parsed ->
                            addOrUpdatePair(
                                ScheduleItem(
                                    dayOfWeek = parsed.dayOfWeek,
                                    pairNumber = parsed.pairNumber,
                                    numeratorSubject = parsed.numeratorSubject,
                                    numeratorRoom = parsed.numeratorRoom
                                )
                            )
                        }
                        _error.value = null
                    }
                    is com.example.schoolschedulepro.data.parser.ParseResult.Error -> {
                        _error.value = result.message
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    suspend fun exportSchedule(): String {
        return try {
            exportToJsonUseCase()
        } catch (e: Exception) {
            _error.value = e.message
            "[]"
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    private fun getCurrentDayOfWeek(): Int {
        val calendar = java.util.Calendar.getInstance()
        var day = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
        if (day == 0) day = 7 // Воскресенье = 7
        return day
    }
}
