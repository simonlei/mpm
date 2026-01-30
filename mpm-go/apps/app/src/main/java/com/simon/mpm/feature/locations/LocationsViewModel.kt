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

    /**
     * 加载地理位置数据
     */
    fun loadLocations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 获取所有照片
            photoRepository.getPhotos(
                start = 0,
                size = 1000,  // 获取足够多的照片用于分组
                order = "-taken_date"
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        val photos = result.data.data ?: emptyList()
                        
                        // 按地址分组
                        val groups = photos
                            .filter { !it.address.isNullOrBlank() }
                            .groupBy { it.address!! }
                        
                        _locationGroups.value = groups
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = null,
                                hasPhotosWithLocation = groups.isNotEmpty()
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