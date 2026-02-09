package com.simon.mpm.feature.locations

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
 * 地理位置ViewModel
 */
@HiltViewModel
class LocationsViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow(LocationsUiState())
    val uiState: StateFlow<LocationsUiState> = _uiState.asStateFlow()

    // 地点列表（地点名称 -> 照片列表）
    private val _locationGroups = MutableStateFlow<Map<String, List<Photo>>>(emptyMap())
    val locationGroups: StateFlow<Map<String, List<Photo>>> = _locationGroups.asStateFlow()

    // 当前选中的地点
    private val _selectedLocation = MutableStateFlow<String?>(null)
    val selectedLocation: StateFlow<String?> = _selectedLocation.asStateFlow()

    // 当前地点的照片
    private val _locationPhotos = MutableStateFlow<List<Photo>>(emptyList())
    val locationPhotos: StateFlow<List<Photo>> = _locationPhotos.asStateFlow()

    init {
        loadLocations()
    }

    companion object {
        private const val PAGE_SIZE = 200
        private const val MAX_LOAD_COUNT = 5000
    }

    /**
     * 加载地理位置数据（分批加载，避免一次性加载大量数据导致内存问题）
     */
    fun loadLocations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val accumulatedGroups = mutableMapOf<String, MutableList<Photo>>()
            var start = 0
            var hasMore = true

            while (hasMore && start < MAX_LOAD_COUNT) {
                val batchResult = loadBatch(start)
                when (batchResult) {
                    is BatchResult.Success -> {
                        // 合并到累积分组中
                        batchResult.photos
                            .filter { !it.address.isNullOrBlank() }
                            .forEach { photo ->
                                accumulatedGroups.getOrPut(photo.address!!) { mutableListOf() }
                                    .add(photo)
                            }

                        hasMore = batchResult.photos.size >= PAGE_SIZE
                        start += PAGE_SIZE

                        // 每批加载后更新 UI
                        _locationGroups.value = accumulatedGroups.toMap()
                        _uiState.update {
                            it.copy(hasPhotosWithLocation = accumulatedGroups.isNotEmpty())
                        }
                    }
                    is BatchResult.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = batchResult.message)
                        }
                        return@launch
                    }
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * 加载单批数据
     */
    private suspend fun loadBatch(start: Int): BatchResult {
        var batchResult: BatchResult = BatchResult.Error("未知错误")
        photoRepository.getPhotos(
            start = start,
            size = PAGE_SIZE,
            order = "-taken_date"
        ).collect { result ->
            when (result) {
                is Result.Success -> {
                    batchResult = BatchResult.Success(result.data.data ?: emptyList())
                }
                is Result.Error -> {
                    batchResult = BatchResult.Error(result.message ?: "加载失败")
                }
                is Result.Loading -> { /* 忽略 */ }
            }
        }
        return batchResult
    }

    private sealed class BatchResult {
        data class Success(val photos: List<Photo>) : BatchResult()
        data class Error(val message: String) : BatchResult()
    }

    /**
     * 选择地点
     */
    fun selectLocation(location: String) {
        _selectedLocation.value = location
        _locationPhotos.value = _locationGroups.value[location] ?: emptyList()
    }

    /**
     * 清除选中的地点
     */
    fun clearSelectedLocation() {
        _selectedLocation.value = null
        _locationPhotos.value = emptyList()
    }

    /**
     * 刷新
     */
    fun refresh() {
        loadLocations()
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 地理位置UI状态
 */
data class LocationsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasPhotosWithLocation: Boolean = false
)