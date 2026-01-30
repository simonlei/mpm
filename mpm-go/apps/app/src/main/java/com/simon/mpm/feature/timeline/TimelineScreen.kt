package com.simon.mpm.feature.timeline

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.simon.mpm.feature.photos.PhotoGrid
import com.simon.mpm.feature.photos.StandardPhotoGridItem
import com.simon.mpm.network.model.Photo
import com.simon.mpm.network.model.TreeNode

/**
 * 时间轴页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onPhotoClick: (Photo) -> Unit = {},
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val timeTree by viewModel.timeTree.collectAsState()
    val expandedNodes by viewModel.expandedNodes.collectAsState()
    val selectedDateKey by viewModel.selectedDateKey.collectAsState()
    val monthPhotos by viewModel.monthPhotos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("时间轴") },
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
            if (selectedDateKey != null) {
                // 显示选中月份的照片
                Column(modifier = Modifier.fillMaxSize()) {
                    // 返回按钮
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.clearSelectedMonth() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "返回"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formatDateKey(selectedDateKey!!),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    // 照片网格
                    PhotoGrid(
                        photos = monthPhotos,
                        isLoading = uiState.isLoadingPhotos,
                        hasMore = false,
                        emptyText = "该月暂无照片",
                        onPhotoClick = onPhotoClick,
                        onLoadMore = {},
                        onRefresh = { viewModel.refresh() },
                        photoItemContent = { photo ->
                            StandardPhotoGridItem(
                                photo = photo,
                                onStarClick = {}
                            )
                        }
                    )
                }
            } else {
                // 显示时间树
                if (timeTree.isEmpty() && !uiState.isLoading) {
                    // 空状态
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "暂无照片",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // 时间树列表
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(timeTree) { yearNode ->
                            TimeTreeYearItem(
                                yearNode = yearNode,
                                isExpanded = yearNode.id in expandedNodes,
                                onToggle = { viewModel.toggleNode(yearNode.id) },
                                onMonthClick = { monthNode ->
                                    val dateKey = String.format("%04d%02d", yearNode.year, monthNode.month)
                                    viewModel.loadMonthPhotos(dateKey)
                                }
                            )
                        }
                    }
                }
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
}

/**
 * 年份节点项
 */
@Composable
fun TimeTreeYearItem(
    yearNode: TreeNode,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onMonthClick: (TreeNode) -> Unit
) {
    Column {
        // 年份标题
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() },
            tonalElevation = if (isExpanded) 2.dp else 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "收起" else "展开"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${yearNode.year}年",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = yearNode.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // 月份列表
        if (isExpanded && yearNode.children != null) {
            yearNode.children.forEach { monthNode ->
                TimeTreeMonthItem(
                    monthNode = monthNode,
                    onClick = { onMonthClick(monthNode) }
                )
            }
        }
    }
}

/**
 * 月份节点项
 */
@Composable
fun TimeTreeMonthItem(
    monthNode: TreeNode,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 48.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${monthNode.month}月",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = monthNode.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 格式化dateKey为可读文本
 */
fun formatDateKey(dateKey: String): String {
    return when (dateKey.length) {
        6 -> {
            val year = dateKey.substring(0, 4)
            val month = dateKey.substring(4, 6)
            "${year}年${month}月"
        }
        4 -> "${dateKey}年"
        else -> dateKey
    }
}
