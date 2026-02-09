package com.simon.mpm.feature.trash

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.simon.mpm.feature.photos.PhotoGrid
import com.simon.mpm.feature.photos.PhotoListViewModel
import com.simon.mpm.feature.photos.TrashPhotoGridItem

/**
 * 回收站屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onBack: () -> Unit,
    onPhotoClick: (Int) -> Unit = {},
    shouldRefresh: Boolean = false,
    viewModel: PhotoListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    
    // 监听刷新信号
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            android.util.Log.d("TrashScreen", "Received refresh signal, refreshing trash list")
            viewModel.refresh()
        }
    }
    
    // 显示操作结果
    LaunchedEffect(uiState.restoreSuccess) {
        if (uiState.restoreSuccess) {
            viewModel.clearRestoreSuccess()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // 可以显示Snackbar或其他错误提示
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = if (totalCount > 0) {
                            "回收站 ($totalCount)"
                        } else {
                            "回收站"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 清空回收站按钮
                    IconButton(
                        onClick = { 
                            if (photos.isNotEmpty()) {
                                viewModel.emptyTrash()
                            }
                        },
                        enabled = photos.isNotEmpty() && !uiState.isLoading
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "清空回收站")
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
            // 使用通用PhotoGrid组件
            PhotoGrid(
                photos = photos,
                isLoading = uiState.isLoading,
                hasMore = uiState.hasMore,
                emptyText = if (totalCount == 0) "回收站为空\n删除的照片会在这里显示" else "正在加载...",
                emptyIcon = Icons.Default.DeleteForever,
                onPhotoClick = { photo -> onPhotoClick(photo.id) },
                onLoadMore = { viewModel.loadMore() },
                onRefresh = { viewModel.refresh() },
                photoItemContent = { photo ->
                    TrashPhotoGridItem(
                        photo = photo,
                        onRestore = { viewModel.restorePhotos(listOf(photo.id)) }
                    )
                }
            )
            
            // 清空回收站进度
            uiState.emptyTrashTaskId?.let { taskId ->
                if (!uiState.emptyTrashCompleted) {
                    TrashProgressDialog(
                        progress = uiState.emptyTrashProgress,
                        total = uiState.emptyTrashTotal,
                        onDismiss = { }
                    )
                }
            }
        }
    }
}

/**
 * 清空回收站进度对话框
 */
@Composable
private fun TrashProgressDialog(
    progress: Int,
    total: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("清空回收站")
        },
        text = {
            Column {
                Text("正在永久删除照片...")
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = if (total > 0) progress / 100f else 0f,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (total > 0) "${progress / 100 * total} / $total" else "准备中...",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            // 不显示确认按钮，让进度自然完成
        }
    )
}