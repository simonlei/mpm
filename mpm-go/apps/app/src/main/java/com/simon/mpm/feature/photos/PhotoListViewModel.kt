package com.simon.mpm.feature.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.repository.PhotoRepository
import com.simon.mpm.network.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 照片列表ViewModel
 */
@HiltViewModel
class PhotoListViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow(PhotoListUiState())
    val uiState: StateFlow<PhotoListUiState> = _uiState.asStateFlow()

    // 照片列表
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    init {
        loadPhotos()
    }

    /**
     * 加载照片列表
     */
    fun loadPhotos(refresh: Boolean = false) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            val currentState = _uiState.value
            
            // 如果是刷新，重置分页
            val start = if (refresh) 0 else currentState.currentPage * PAGE_SIZE
            
            photoRepository.getPhotos(
                star = currentState.filterStar,
                video = currentState.filterVideo,
                trashed = currentState.filterTrashed,
                start = start,
                size = PAGE_SIZE,
                dateKey = currentState.filterDateKey,
                order = currentState.sortOrder
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        android.util.Log.d("PhotoListViewModel", "Loading photos...")
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Result.Success -> {
                        val response = result.data
                        android.util.Log.d("PhotoListViewModel", "Success: totalRows=${response.totalRows}, data size=${response.data.size}")
                        val newPhotos = if (refresh) {
                            response.data
                        } else {
                            _photos.value + response.data
                        }
                        
                        _photos.value = newPhotos
                        android.util.Log.d("PhotoListViewModel", "Photos updated: ${newPhotos.size} photos")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                currentPage = if (refresh) 0 else it.currentPage + 1,
                                totalRows = response.totalRows,
                                hasMore = response.endRow < response.totalRows,
                                isRefreshing = false
                            )
                        }
                    }
                    is Result.Error -> {
                        android.util.Log.e("PhotoListViewModel", "Error: ${result.message}", result.exception)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message,
                                isRefreshing = false
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 加载更多照片
     */
    fun loadMore() {
        if (!_uiState.value.hasMore || _uiState.value.isLoading) return
        loadPhotos(refresh = false)
    }

    /**
     * 刷新照片列表
     */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadPhotos(refresh = true)
    }

    /**
     * 切换照片收藏状态
     */
    fun toggleStar(photo: Photo) {
        viewModelScope.launch {
            val result = photoRepository.toggleStar(photo.id, photo.star)
            if (result is Result.Success) {
                // 更新本地列表
                _photos.update { photos ->
                    photos.map {
                        if (it.id == photo.id) {
                            it.copy(star = !photo.star)
                        } else {
                            it
                        }
                    }
                }
            }
        }
    }

    /**
     * 设置筛选条件
     */
    fun setFilter(
        star: Boolean? = null,
        video: Boolean? = null,
        trashed: Boolean? = null,
        dateKey: String? = null
    ) {
        _uiState.update {
            it.copy(
                filterStar = star ?: it.filterStar,
                filterVideo = video ?: it.filterVideo,
                filterTrashed = trashed ?: it.filterTrashed,
                filterDateKey = dateKey ?: it.filterDateKey
            )
        }
        refresh()
    }

    /**
     * 设置排序方式
     */
    fun setSortOrder(order: String) {
        _uiState.update { it.copy(sortOrder = order) }
        refresh()
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    companion object {
        private const val PAGE_SIZE = 75
    }
}

/**
 * 照片列表UI状态
 */
data class PhotoListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val totalRows: Int = 0,
    val hasMore: Boolean = true,
    
    // 筛选条件
    val filterStar: Boolean = false,
    val filterVideo: Boolean = false,
    val filterTrashed: Boolean = false,
    val filterDateKey: String = "",
    
    // 排序方式
    val sortOrder: String = "-id"  // 默认按ID降序
)
