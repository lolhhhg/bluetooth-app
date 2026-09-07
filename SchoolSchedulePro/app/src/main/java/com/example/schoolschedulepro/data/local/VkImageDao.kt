package com.example.schoolschedulepro.data.local

import androidx.room.*
import com.example.schoolschedulepro.data.model.VkImage
import kotlinx.coroutines.flow.Flow

@Dao
interface VkImageDao {
    @Query("SELECT * FROM vk_images ORDER BY postDate DESC")
    fun getAllImages(): Flow<List<VkImage>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(image: VkImage)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(images: List<VkImage>)
    
    @Delete
    suspend fun delete(image: VkImage)
    
    @Query("DELETE FROM vk_images")
    suspend fun deleteAll()
}
