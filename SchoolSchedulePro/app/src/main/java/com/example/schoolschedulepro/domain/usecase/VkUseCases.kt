package com.example.schoolschedulepro.domain.usecase

import com.example.schoolschedulepro.data.model.VkImage
import com.example.schoolschedulepro.domain.repository.VkRepository
import kotlinx.coroutines.flow.Flow

class FetchVkImagesUseCase(private val repository: VkRepository) {
    suspend operator fun invoke(): Result<List<VkImage>> {
        return repository.fetchVkImages()
    }
}

class GetAllVkImagesUseCase(private val repository: VkRepository) {
    operator fun invoke(): Flow<List<VkImage>> {
        return repository.getAllImages()
    }
}

class ClearVkCacheUseCase(private val repository: VkRepository) {
    suspend operator fun invoke() {
        repository.clearCache()
    }
}
