package com.simon.mpm.feature.trash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.simon.mpm.common.Constants
import com.simon.mpm.network.model.Photo
import kotlinx.coroutines.launch

/**
 * 回收站页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onBack: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    
    val serverUrl = "http://127.0.0.1:8080" // 临时硬编码，实际应该从PreferencesManager获取
    
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
                title = { Text("回收站") },
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
            when {
                uiState.isLoading && photos.isEmpty() -> {
                    // 加载状态
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                photos.isEmpty() && !uiState.isLoading -> {
                    // 空状态
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (totalCount == 0) "回收站为空" else "正在加载...",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (totalCount == 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "删除的照片会在这里显示",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                else -> {
                    // 照片网格
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(photos) { photo ->
                            TrashPhotoItem(
                                photo = photo,
                                serverUrl = serverUrl,
                                onRestore = { viewModel.restorePhotos(listOf(photo.id)) }
                            )
                        }
                        
                        // 加载更多
                        item {
                            if (uiState.hasMore && !uiState.isLoading) {
                                LaunchedEffect(Unit) {
                                    viewModel.loadPhotos(photos.size)
                                }
                            }
                        }
                    }
                }
            }
            
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
            
            // 底部加载指示器
            if (uiState.isLoading && photos.isNotEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

/**
 * 回收站照片项
 */
@Composable
private fun TrashPhotoItem(
    photo: Photo,
    serverUrl: String,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth(),
        onClick = onRestore
    ) {
        Box {
            // 照片缩略图
            AsyncImage(
                model = "$serverUrl${Constants.COS_PATH}${photo.thumb}",
                contentDescription = photo.name,
                modifier = Modifier.fillMaxSize()
            )
            
            // 恢复按钮覆盖层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                IconButton(
                    onClick = onRestore,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "恢复",
                        tint = MaterialTheme.colorScheme.primary
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