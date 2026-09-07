package com.example.schoolschedulepro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolschedulepro.data.model.ScheduleItem
import com.example.schoolschedulepro.domain.usecase.GetAllScheduleUseCase
import com.example.schoolschedulepro.domain.usecase.UpdateDenominatorHomeworkUseCase
import com.example.schoolschedulepro.domain.usecase.UpdateNumeratorHomeworkUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeworkViewModel(
    private val getAllScheduleUseCase: GetAllScheduleUseCase,
    private val updateNumeratorHomeworkUseCase: UpdateNumeratorHomeworkUseCase,
    private val updateDenominatorHomeworkUseCase: UpdateDenominatorHomeworkUseCase
) : ViewModel() {
    
    data class HomeworkEntry(
        val scheduleItem: ScheduleItem,
        val dayName: String,
        val pairNumber: Int,
        val isNumerator: Boolean,
        val subject: String?,
        val homework: String?
    )
    
    private val _allHomework = MutableStateFlow<List<HomeworkEntry>>(emptyList())
    val allHomework: StateFlow<List<HomeworkEntry>> = _allHomework.asStateFlow()
    
    private val _filteredHomework = MutableStateFlow<List<HomeworkEntry>>(emptyList())
    val filteredHomework: StateFlow<List<HomeworkEntry>> = _filteredHomework.asStateFlow()
    
    private val _showTodayOnly = MutableStateFlow(false)
    val showTodayOnly: StateFlow<Boolean> = _showTodayOnly.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadAllHomework()
    }
    
    private fun loadAllHomework() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                getAllScheduleUseCase().collect { items ->
                    val homeworkList = mutableListOf<HomeworkEntry>()
                    val dayNames = listOf("", "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
                    
                    items.forEach { item ->
                        // Числитель
                        if (!item.numeratorHomework.isNullOrBlank()) {
                            homeworkList.add(
                                HomeworkEntry(
                                    scheduleItem = item,
                                    dayName = dayNames.getOrElse(item.dayOfWeek) { "" },
                                    pairNumber = item.pairNumber,
                                    isNumerator = true,
                                    subject = item.numeratorSubject,
                                    homework = item.numeratorHomework
                                )
                            )
                        }
                        // Знаменатель
                        if (!item.denominatorHomework.isNullOrBlank()) {
                            homeworkList.add(
                                HomeworkEntry(
                                    scheduleItem = item,
                                    dayName = dayNames.getOrElse(item.dayOfWeek) { "" },
                                    pairNumber = item.pairNumber,
                                    isNumerator = false,
                                    subject = item.denominatorSubject,
                                    homework = item.denominatorHomework
                                )
                            )
                        }
                    }
                    
                    _allHomework.value = homeworkList
                    applyFilter()
                }
            } catch (e: Exception) {
                _isLoading.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun toggleShowToday() {
        _showTodayOnly.value = !_showTodayOnly.value
        applyFilter()
    }
    
    private fun applyFilter() {
        if (_showTodayOnly.value) {
            val currentDay = getCurrentDayOfWeek()
            _filteredHomework.value = _allHomework.value.filter { 
                getDayIndex(it.dayName) == currentDay 
            }
        } else {
            _filteredHomework.value = _allHomework.value
        }
    }
    
    fun updateHomework(itemId: Long, isNumerator: Boolean, homework: String?) {
        viewModelScope.launch {
            try {
                if (isNumerator) {
                    updateNumeratorHomeworkUseCase(itemId, homework)
                } else {
                    updateDenominatorHomeworkUseCase(itemId, homework)
                }
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }
    
    private fun getDayIndex(dayName: String): Int {
        return when (dayName) {
            "ПН" -> 1
            "ВТ" -> 2
            "СР" -> 3
            "ЧТ" -> 4
            "ПТ" -> 5
            "СБ" -> 6
            "ВС" -> 7
            else -> 1
        }
    }
    
    private fun getCurrentDayOfWeek(): Int {
        val calendar = java.util.Calendar.getInstance()
        var day = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
        if (day == 0) day = 7
        return day
    }
}
