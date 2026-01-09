package com.simon.mpm.feature.photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.repository.AuthRepository
import com.simon.mpm.data.repository.PhotoRepository
import com.simon.mpm.network.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 统一的照片列表ViewModel
 * 支持普通照片列表和回收站
 */
@HiltViewModel
class PhotoListViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 是否为回收站模式
    private val isTrashed: Boolean = savedStateHandle.get<Boolean>("trashed") ?: false

    // UI状态
    private val _uiState = MutableStateFlow(PhotoListUiState(filterTrashed = isTrashed))
    val uiState: StateFlow<PhotoListUiState> = _uiState.asStateFlow()

    // 照片列表
    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    // 总数（用于回收站）
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    init {
        loadPhotos()
        if (isTrashed) {
            loadTotalCount()
        }
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
                        val photoList = response.data ?: emptyList()
                        android.util.Log.d("PhotoListViewModel", "Success: totalRows=${response.totalRows}, data size=${photoList.size}")
                        val newPhotos = if (refresh) {
                            photoList
                        } else {
                            _photos.value + photoList
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
     * 切换收藏筛选
     */
    fun toggleStarFilter() {
        _uiState.update { it.copy(filterStar = !it.filterStar) }
        refresh()
    }
    
    /**
     * 切换视频筛选
     */
    fun toggleVideoFilter() {
        _uiState.update { it.copy(filterVideo = !it.filterVideo) }
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

    // ========== 回收站相关功能 ==========

    /**
     * 加载总数（回收站使用）
     */
    fun loadTotalCount() {
        if (!isTrashed) return
        
        viewModelScope.launch {
            photoRepository.getPhotosCount(trashed = true).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _totalCount.value = result.data
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
     * 恢复照片（回收站使用）
     * 使用trashPhotos接口，该接口会反转照片的trashed状态
     */
    fun restorePhotos(photoIds: List<Int>) {
        if (!isTrashed) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            photoRepository.trashPhotos(photoIds).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // 从列表中移除已恢复的照片
                        _photos.update { current ->
                            current.filter { it.id !in photoIds }
                        }
                        _totalCount.update { it - photoIds.size }
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                restoreSuccess = true
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 清空回收站（回收站使用）
     */
    fun emptyTrash() {
        if (!isTrashed) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            photoRepository.emptyTrash().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                emptyTrashTaskId = result.data.taskId
                            )
                        }
                        // 开始轮询进度
                        startProgressPolling(result.data.taskId)
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * 开始进度轮询（清空回收站使用）
     */
    private fun startProgressPolling(taskId: String) {
        viewModelScope.launch {
            var completed = false
            while (!completed) {
                photoRepository.getProgress(taskId).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            _uiState.update { 
                                it.copy(
                                    emptyTrashProgress = result.data.progress,
                                    emptyTrashTotal = result.data.total,
                                    emptyTrashCompleted = result.data.completed
                                )
                            }
                            
                            if (result.data.completed) {
                                completed = true
                                // 清空完成，重新加载数据
                                _photos.value = emptyList()
                                _totalCount.value = 0
                                _uiState.update { 
                                    it.copy(
                                        emptyTrashTaskId = null,
                                        emptyTrashCompleted = false
                                    )
                                }
                            }
                        }
                        is Result.Error -> {
                            completed = true
                            _uiState.update { 
                                it.copy(
                                    error = result.message,
                                    emptyTrashTaskId = null
                                )
                            }
                        }
                        else -> {}
                    }
                }
                
                if (!completed) {
                    kotlinx.coroutines.delay(1000) // 每秒轮询一次
                }
            }
        }
    }

    /**
     * 清除恢复成功状态
     */
    fun clearRestoreSuccess() {
        _uiState.update { it.copy(restoreSuccess = false) }
    }

    /**
     * 退出登录
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
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
    val sortOrder: String = "-id",  // 默认按ID降序
    
    // 回收站相关状态
    val restoreSuccess: Boolean = false,
    val emptyTrashTaskId: String? = null,
    val emptyTrashProgress: Int = 0,
    val emptyTrashTotal: Int = 0,
    val emptyTrashCompleted: Boolean = false
)
