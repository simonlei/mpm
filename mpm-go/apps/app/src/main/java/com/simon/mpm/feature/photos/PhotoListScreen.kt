package com.simon.mpm.feature.photos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.simon.mpm.network.model.Photo

/**
 * 照片列表屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreen(
    onPhotoClick: (Photo) -> Unit,
    viewModel: PhotoListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val gridState = rememberLazyGridState()

    // 监听滚动到底部，加载更多
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && 
                    lastVisibleIndex >= photos.size - 10 && 
                    uiState.hasMore && 
                    !uiState.isLoading) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("照片") },
                actions = {
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
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (photos.isEmpty() && !uiState.isLoading) {
                // 空状态
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "暂无照片",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { viewModel.refresh() }) {
                            Text("刷新")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    state = gridState,
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(photos, key = { it.id }) { photo ->
                        PhotoGridItem(
                            photo = photo,
                            onClick = { onPhotoClick(photo) },
                            onStarClick = { viewModel.toggleStar(photo) }
                        )
                    }

                    // 加载更多指示器
                    if (uiState.isLoading && photos.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

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

/**
 * 照片网格项
 */
@Composable
fun PhotoGridItem(
    photo: Photo,
    onClick: () -> Unit,
    onStarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    android.util.Log.d("PhotoGridItem", "Rendering photo: id=${photo.id}, name=${photo.name}, thumb=${photo.thumb}")
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        // 照片缩略图
        AsyncImage(
            model = photo.thumb,
            contentDescription = photo.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            onError = { error ->
                android.util.Log.e("PhotoGridItem", "Image load error for ${photo.name}: ${error.result.throwable.message}")
            },
            onSuccess = {
                android.util.Log.d("PhotoGridItem", "Image loaded successfully: ${photo.name}")
            }
        )

        // 收藏图标
        if (photo.star) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "已收藏",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(20.dp)
                    .clickable(onClick = onStarClick)
            )
        }
    }
}
