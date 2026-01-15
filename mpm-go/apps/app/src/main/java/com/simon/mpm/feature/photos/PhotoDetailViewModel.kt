package com.simon.mpm.feature.photos

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.repository.PhotoRepository
import com.simon.mpm.network.model.Activity
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

    companion object {
        private const val TAG = "PhotoDetailViewModel"
    }

    // 从导航参数获取照片ID和来源
    private val photoId: Int = savedStateHandle.get<Int>("photo_id") ?: 0
    val fromTrash: Boolean = savedStateHandle.get<Boolean>("fromTrash") ?: false

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

    // 活动列表
    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities.asStateFlow()

    // 标签列表
    private val _allTags = MutableStateFlow<List<String>>(emptyList())
    val allTags: StateFlow<List<String>> = _allTags.asStateFlow()

    init {
        // 如果有photoId，加载照片详情和列表
        if (photoId > 0) {
            loadPhotoDetail()
            loadPhotoList()
            loadActivities()
            loadAllTags()
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
            android.util.Log.d("PhotoDetailVM", "Loading photo list for photoId: $photoId, fromTrash: $fromTrash")
            // 根据来源加载对应的照片列表
            photoRepository.getPhotos(
                star = false,
                video = false,
                trashed = fromTrash,  // 关键：根据fromTrash参数决定加载哪个列表
                start = 0,
                size = 100, // 加载附近的照片用于左右滑动
                dateKey = "",
                order = "-id"
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val photoList = result.data.data ?: emptyList()
                        _photoList.value = photoList
                        android.util.Log.d("PhotoDetailVM", "Photo list loaded: ${photoList.size} photos (trashed=$fromTrash)")
                        // 找到当前照片的索引
                        val index = photoList.indexOfFirst { it.id == photoId }
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
                    // 更新当前照片的rotate值
                    _photo.update { it?.copy(rotate = newRotate) }
                    
                    // 同时更新photoList中对应照片的rotate值
                    val currentIndex = _currentPhotoIndex.value
                    _photoList.update { list ->
                        list.mapIndexed { index, photo ->
                            if (index == currentIndex) {
                                photo.copy(rotate = newRotate)
                            } else {
                                photo
                            }
                        }
                    }
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
                        android.util.Log.d("PhotoDetailVM", "Photo moved to trash, handling navigation")
                        handlePhotoDeleted()
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
     * 恢复照片（从回收站）
     * 使用trashPhotos接口，该接口会反转照片的trashed状态
     */
    fun restorePhoto() {
        val currentPhoto = _photo.value ?: return
        
        viewModelScope.launch {
            photoRepository.trashPhotos(listOf(currentPhoto.id)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        android.util.Log.d("PhotoDetailVM", "Photo restored, handling navigation")
                        handlePhotoDeleted()
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
     * 永久删除照片（从回收站）
     */
    fun permanentlyDelete() {
        val currentPhoto = _photo.value ?: return
        
        viewModelScope.launch {
            photoRepository.deletePhotos(listOf(currentPhoto.id)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        android.util.Log.d("PhotoDetailVM", "Photo permanently deleted, handling navigation")
                        handlePhotoDeleted()
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
     * 处理照片删除后的逻辑
     * 1. 从列表中移除被删除的照片
     * 2. 切换到下一张照片（如果是最后一张则切换到前一张）
     * 3. 如果没有照片了才退出详情页
     */
    private fun handlePhotoDeleted() {
        val currentIndex = _currentPhotoIndex.value
        val currentList = _photoList.value.toMutableList()
        
        android.util.Log.d("PhotoDetailVM", "handlePhotoDeleted: currentIndex=$currentIndex, listSize=${currentList.size}")
        
        // 从列表中移除当前照片
        if (currentIndex >= 0 && currentIndex < currentList.size) {
            currentList.removeAt(currentIndex)
            _photoList.value = currentList
            
            android.util.Log.d("PhotoDetailVM", "Photo removed, new listSize=${currentList.size}")
            
            // 如果列表为空，标记需要退出
            if (currentList.isEmpty()) {
                android.util.Log.d("PhotoDetailVM", "No more photos, marking for exit")
                _uiState.update { it.copy(photoDeleted = true) }
                return
            }
            
            // 确定下一张照片的索引
            val nextIndex = when {
                currentIndex < currentList.size -> currentIndex // 显示下一张（当前位置）
                currentIndex > 0 -> currentIndex - 1 // 已经是最后一张，显示前一张
                else -> 0 // 保险起见
            }
            
            android.util.Log.d("PhotoDetailVM", "Switching to nextIndex=$nextIndex")
            
            // 更新索引并加载新照片
            _currentPhotoIndex.value = nextIndex
            val nextPhoto = currentList[nextIndex]
            loadPhotoDetail(nextPhoto.id)
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

    /**
     * 加载活动列表
     */
    private fun loadActivities() {
        viewModelScope.launch {
            photoRepository.getActivities().collect { result ->
                if (result is Result.Success) {
                    _activities.value = result.data
                }
            }
        }
    }

    /**
     * 加载所有标签
     */
    private fun loadAllTags() {
        Log.d(TAG, "loadAllTags: 开始加载标签列表")
        viewModelScope.launch {
            photoRepository.getAllTags().collect { result ->
                Log.d(TAG, "loadAllTags: 收到结果 - ${result.javaClass.simpleName}")
                if (result is Result.Success) {
                    Log.d(TAG, "loadAllTags: 成功获取 ${result.data.size} 个标签")
                    _allTags.value = result.data
                } else if (result is Result.Error) {
                    Log.e(TAG, "loadAllTags: 获取标签失败 - ${result.exception.message}")
                }
            }
        }
    }

    /**
     * 更新照片信息
     */
    fun updatePhotoInfo(editData: PhotoEditData) {
        val currentPhoto = _photo.value ?: return
        
        viewModelScope.launch {
            photoRepository.updatePhoto(
                id = currentPhoto.id,
                latitude = editData.latitude,
                longitude = editData.longitude,
                takenDate = editData.takenDate,
                activity = editData.activity,
                tags = editData.tags
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // 更新当前照片
                        _photo.value = result.data
                        
                        // 更新列表中的照片
                        val currentIndex = _currentPhotoIndex.value
                        _photoList.update { list ->
                            list.mapIndexed { index, photo ->
                                if (index == currentIndex) {
                                    result.data
                                } else {
                                    photo
                                }
                            }
                        }
                        
                        // 关闭编辑对话框
                        _uiState.update { it.copy(showEditDialog = false) }
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
     * 显示/隐藏编辑对话框
     */
    fun toggleEditDialog() {
        val newShowState = !_uiState.value.showEditDialog
        Log.d(TAG, "toggleEditDialog: showEditDialog = $newShowState")
        _uiState.update { it.copy(showEditDialog = newShowState) }
        
        // 如果是打开对话框，重新从服务端拉取最新的标签列表
        if (newShowState) {
            Log.d(TAG, "toggleEditDialog: 准备加载标签列表")
            loadAllTags()
        }
    }
}

/**
 * 照片详情UI状态
 */
data class PhotoDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showInfoPanel: Boolean = false,
    val photoDeleted: Boolean = false,
    val showEditDialog: Boolean = false
)
