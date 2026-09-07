package com.example.schoolschedulepro.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vk_images")
data class VkImage(
    @PrimaryKey val url: String,
    val postId: Long,
    val postDate: Long,
    val caption: String? = null,
    val downloadedAt: Long = System.currentTimeMillis()
)
