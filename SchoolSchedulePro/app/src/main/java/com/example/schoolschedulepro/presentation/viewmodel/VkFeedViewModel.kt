package com.example.schoolschedulepro.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schoolschedulepro.data.model.VkImage
import com.example.schoolschedulepro.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VkFeedViewModel(
    private val fetchVkImagesUseCase: FetchVkImagesUseCase,
    private val getAllVkImagesUseCase: GetAllVkImagesUseCase,
    private val clearVkCacheUseCase: ClearVkCacheUseCase
) : ViewModel() {
    
    private val _images = MutableStateFlow<List<VkImage>>(emptyList())
    val images: StateFlow<List<VkImage>> = _images.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadImages()
    }
    
    fun loadImages() {
        viewModelScope.launch {
            try {
                getAllVkImagesUseCase().collect { imageList ->
                    _images.value = imageList
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun refreshImages() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = fetchVkImagesUseCase()
                result.fold(
                    onSuccess = { 
                        _error.value = null
                        loadImages()
                    },
                    onFailure = { exception ->
                        _error.value = exception.message
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            try {
                clearVkCacheUseCase()
                _images.value = emptyList()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
