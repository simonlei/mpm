package com.simon.mpm.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.repository.PhotoRepository
import com.simon.mpm.network.model.Photo
import com.simon.mpm.network.model.TreeNode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 时间轴ViewModel
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    // 时间树数据
    private val _timeTree = MutableStateFlow<List<TreeNode>>(emptyList())
    val timeTree: StateFlow<List<TreeNode>> = _timeTree.asStateFlow()

    // 展开的节点ID集合
    private val _expandedNodes = MutableStateFlow<Set<Int>>(emptySet())
    val expandedNodes: StateFlow<Set<Int>> = _expandedNodes.asStateFlow()

    // 当前选中的月份照片
    private val _monthPhotos = MutableStateFlow<List<Photo>>(emptyList())
    val monthPhotos: StateFlow<List<Photo>> = _monthPhotos.asStateFlow()

    // 当前选中的dateKey
    private val _selectedDateKey = MutableStateFlow<String?>(null)
    val selectedDateKey: StateFlow<String?> = _selectedDateKey.asStateFlow()

    init {
        loadTimeTree()
    }

    /**
     * 加载时间树
     */
    fun loadTimeTree() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            photoRepository.getPhotosDateTree(
                trashed = false,
                star = false
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        _timeTree.value = result.data
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = null
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
                }
            }
        }
    }

    /**
     * 切换节点展开/折叠状态
     */
    fun toggleNode(nodeId: Int) {
        _expandedNodes.update { current ->
            if (nodeId in current) {
                current - nodeId
            } else {
                current + nodeId
            }
        }
    }

    /**
     * 加载指定月份的照片
     */
    fun loadMonthPhotos(dateKey: String) {
        viewModelScope.launch {
            _selectedDateKey.value = dateKey
            _uiState.update { it.copy(isLoadingPhotos = true, error = null) }
            
            photoRepository.getPhotos(
                dateKey = dateKey,
                start = 0,
                size = 100,
                order = "-taken_date"
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoadingPhotos = true) }
                    }
                    is Result.Success -> {
                        _monthPhotos.value = result.data.data ?: emptyList()
                        _uiState.update { 
                            it.copy(
                                isLoadingPhotos = false,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoadingPhotos = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 清除选中的月份
     */
    fun clearSelectedMonth() {
        _selectedDateKey.value = null
        _monthPhotos.value = emptyList()
    }

    /**
     * 刷新
     */
    fun refresh() {
        loadTimeTree()
        _selectedDateKey.value?.let { loadMonthPhotos(it) }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 时间轴UI状态
 */
data class TimelineUiState(
    val isLoading: Boolean = false,
    val isLoadingPhotos: Boolean = false,
    val error: String? = null
)
