package com.simon.mpm.feature.albums

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.repository.PhotoRepository
import com.simon.mpm.network.model.FolderNode
import com.simon.mpm.network.model.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 相册ViewModel
 */
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow(AlbumsUiState())
    val uiState: StateFlow<AlbumsUiState> = _uiState.asStateFlow()

    // 文件夹树数据（完整树结构）
    private val _fullFolderTree = MutableStateFlow<List<FolderNode>>(emptyList())
    
    // 根节点列表（parent_id=-1的节点）
    private val _folderTree = MutableStateFlow<List<FolderNode>>(emptyList())
    val folderTree: StateFlow<List<FolderNode>> = _folderTree.asStateFlow()

    // 展开的节点路径集合
    private val _expandedPaths = MutableStateFlow<Set<String>>(emptySet())
    val expandedPaths: StateFlow<Set<String>> = _expandedPaths.asStateFlow()

    // 当前选中的文件夹路径
    private val _selectedPath = MutableStateFlow<String?>(null)
    val selectedPath: StateFlow<String?> = _selectedPath.asStateFlow()

    // 当前文件夹的照片
    private val _folderPhotos = MutableStateFlow<List<Photo>>(emptyList())
    val folderPhotos: StateFlow<List<Photo>> = _folderPhotos.asStateFlow()

    // 面包屑路径
    private val _breadcrumbs = MutableStateFlow<List<String>>(emptyList())
    val breadcrumbs: StateFlow<List<String>> = _breadcrumbs.asStateFlow()

    init {
        loadFolderTree()
    }

    /**
     * 加载文件夹树
     */
    fun loadFolderTree() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            photoRepository.getFoldersTree(
                trashed = false,
                star = false
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        // 保存完整树结构
                        _fullFolderTree.value = result.data
                        // 只展示根节点（parent_id=-1）
                        _folderTree.value = result.data.filter { it.parentId == -1 }
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
    fun toggleFolder(path: String) {
        _expandedPaths.update { current ->
            if (path in current) {
                current - path
            } else {
                current + path
            }
        }
    }

    /**
     * 选择文件夹并加载照片
     */
    fun selectFolder(path: String) {
        _selectedPath.value = path
        updateBreadcrumbs(path)
        loadFolderPhotos(path)
    }

    /**
     * 更新面包屑导航
     */
    private fun updateBreadcrumbs(path: String) {
        val parts = path.split("/").filter { it.isNotEmpty() }
        _breadcrumbs.value = parts
    }

    /**
     * 加载指定文件夹的照片
     */
    fun loadFolderPhotos(path: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingPhotos = true, error = null) }
            
            photoRepository.getPhotos(
                path = path,
                start = 0,
                size = 100,
                order = "-taken_date"
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoadingPhotos = true) }
                    }
                    is Result.Success -> {
                        _folderPhotos.value = result.data.data ?: emptyList()
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
     * 清除选中的文件夹
     */
    fun clearSelectedFolder() {
        _selectedPath.value = null
        _folderPhotos.value = emptyList()
        _breadcrumbs.value = emptyList()
    }

    /**
     * 刷新
     */
    fun refresh() {
        loadFolderTree()
        _selectedPath.value?.let { loadFolderPhotos(it) }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 相册UI状态
 */
data class AlbumsUiState(
    val isLoading: Boolean = false,
    val isLoadingPhotos: Boolean = false,
    val error: String? = null
)