package com.simon.mpm.feature.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.repository.PhotoRepository
import com.simon.mpm.network.model.Photo
import com.simon.mpm.network.model.ProgressResponse
import com.simon.mpm.network.model.TaskResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 回收站ViewModel
 * 管理回收站的状态和操作
 */
@HiltViewModel
class TrashViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrashUiState())
    val uiState: StateFlow<TrashUiState> = _uiState.asStateFlow()

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    init {
        loadPhotos()
        loadTotalCount()
    }

    /**
     * 加载照片列表
     */
    fun loadPhotos(start: Int = 0, size: Int = 75) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            photoRepository.getPhotos(
                trashed = true,
                start = start,
                size = size,
                order = "-taken_date"
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        if (start == 0) {
                            _photos.value = result.data.data
                        } else {
                            _photos.update { current ->
                                current + result.data.data
                            }
                        }
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                hasMore = result.data.endRow < result.data.totalRows
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
     * 加载总数
     */
    fun loadTotalCount() {
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
     * 恢复照片
     */
    fun restorePhotos(photoIds: List<Int>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            photoRepository.restorePhotos(photoIds).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // 从列表中移除已恢复的照片
                        _photos.update { current ->
                            current.filter { it.id !in photoIds }
                        }
                        _totalCount.update { it -> it - photoIds.size }
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
     * 清空回收站
     */
    fun emptyTrash() {
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
     * 开始进度轮询
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
     * 清除消息状态
     */
    fun clearRestoreSuccess() {
        _uiState.update { it.copy(restoreSuccess = false) }
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
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val restoreSuccess: Boolean = false,
    val emptyTrashTaskId: String? = null,
    val emptyTrashProgress: Int = 0,
    val emptyTrashTotal: Int = 0,
    val emptyTrashCompleted: Boolean = false
)