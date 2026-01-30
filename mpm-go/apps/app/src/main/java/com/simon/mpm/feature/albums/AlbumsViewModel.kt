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

    // 文件夹树数据（动态加载的树结构）
    private val _folderTree = MutableStateFlow<List<FolderNode>>(emptyList())
    val folderTree: StateFlow<List<FolderNode>> = _folderTree.asStateFlow()

    // 展开的节点ID集合（用于记录哪些节点已展开）
    private val _expandedNodeIds = MutableStateFlow<Set<Int>>(emptySet())
    val expandedNodeIds: StateFlow<Set<Int>> = _expandedNodeIds.asStateFlow()
    
    // 正在加载子节点的节点ID集合
    private val _loadingNodeIds = MutableStateFlow<Set<Int>>(emptySet())

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
     * 加载根节点（parent_id=-1）
     */
    fun loadFolderTree() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // 加载根节点（parent_id=-1）
            photoRepository.getFoldersTree(
                trashed = false,
                star = false,
                parentId = -1
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        _folderTree.value = result.data
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
     * 加载指定节点的子节点
     */
    private fun loadChildNodes(parentId: Int) {
        viewModelScope.launch {
            // 标记正在加载
            _loadingNodeIds.update { it + parentId }
            
            photoRepository.getFoldersTree(
                trashed = false,
                star = false,
                parentId = parentId
            ).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // 将子节点插入到树中
                        _folderTree.update { tree ->
                            insertChildNodes(tree, parentId, result.data)
                        }
                        // 移除加载标记
                        _loadingNodeIds.update { it - parentId }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(error = result.message) }
                        _loadingNodeIds.update { it - parentId }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }
    
    /**
     * 递归插入子节点到树中
     */
    private fun insertChildNodes(
        nodes: List<FolderNode>,
        parentId: Int,
        children: List<FolderNode>
    ): List<FolderNode> {
        return nodes.map { node ->
            if (node.id == parentId) {
                // 找到父节点，插入子节点
                node.copy(children = children)
            } else if (node.children != null) {
                // 递归查找子节点
                node.copy(children = insertChildNodes(node.children, parentId, children))
            } else {
                node
            }
        }
    }

    /**
     * 切换节点展开/折叠状态
     */
    fun toggleFolder(nodeId: Int) {
        val isExpanded = nodeId in _expandedNodeIds.value
        
        if (isExpanded) {
            // 折叠节点
            _expandedNodeIds.update { it - nodeId }
        } else {
            // 展开节点
            _expandedNodeIds.update { it + nodeId }
            
            // 检查是否已加载子节点
            val node = findNodeById(_folderTree.value, nodeId)
            if (node != null && node.children == null) {
                // 子节点未加载，发起请求
                loadChildNodes(nodeId)
            }
        }
    }
    
    /**
     * 根据ID查找节点
     */
    private fun findNodeById(nodes: List<FolderNode>, nodeId: Int): FolderNode? {
        for (node in nodes) {
            if (node.id == nodeId) return node
            node.children?.let { children ->
                findNodeById(children, nodeId)?.let { return it }
            }
        }
        return null
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