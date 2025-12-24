package com.simon.mpm.feature.photos

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
    viewModel: PhotoListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val photos by viewModel.photos.collectAsState()

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
