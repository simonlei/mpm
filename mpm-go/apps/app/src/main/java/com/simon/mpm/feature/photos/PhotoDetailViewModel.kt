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

    // 照片列表（用于左右滑动切换）
    private val _photoList = MutableStateFlow<List<Photo>>(emptyList())
    val photoList: StateFlow<List<Photo>> = _photoList.asStateFlow()

    // 当前照片索引
    private val _currentPhotoIndex = MutableStateFlow(0)
    val currentPhotoIndex: StateFlow<Int> = _currentPhotoIndex.asStateFlow()

    init {
        // 如果有photoId，加载照片详情和列表
        if (photoId > 0) {
            loadPhotoDetail()
            loadPhotoList()
        }
    }

    /**
     * 加载照片详情
     */
    private fun loadPhotoDetail(id: Int = photoId) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            photoRepository.getPhotoById(id).collect { result ->
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
     * 加载照片列表（用于左右滑动）
     */
    private fun loadPhotoList() {
        viewModelScope.launch {
            android.util.Log.d("PhotoDetailVM", "Loading photo list for photoId: $photoId")
            // 加载当前照片所在的照片列表
            photoRepository.getPhotos(
                star = false,
                video = false,
                trashed = false,
                start = 0,
                size = 1000, // 加载足够多的照片
                dateKey = "",
                order = "-id"
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        _photoList.value = result.data.data
                        android.util.Log.d("PhotoDetailVM", "Photo list loaded: ${result.data.data.size} photos")
                        // 找到当前照片的索引
                        val index = result.data.data.indexOfFirst { it.id == photoId }
                        android.util.Log.d("PhotoDetailVM", "Current photo index: $index")
                        if (index >= 0) {
                            _currentPhotoIndex.value = index
                        }
                    }
                    else -> {
                        android.util.Log.d("PhotoDetailVM", "Photo list loading failed or loading")
                    }
                }
            }
        }
    }

    /**
     * 切换到指定索引的照片
     */
    fun switchToPhoto(index: Int) {
        android.util.Log.d("PhotoDetailVM", "switchToPhoto called with index: $index, list size: ${_photoList.value.size}")
        if (index < 0 || index >= _photoList.value.size) {
            android.util.Log.w("PhotoDetailVM", "Invalid index: $index")
            return
        }
        
        _currentPhotoIndex.value = index
        val photo = _photoList.value[index]
        android.util.Log.d("PhotoDetailVM", "Switching to photo: ${photo.id} - ${photo.name}")
        loadPhotoDetail(photo.id)
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
