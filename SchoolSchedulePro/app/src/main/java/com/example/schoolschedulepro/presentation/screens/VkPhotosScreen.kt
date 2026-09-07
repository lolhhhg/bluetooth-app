package com.example.schoolschedulepro.presentation.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearCache
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.schoolschedulepro.presentation.viewmodel.VkFeedViewModel

@Composable
fun VkPhotosScreen(
    viewModel: VkFeedViewModel = viewModel()
) {
    val images by viewModel.images.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "ЕЖЕДНЕВНОЕ РАСПИСАНИЕ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshImages() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Обновить")
                    }
                    IconButton(onClick = { viewModel.clearCache() }) {
                        Icon(
                            imageVector = Icons.Default.ClearCache,
                            contentDescription = "Очистить кэш"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Кэш")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && images.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (images.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Фото не найдены",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Попробуйте обновить",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.refreshImages() }) {
                        Text("Обновить")
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images.size) { index ->
                        val image = images[index]
                        VkPhotoCard(
                            imageUrl = image.url,
                            caption = image.caption,
                            postDate = image.postDate,
                            onClick = {
                                // Открыть в VK или сохранить
                            }
                        )
                    }
                }
            }
            
            // Показать ошибку
            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
            }
        }
    }
}

@Composable
fun VkPhotoCard(
    imageUrl: String,
    caption: String?,
    postDate: Long,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(4f / 3f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = imageUrl,
                contentDescription = caption ?: "Расписание",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Градиент снизу
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(60.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                android.graphics.Color.TRANSPARENT,
                                android.graphics.Color.parseColor("#80000000")
                            )
                        )
                    )
            )
            
            // Дата
            Text(
                text = formatPostDate(postDate),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                fontSize = 14.sp,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun formatPostDate(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
    return format.format(date)
}
