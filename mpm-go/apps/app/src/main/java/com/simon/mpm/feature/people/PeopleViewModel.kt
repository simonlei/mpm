package com.simon.mpm.feature.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.data.repository.PhotoRepository
import com.simon.mpm.network.model.Face
import com.simon.mpm.network.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 人脸识别ViewModel
 */
@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow(PeopleUiState())
    val uiState: StateFlow<PeopleUiState> = _uiState.asStateFlow()

    // 服务器地址
    val serverUrl: StateFlow<String> = preferencesManager.serverUrl
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // 人脸列表
    private val _faces = MutableStateFlow<List<Face>>(emptyList())
    val faces: StateFlow<List<Face>> = _faces.asStateFlow()

    // 当前选中的人脸ID
    private val _selectedFaceId = MutableStateFlow<Int?>(null)
    val selectedFaceId: StateFlow<Int?> = _selectedFaceId.asStateFlow()

    // 当前人脸的照片
    private val _facePhotos = MutableStateFlow<List<Photo>>(emptyList())
    val facePhotos: StateFlow<List<Photo>> = _facePhotos.asStateFlow()

    init {
        loadFaces()
    }

    /**
     * 加载人脸列表
     */
    fun loadFaces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            photoRepository.getFaces(
                showHidden = false,
                page = 1,
                size = 100,
                nameFilter = ""
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        _faces.value = result.data.faces
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = null,
                                hasFaces = result.data.faces.isNotEmpty()
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
     * 选择人脸
     */
    fun selectFace(faceId: Int) {
        _selectedFaceId.value = faceId
        loadFacePhotos(faceId)
    }

    /**
     * 加载指定人脸的照片
     */
    fun loadFacePhotos(faceId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPhotos = true, error = null) }
            
            photoRepository.getPhotos(
                faceId = faceId,
                start = 0,
                size = 100,
                order = "-taken_date"
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoadingPhotos = true) }
                    }
                    is Result.Success -> {
                        _facePhotos.value = result.data.data ?: emptyList()
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
     * 清除选中的人脸
     */
    fun clearSelectedFace() {
        _selectedFaceId.value = null
        _facePhotos.value = emptyList()
    }

    /**
     * 刷新
     */
    fun refresh() {
        loadFaces()
        _selectedFaceId.value?.let { loadFacePhotos(it) }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 人脸识别UI状态
 */
data class PeopleUiState(
    val isLoading: Boolean = false,
    val isLoadingPhotos: Boolean = false,
    val error: String? = null,
    val hasFaces: Boolean = false
)