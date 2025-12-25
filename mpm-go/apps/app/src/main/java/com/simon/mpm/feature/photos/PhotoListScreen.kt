package com.simon.mpm.feature.photos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.simon.mpm.network.model.Photo

/**
 * 照片列表屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreen(
    onPhotoClick: (Photo) -> Unit,
    onNavigateToTrash: () -> Unit = {},
    onLogout: () -> Unit = {},
    shouldRefresh: Boolean = false,
    viewModel: PhotoListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val photos by viewModel.photos.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // 监听刷新信号
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            android.util.Log.d("PhotoListScreen", "Received refresh signal, refreshing list")
            viewModel.refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (uiState.totalRows > 0) {
                            "照片 (${uiState.totalRows})"
                        } else {
                            "照片"
                        }
                    )
                },
                actions = {
                    // 排序按钮
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "排序"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            // ID排序
                            DropdownMenuItem(
                                text = { Text("ID 降序") },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.setSortOrder("-id")
                                },
                                trailingIcon = if (uiState.sortOrder == "-id") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            DropdownMenuItem(
                                text = { Text("ID 升序") },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.setSortOrder("id")
                                },
                                trailingIcon = if (uiState.sortOrder == "id") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            
                            Divider()
                            
                            // 拍摄日期排序
                            DropdownMenuItem(
                                text = { Text("日期 降序") },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.setSortOrder("-taken_date")
                                },
                                trailingIcon = if (uiState.sortOrder == "-taken_date") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            DropdownMenuItem(
                                text = { Text("日期 升序") },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.setSortOrder("taken_date")
                                },
                                trailingIcon = if (uiState.sortOrder == "taken_date") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            
                            Divider()
                            
                            // 文件大小排序
                            DropdownMenuItem(
                                text = { Text("大小 降序") },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.setSortOrder("-size")
                                },
                                trailingIcon = if (uiState.sortOrder == "-size") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            DropdownMenuItem(
                                text = { Text("大小 升序") },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.setSortOrder("size")
                                },
                                trailingIcon = if (uiState.sortOrder == "size") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            
                            Divider()
                            
                            // 宽度排序
                            DropdownMenuItem(
                                text = { Text("宽度 降序") },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.setSortOrder("-width")
                                },
                                trailingIcon = if (uiState.sortOrder == "-width") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            DropdownMenuItem(
                                text = { Text("宽度 升序") },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.setSortOrder("width")
                                },
                                trailingIcon = if (uiState.sortOrder == "width") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            
                            Divider()
                            
                            // 高度排序
                            DropdownMenuItem(
                                text = { Text("高度 降序") },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.setSortOrder("-height")
                                },
                                trailingIcon = if (uiState.sortOrder == "-height") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                            DropdownMenuItem(
                                text = { Text("高度 升序") },
                                onClick = {
                                    showSortMenu = false
                                    viewModel.setSortOrder("height")
                                },
                                trailingIcon = if (uiState.sortOrder == "height") {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                    // 刷新按钮
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新"
                        )
                    }
                    
                    // 更多菜单
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "更多"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("回收站") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToTrash()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null
                                    )
                                }
                            )
                            
                            Divider()
                            
                            DropdownMenuItem(
                                text = { Text("退出登录") },
                                onClick = {
                                    showMenu = false
                                    viewModel.logout()
                                    onLogout()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 筛选栏
            FilterBar(
                filterStar = uiState.filterStar,
                filterVideo = uiState.filterVideo,
                onFilterStarChange = { viewModel.toggleStarFilter() },
                onFilterVideoChange = { viewModel.toggleVideoFilter() }
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // 使用通用PhotoGrid组件
                PhotoGrid(
                    photos = photos,
                    isLoading = uiState.isLoading,
                    hasMore = uiState.hasMore,
                    emptyText = "暂无照片",
                    onPhotoClick = onPhotoClick,
                    onLoadMore = { viewModel.loadMore() },
                    onRefresh = { viewModel.refresh() },
                    photoItemContent = { photo ->
                        StandardPhotoGridItem(
                            photo = photo,
                            onStarClick = { viewModel.toggleStar(photo) }
                        )
                    }
                )

                // 刷新指示器
                if (uiState.isRefreshing) {
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
    }
}

/**
 * 筛选栏组件
 */
@Composable
fun FilterBar(
    filterStar: Boolean,
    filterVideo: Boolean,
    onFilterStarChange: () -> Unit,
    onFilterVideoChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 收藏筛选
            FilterChip(
                selected = filterStar,
                onClick = onFilterStarChange,
                label = { Text("收藏") },
                leadingIcon = if (filterStar) {
                    {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
            
            // 视频筛选
            FilterChip(
                selected = filterVideo,
                onClick = onFilterVideoChange,
                label = { Text("视频") },
                leadingIcon = if (filterVideo) {
                    {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}
