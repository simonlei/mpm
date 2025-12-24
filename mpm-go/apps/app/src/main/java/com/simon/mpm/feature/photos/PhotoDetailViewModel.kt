package com.simon.mpm.feature.photos

import androidx.lifecycle.SavedStateHandle
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
 * 照片详情ViewModel
 */
@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 从导航参数获取照片ID
    private val photoId: Int = savedStateHandle.get<Int>("photoId") ?: 0

    // UI状态
    private val _uiState = MutableStateFlow(PhotoDetailUiState())
    val uiState: StateFlow<PhotoDetailUiState> = _uiState.asStateFlow()

    // 当前照片
    private val _photo = MutableStateFlow<Photo?>(null)
    val photo: StateFlow<Photo?> = _photo.asStateFlow()

    init {
        // 如果有photoId，加载照片详情
        if (photoId > 0) {
            loadPhotoDetail()
        }
    }

    /**
     * 加载照片详情
     */
    private fun loadPhotoDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            photoRepository.getPhotoById(photoId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        _photo.value = result.data
                        _uiState.update { it.copy(isLoading = false, error = null) }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isLoading = false, error = result.message)
                        }
                    }
                }
            }
        }
    }

    /**
     * 切换收藏状态
     */
    fun toggleStar() {
        val currentPhoto = _photo.value ?: return
        
        viewModelScope.launch {
            val result = photoRepository.toggleStar(currentPhoto.id, currentPhoto.star)
            if (result is Result.Success) {
                _photo.update { it?.copy(star = !currentPhoto.star) }
            }
        }
    }

    /**
     * 旋转照片
     */
    fun rotatePhoto(degrees: Int) {
        val currentPhoto = _photo.value ?: return
        val newRotate = (currentPhoto.rotate + degrees) % 360
        
        viewModelScope.launch {
            photoRepository.updatePhoto(
                id = currentPhoto.id,
                rotate = newRotate
            ).collect { result ->
                if (result is Result.Success) {
                    _photo.update { it?.copy(rotate = newRotate) }
                }
            }
        }
    }

    /**
     * 移动到回收站
     */
    fun moveToTrash() {
        val currentPhoto = _photo.value ?: return
        
        viewModelScope.launch {
            photoRepository.trashPhotos(listOf(currentPhoto.id)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(photoDeleted = true) }
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
     * 显示/隐藏信息面板
     */
    fun toggleInfoPanel() {
        _uiState.update { it.copy(showInfoPanel = !it.showInfoPanel) }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 照片详情UI状态
 */
data class PhotoDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showInfoPanel: Boolean = false,
    val photoDeleted: Boolean = false
)
