package com.simon.mpm.feature.photos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: PhotoListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val activities by viewModel.activities.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
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
                        // 收藏筛选按钮
                        IconButton(onClick = { viewModel.toggleStarFilter() }) {
                            Badge(
                                containerColor = if (uiState.filterStar) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "收藏"
                                )
                            }
                        }
                        
                        // 视频筛选按钮
                        IconButton(onClick = { viewModel.toggleVideoFilter() }) {
                            Badge(
                                containerColor = if (uiState.filterVideo) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoLibrary,
                                    contentDescription = "视频"
                                )
                            }
                        }
                        
                        // 高级筛选按钮
                        IconButton(onClick = { showFilterDialog = true }) {
                            Badge(
                                containerColor = if (uiState.hasActiveFilters) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "高级筛选"
                                )
                            }
                        }
                        
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
                                    text = { Text("设置") },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToSettings()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null
                                        )
                                    }
                                )
                                
                                Divider()
                                
                                DropdownMenuItem(
                                    text = { Text("退出登录") },
                                    onClick = {
                                        showMenu = false
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
                
                // 活动筛选条件显示
                if (uiState.hasActiveFilters) {
                    ActiveFiltersBar(
                        uiState = uiState,
                        activities = activities,
                        onClearFilters = { viewModel.clearAllFilters() }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
    
    // 高级筛选对话框
    if (showFilterDialog) {
        AdvancedFilterDialog(
            uiState = uiState,
            activities = activities,
            allTags = allTags,
            onDismiss = { showFilterDialog = false },
            onApplyFilters = { dateKey, tag, path ->
                viewModel.applyAdvancedFilters(dateKey, tag, path)
                showFilterDialog = false
            },
            onClearFilters = {
                viewModel.clearAllFilters()
                showFilterDialog = false
            }
        )
    }
}

/**
 * 活动筛选条件显示栏
 */
@Composable
fun ActiveFiltersBar(
    uiState: PhotoListUiState,
    activities: List<com.simon.mpm.network.model.Activity>,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "筛选:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            // 日期筛选
            if (uiState.filterDateKey.isNotEmpty()) {
                val dateLabel = when {
                    uiState.filterDateKey.toIntOrNull()?.let { it >= 1000000 } == true -> {
                        val activityId = uiState.filterDateKey.toInt() - 1000000
                        activities.find { it.id == activityId }?.name ?: "活动"
                    }
                    uiState.filterDateKey.length == 6 -> {
                        val year = uiState.filterDateKey.substring(0, 4)
                        val month = uiState.filterDateKey.substring(4, 6)
                        "${year}年${month}月"
                    }
                    uiState.filterDateKey.length == 4 -> {
                        "${uiState.filterDateKey}年"
                    }
                    else -> uiState.filterDateKey
                }
                
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text(dateLabel) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
            
            // 标签筛选
            if (uiState.filterTag.isNotEmpty()) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("标签: ${uiState.filterTag}") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
            
            // 路径筛选
            if (uiState.filterPath.isNotEmpty()) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("路径: ${uiState.filterPath}") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // 清除所有筛选
            TextButton(onClick = onClearFilters) {
                Text("清除")
            }
        }
    }
}

/**
 * 高级筛选对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFilterDialog(
    uiState: PhotoListUiState,
    activities: List<com.simon.mpm.network.model.Activity>,
    allTags: List<String>,
    onDismiss: () -> Unit,
    onApplyFilters: (dateKey: String, tag: String, path: String) -> Unit,
    onClearFilters: () -> Unit
) {
    var selectedDateKey by remember { mutableStateOf(uiState.filterDateKey) }
    var selectedTag by remember { mutableStateOf(uiState.filterTag) }
    var selectedPath by remember { mutableStateOf(uiState.filterPath) }
    var expandedDateMenu by remember { mutableStateOf(false) }
    var expandedTagMenu by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("高级筛选") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 日期筛选
                item {
                    Column {
                        Text(
                            text = "按日期/活动筛选",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = expandedDateMenu,
                            onExpandedChange = { expandedDateMenu = it }
                        ) {
                            OutlinedTextField(
                                value = when {
                                    selectedDateKey.isEmpty() -> "全部"
                                    selectedDateKey.toIntOrNull()?.let { it >= 1000000 } == true -> {
                                        val activityId = selectedDateKey.toInt() - 1000000
                                        activities.find { it.id == activityId }?.name ?: "活动"
                                    }
                                    selectedDateKey.length == 6 -> {
                                        val year = selectedDateKey.substring(0, 4)
                                        val month = selectedDateKey.substring(4, 6)
                                        "${year}年${month}月"
                                    }
                                    selectedDateKey.length == 4 -> "${selectedDateKey}年"
                                    else -> selectedDateKey
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("选择日期/活动") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDateMenu) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expandedDateMenu,
                                onDismissRequest = { expandedDateMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("全部") },
                                    onClick = {
                                        selectedDateKey = ""
                                        expandedDateMenu = false
                                    }
                                )
                                
                                if (activities.isNotEmpty()) {
                                    Divider()
                                    activities.forEach { activity ->
                                        DropdownMenuItem(
                                            text = { Text(activity.name) },
                                            onClick = {
                                                selectedDateKey = (activity.id + 1000000).toString()
                                                expandedDateMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 标签筛选
                item {
                    Column {
                        Text(
                            text = "按标签筛选",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        ExposedDropdownMenuBox(
                            expanded = expandedTagMenu,
                            onExpandedChange = { expandedTagMenu = it }
                        ) {
                            OutlinedTextField(
                                value = if (selectedTag.isEmpty()) "全部" else selectedTag,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("选择标签") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTagMenu) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expandedTagMenu,
                                onDismissRequest = { expandedTagMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("全部") },
                                    onClick = {
                                        selectedTag = ""
                                        expandedTagMenu = false
                                    }
                                )
                                
                                if (allTags.isNotEmpty()) {
                                    Divider()
                                    allTags.forEach { tag ->
                                        DropdownMenuItem(
                                            text = { Text(tag) },
                                            onClick = {
                                                selectedTag = tag
                                                expandedTagMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 路径筛选
                item {
                    Column {
                        Text(
                            text = "按路径筛选",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = selectedPath,
                            onValueChange = { selectedPath = it },
                            label = { Text("输入路径") },
                            placeholder = { Text("例如: /2024/春游") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onApplyFilters(selectedDateKey, selectedTag, selectedPath)
                }
            ) {
                Text("应用")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onClearFilters) {
                    Text("清除筛选")
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
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