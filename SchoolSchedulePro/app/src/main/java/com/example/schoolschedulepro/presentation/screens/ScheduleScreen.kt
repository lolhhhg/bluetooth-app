package com.example.schoolschedulepro.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schoolschedulepro.data.model.ScheduleItem
import com.example.schoolschedulepro.presentation.components.HomeworkDialog
import com.example.schoolschedulepro.presentation.components.PairCard
import com.example.schoolschedulepro.presentation.theme.Orange500
import com.example.schoolschedulepro.presentation.viewmodel.ScheduleViewModel
import java.io.File
import java.io.FileOutputStream

@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel = viewModel()
) {
    val scheduleItems by viewModel.scheduleItems.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showHomeworkDialog by remember { mutableStateOf(false) }
    var editingItemId by remember { mutableStateOf<Long?>(null) }
    var isEditingNumerator by remember { mutableStateOf(true) }
    
    val context = LocalContext.current
    
    // File picker launcher
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                viewModel.importPdf(inputStream)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "РАСПИСАНИЕ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = { viewModel.goToToday() }) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Сегодня"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сегодня")
                    }
                    IconButton(onClick = { 
                        // Экспорт в JSON
                        // Реализация будет добавлена
                    }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Экспорт"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Экспорт")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pdfPickerLauncher.launch("application/pdf")
                },
                containerColor = Orange500
            ) {
                Text("📄", fontSize = 24.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Дни недели
            DayTabs(
                selectedDay = selectedDay,
                onDaySelected = { viewModel.selectDay(it) }
            )
            
            // Список пар
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(5) { pairIndex ->
                        val pairNumber = pairIndex + 1
                        val item = scheduleItems.find { it.pairNumber == pairNumber }
                        
                        PairCard(
                            pairNumber = pairNumber,
                            scheduleItem = item,
                            onEditNumeratorHomework = {
                                editingItemId = item?.id
                                isEditingNumerator = true
                                showHomeworkDialog = true
                            },
                            onEditDenominatorHomework = {
                                editingItemId = item?.id
                                isEditingNumerator = false
                                showHomeworkDialog = true
                            }
                        )
                    }
                }
            }
        }
        
        // Диалог редактирования ДЗ
        if (showHomeworkDialog) {
            val currentItem = scheduleItems.find { it.id == editingItemId }
            val currentHomework = if (isEditingNumerator) {
                currentItem?.numeratorHomework
            } else {
                currentItem?.denominatorHomework
            }
            
            HomeworkDialog(
                currentHomework = currentHomework,
                isNumerator = isEditingNumerator,
                onDismiss = { showHomeworkDialog = false },
                onSave = { homework ->
                    editingItemId?.let { id ->
                        if (isEditingNumerator) {
                            viewModel.updateNumeratorHomework(id, homework)
                        } else {
                            viewModel.updateDenominatorHomework(id, homework)
                        }
                    }
                    showHomeworkDialog = false
                }
            )
        }
        
        // Показать ошибку
        error?.let { errorMessage ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK", color = MaterialTheme.colorScheme.onError)
                    }
                },
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ) {
                Text(errorMessage)
            }
        }
    }
}

@Composable
fun DayTabs(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    val days = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
    val currentDay = getCurrentDayOfWeek()
    
    TabRow(
        selectedTabIndex = selectedDay - 1,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        days.forEachIndexed { index, day ->
            val dayNumber = index + 1
            Tab(
                selected = selectedDay == dayNumber,
                onClick = { onDaySelected(dayNumber) },
                text = {
                    Text(
                        text = day,
                        fontWeight = if (dayNumber == currentDay) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

private fun getCurrentDayOfWeek(): Int {
    val calendar = java.util.Calendar.getInstance()
    var day = calendar.get(java.util.Calendar.DAY_OF_WEEK) - 1
    if (day == 0) day = 7
    return day
}
