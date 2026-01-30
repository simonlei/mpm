package com.simon.mpm.feature.albums

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.simon.mpm.feature.photos.PhotoGrid
import com.simon.mpm.feature.photos.StandardPhotoGridItem
import com.simon.mpm.network.model.FolderNode
import com.simon.mpm.network.model.Photo

/**
 * 相册页面
 * 支持横竖屏自适应布局
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
    onPhotoClick: (Photo) -> Unit = {},
    viewModel: AlbumsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val folderTree by viewModel.folderTree.collectAsState()
    val expandedNodeIds by viewModel.expandedNodeIds.collectAsState()
    val selectedPath by viewModel.selectedPath.collectAsState()
    val folderPhotos by viewModel.folderPhotos.collectAsState()
    val breadcrumbs by viewModel.breadcrumbs.collectAsState()
    
    // 判断横竖屏
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // 底部抽屉状态（竖屏模式使用）
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("相册") },
                actions = {
                    // 刷新按钮
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLandscape) {
                // 横屏模式：左右分栏布局
                LandscapeLayout(
                    folderTree = folderTree,
                    expandedNodeIds = expandedNodeIds,
                    selectedPath = selectedPath,
                    folderPhotos = folderPhotos,
                    isLoading = uiState.isLoading,
                    isLoadingPhotos = uiState.isLoadingPhotos,
                    onFolderToggle = viewModel::toggleFolder,
                    onFolderSelect = viewModel::selectFolder,
                    onPhotoClick = onPhotoClick
                )
            } else {
                // 竖屏模式：面包屑导航 + 照片网格 + 底部抽屉
                PortraitLayout(
                    folderTree = folderTree,
                    expandedNodeIds = expandedNodeIds,
                    selectedPath = selectedPath,
                    folderPhotos = folderPhotos,
                    breadcrumbs = breadcrumbs,
                    isLoading = uiState.isLoading,
                    isLoadingPhotos = uiState.isLoadingPhotos,
                    onFolderToggle = viewModel::toggleFolder,
                    onFolderSelect = { path ->
                        viewModel.selectFolder(path)
                        showBottomSheet = false
                    },
                    onPhotoClick = onPhotoClick,
                    onShowFolderTree = { showBottomSheet = true },
                    onBack = viewModel::clearSelectedFolder
                )
            }

            // 加载指示器
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }

            // 错误提示
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("关闭")
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }

    // 底部抽屉（竖屏模式）
    if (showBottomSheet && !isLandscape) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(0.7f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "选择文件夹",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                FolderTreePanel(
                    folders = folderTree,
                    expandedNodeIds = expandedNodeIds,
                    selectedPath = selectedPath,
                    onFolderToggle = viewModel::toggleFolder,
                    onFolderSelect = { path ->
                        viewModel.selectFolder(path)
                        showBottomSheet = false
                    }
                )
            }
        }
    }
}

/**
 * 横屏布局：左侧目录树 + 右侧照片网格
 */
@Composable
private fun LandscapeLayout(
    folderTree: List<FolderNode>,
    expandedNodeIds: Set<Int>,
    selectedPath: String?,
    folderPhotos: List<Photo>,
    isLoading: Boolean,
    isLoadingPhotos: Boolean,
    onFolderToggle: (Int) -> Unit,
    onFolderSelect: (String) -> Unit,
    onPhotoClick: (Photo) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // 左侧：目录树面板
        Surface(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight(),
            tonalElevation = 1.dp
        ) {
            FolderTreePanel(
                folders = folderTree,
                expandedNodeIds = expandedNodeIds,
                selectedPath = selectedPath,
                onFolderToggle = onFolderToggle,
                onFolderSelect = onFolderSelect
            )
        }

        // 右侧：照片网格
        Box(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
        ) {
            if (selectedPath != null) {
                PhotoGrid(
                    photos = folderPhotos,
                    isLoading = isLoadingPhotos,
                    hasMore = false,
                    emptyText = "该文件夹暂无照片",
                    onPhotoClick = onPhotoClick,
                    onLoadMore = {},
                    onRefresh = {},
                    photoItemContent = { photo ->
                        StandardPhotoGridItem(
                            photo = photo,
                            onStarClick = {}
                        )
                    }
                )
            } else {
                // 未选中文件夹时的提示
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "请选择一个文件夹",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 竖屏布局：面包屑导航 + 照片网格
 */
@Composable
private fun PortraitLayout(
    folderTree: List<FolderNode>,
    expandedNodeIds: Set<Int>,
    selectedPath: String?,
    folderPhotos: List<Photo>,
    breadcrumbs: List<String>,
    isLoading: Boolean,
    isLoadingPhotos: Boolean,
    onFolderToggle: (Int) -> Unit,
    onFolderSelect: (String) -> Unit,
    onPhotoClick: (Photo) -> Unit,
    onShowFolderTree: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (selectedPath != null) {
            // 面包屑导航
            BreadcrumbNavigation(
                breadcrumbs = breadcrumbs,
                onBack = onBack,
                onShowFolderTree = onShowFolderTree
            )
            
            // 照片网格
            PhotoGrid(
                photos = folderPhotos,
                isLoading = isLoadingPhotos,
                hasMore = false,
                emptyText = "该文件夹暂无照片",
                onPhotoClick = onPhotoClick,
                onLoadMore = {},
                onRefresh = {},
                photoItemContent = { photo ->
                    StandardPhotoGridItem(
                        photo = photo,
                        onStarClick = {}
                    )
                }
            )
        } else {
            // 未选中文件夹时显示目录树
            FolderTreePanel(
                folders = folderTree,
                expandedNodeIds = expandedNodeIds,
                selectedPath = selectedPath,
                onFolderToggle = onFolderToggle,
                onFolderSelect = onFolderSelect
            )
        }
    }
}

/**
 * 面包屑导航
 */
@Composable
private fun BreadcrumbNavigation(
    breadcrumbs: List<String>,
    onBack: () -> Unit,
    onShowFolderTree: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 返回按钮
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回"
                )
            }
            
            // 面包屑路径
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "首页",
                    style = MaterialTheme.typography.bodyMedium
                )
                breadcrumbs.forEach { part ->
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = part,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // 文件夹图标按钮
            IconButton(onClick = onShowFolderTree) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "选择文件夹"
                )
            }
        }
    }
}

/**
 * 文件夹树面板
 */
@Composable
private fun FolderTreePanel(
    folders: List<FolderNode>,
    expandedNodeIds: Set<Int>,
    selectedPath: String?,
    onFolderToggle: (Int) -> Unit,
    onFolderSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (folders.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "暂无文件夹",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(folders) { folder ->
                FolderTreeItem(
                    folder = folder,
                    level = 0,
                    isExpanded = folder.id in expandedNodeIds,
                    isSelected = folder.path == selectedPath,
                    onToggle = { onFolderToggle(folder.id) },
                    onSelect = { onFolderSelect(folder.path) },
                    expandedNodeIds = expandedNodeIds,
                    selectedPath = selectedPath,
                    onFolderToggle = onFolderToggle,
                    onFolderSelect = onFolderSelect
                )
            }
        }
    }
}

/**
 * 文件夹树项
 */
@Composable
private fun FolderTreeItem(
    folder: FolderNode,
    level: Int,
    isExpanded: Boolean,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onSelect: () -> Unit,
    expandedNodeIds: Set<Int>,
    selectedPath: String?,
    onFolderToggle: (Int) -> Unit,
    onFolderSelect: (String) -> Unit
) {
    Column {
        // 文件夹项
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect() },
            color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = (16 + level * 24).dp,
                        end = 16.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 展开/折叠图标（根据hasChildren字段决定是否显示）
                if (folder.hasChildren) {
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) {
                                Icons.Default.ExpandMore
                            } else {
                                Icons.Default.ChevronRight
                            },
                            contentDescription = if (isExpanded) "收起" else "展开",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // 没有子节点时，用空白占位保持对齐
                    Spacer(modifier = Modifier.width(24.dp))
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 文件夹图标
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 文件夹名称
                Text(
                    text = folder.title ?: "未命名",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // 子文件夹（只在展开且children不为null时显示）
        if (isExpanded && folder.children != null) {
            folder.children.forEach { childFolder ->
                FolderTreeItem(
                    folder = childFolder,
                    level = level + 1,
                    isExpanded = childFolder.id in expandedNodeIds,
                    isSelected = childFolder.path == selectedPath,
                    onToggle = { onFolderToggle(childFolder.id) },
                    onSelect = { onFolderSelect(childFolder.path) },
                    expandedNodeIds = expandedNodeIds,
                    selectedPath = selectedPath,
                    onFolderToggle = onFolderToggle,
                    onFolderSelect = onFolderSelect
                )
            }
        }
    }
}