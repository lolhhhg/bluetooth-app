package com.example.schoolschedulepro.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schoolschedulepro.presentation.components.HomeworkDialog
import com.example.schoolschedulepro.presentation.viewmodel.HomeworkViewModel

@Composable
fun HomeworkScreen(
    viewModel: HomeworkViewModel = viewModel()
) {
    val homeworkList by viewModel.filteredHomework.collectAsState()
    val showTodayOnly by viewModel.showTodayOnly.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedEntry by remember { mutableStateOf<HomeworkViewModel.HomeworkEntry?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "ДОМАШНЕЕ ЗАДАНИЕ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Фильтры
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !showTodayOnly,
                    onClick = { if (showTodayOnly) viewModel.toggleShowToday() },
                    label = { Text("Все") }
                )
                FilterChip(
                    selected = showTodayOnly,
                    onClick = { if (!showTodayOnly) viewModel.toggleShowToday() },
                    label = { Text("На сегодня") }
                )
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (homeworkList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showTodayOnly) 
                            "На сегодня ничего не задано" 
                        else 
                            "Домашних заданий нет",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(homeworkList.size) { index ->
                        val entry = homeworkList[index]
                        HomeworkCard(
                            entry = entry,
                            onEdit = {
                                selectedEntry = entry
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }
        
        // Диалог редактирования
        if (showDialog && selectedEntry != null) {
            HomeworkDialog(
                currentHomework = selectedEntry!!.homework,
                isNumerator = selectedEntry!!.isNumerator,
                onDismiss = { showDialog = false },
                onSave = { newHomework ->
                    selectedEntry?.let { entry ->
                        viewModel.updateHomework(
                            entry.scheduleItem.id,
                            entry.isNumerator,
                            newHomework.ifBlank { null }
                        )
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun HomeworkCard(
    entry: HomeworkViewModel.HomeworkEntry,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${entry.dayName} • Пара ${entry.pairNumber}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = entry.subject ?: "Неизвестный предмет",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Редактировать"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = if (entry.isNumerator) "Числитель" else "Знаменатель",
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = entry.homework ?: "",
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
