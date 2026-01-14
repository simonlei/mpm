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
 * 回收站ViewModel
 */
@HiltViewModel
class TrashViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    private var currentPage = 0
    private val pageSize = 75

    init {
        loadPhotos()
        loadPhotoCount()
    }

    /**
     * 加载照片列表
     */
    private fun loadPhotos(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 0
            _uiState.update { it.copy(photos = emptyList(), hasMore = true) }
        }

        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = currentPage == 0,
                    isLoadingMore = currentPage > 0
                )
            }

            photoRepository.getPhotos(
                trashed = true,
                start = currentPage * pageSize,
                size = pageSize,
                order = "-id"
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        // 已在上面处理
                    }
                    is Result.Success -> {
                        val newPhotos = result.data.data ?: emptyList()
                        val hasMore = newPhotos.size >= pageSize

                        _uiState.update {
                            it.copy(
                                photos = if (currentPage == 0) newPhotos else it.photos + newPhotos,
                                isLoading = false,
                                isLoadingMore = false,
                                hasMore = hasMore,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 加载照片总数
     */
    private fun loadPhotoCount() {
        viewModelScope.launch {
            photoRepository.getPhotoCount(trashed = true).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(totalCount = result.data) }
                }
            }
        }
    }

    /**
     * 加载更多
     */
    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        currentPage++
        loadPhotos()
    }

    /**
     * 刷新
     */
    fun refresh() {
        loadPhotos(refresh = true)
        loadPhotoCount()
    }

    /**
     * 清空回收站
     */
    fun emptyTrash() {
        viewModelScope.launch {
            photoRepository.emptyTrash().collect { result ->
                when (result) {
                    is Result.Success -> {
                        // 清空成功，刷新列表
                        refresh()
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 回收站UI状态
 */
data class TrashUiState(
    val photos: List<Photo> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val totalCount: Int = 0
)
