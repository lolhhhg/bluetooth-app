package com.example.schoolschedulepro.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.schoolschedulepro.data.model.ScheduleItem
import com.example.schoolschedulepro.presentation.theme.Orange500
import com.example.schoolschedulepro.presentation.theme.SuccessGreen
import com.example.schoolschedulepro.presentation.theme.TextGray

@Composable
fun PairCard(
    pairNumber: Int,
    scheduleItem: ScheduleItem?,
    onEditNumeratorHomework: () -> Unit,
    onEditDenominatorHomework: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (scheduleItem != null && 
                (!scheduleItem.numeratorSubject.isNullOrBlank() || 
                 !scheduleItem.denominatorSubject.isNullOrBlank())) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер пары
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Orange500),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$pairNumber",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                if (scheduleItem == null || 
                    (scheduleItem.numeratorSubject.isNullOrBlank() && 
                     scheduleItem.denominatorSubject.isNullOrBlank())) {
                    // Нет пары
                    Text(
                        text = "(нет пары)",
                        color = TextGray,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Числитель
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ЧИСЛИТЕЛЬ", fontSize = 12.sp, color = TextGray)
                            if (!scheduleItem.numeratorSubject.isNullOrBlank()) {
                                Text(
                                    text = scheduleItem.numeratorSubject!!,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                if (!scheduleItem.numeratorRoom.isNullOrBlank()) {
                                    Text(
                                        text = "ауд. ${scheduleItem.numeratorRoom}",
                                        fontSize = 14.sp,
                                        color = TextGray
                                    )
                                }
                                HomeworkButton(
                                    hasHomework = !scheduleItem.numeratorHomework.isNullOrBlank(),
                                    homeworkText = scheduleItem.numeratorHomework,
                                    onClick = onEditNumeratorHomework
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Знаменатель
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ЗНАМЕНАТЕЛЬ", fontSize = 12.sp, color = TextGray)
                            if (!scheduleItem.denominatorSubject.isNullOrBlank()) {
                                Text(
                                    text = scheduleItem.denominatorSubject!!,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                if (!scheduleItem.denominatorRoom.isNullOrBlank()) {
                                    Text(
                                        text = "ауд. ${scheduleItem.denominatorRoom}",
                                        fontSize = 14.sp,
                                        color = TextGray
                                    )
                                }
                                HomeworkButton(
                                    hasHomework = !scheduleItem.denominatorHomework.isNullOrBlank(),
                                    homeworkText = scheduleItem.denominatorHomework,
                                    onClick = onEditDenominatorHomework
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeworkButton(
    hasHomework: Boolean,
    homeworkText: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (hasHomework) "✅" else "📝",
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "ДЗ",
            fontSize = 14.sp,
            color = if (hasHomework) SuccessGreen else Orange500
        )
        if (hasHomework && !homeworkText.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = homeworkText.take(20) + if (homeworkText.length > 20) "..." else "",
                fontSize = 12.sp,
                color = TextGray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun HomeworkDialog(
    currentHomework: String?,
    isNumerator: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var homeworkText by remember { mutableStateOf(currentHomework ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Домашнее задание") },
        text = {
            Column {
                Text(
                    text = if (isNumerator) "Числитель" else "Знаменатель",
                    fontSize = 14.sp,
                    color = Orange500,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = homeworkText,
                    onValueChange = { homeworkText = it },
                    label = { Text("Введите ДЗ") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(homeworkText) }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
