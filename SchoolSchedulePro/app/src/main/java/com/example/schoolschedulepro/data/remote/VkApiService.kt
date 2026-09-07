package com.example.schoolschedulepro.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface VkApiService {
    @GET("topic-{topicId}_{postId}")
    suspend fun getTopicPosts(
        @Path("topicId") topicId: Long,
        @Path("postId") postId: Long
    ): VkApiResponse
    
    companion object {
        const val BASE_URL = "https://vk.ru/"
    }
}

data class VkApiResponse(
    val response: VkResponse?
)

data class VkResponse(
    val items: List<VkPost>?
)

data class VkPost(
    val id: Long,
    val date: Long,
    val text: String?,
    val attachments: List<VkAttachment>?
)

data class VkAttachment(
    val type: String,
    val photo: VkPhoto?
)

data class VkPhoto(
    val sizes: List<VkPhotoSize>
)

data class VkPhotoSize(
    val type: String,
    val url: String,
    val width: Int,
    val height: Int
)
