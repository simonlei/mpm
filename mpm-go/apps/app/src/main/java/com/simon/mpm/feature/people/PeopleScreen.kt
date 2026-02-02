package com.simon.mpm.feature.people

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.simon.mpm.common.Constants
import com.simon.mpm.feature.photos.PhotoGrid
import com.simon.mpm.feature.photos.StandardPhotoGridItem
import com.simon.mpm.network.model.Face
import com.simon.mpm.network.model.Photo

/**
 * 人脸识别页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(
    onPhotoClick: (Photo) -> Unit = {},
    viewModel: PeopleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val faces by viewModel.faces.collectAsState()
    val selectedFaceId by viewModel.selectedFaceId.collectAsState()
    val facePhotos by viewModel.facePhotos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("人脸") },
                navigationIcon = {
                    if (selectedFaceId != null) {
                        IconButton(onClick = { viewModel.clearSelectedFace() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    }
                },
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
            when {
                selectedFaceId != null -> {
                    // 显示选中人脸的照片
                    PhotoGrid(
                        photos = facePhotos,
                        isLoading = uiState.isLoadingPhotos,
                        hasMore = false,
                        emptyText = "该人物暂无照片",
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
                
                faces.isEmpty() && !uiState.isLoading -> {
                    // 空状态
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FaceRetouchingOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "暂无识别到的人物",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "该功能需要服务端支持",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "服务端会自动识别照片中的人脸",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    // 人脸网格
                    FaceGrid(
                        faces = faces,
                        onFaceClick = { face ->
                            viewModel.selectFace(face.faceId)
                        }
                    )
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
 * 人脸网格
 */
@Composable
private fun FaceGrid(
    faces: List<Face>,
    onFaceClick: (Face) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(faces) { face ->
            FaceItem(
                face = face,
                onClick = { onFaceClick(face) }
            )
        }
    }
}

/**
 * 人脸项
 */
@Composable
private fun FaceItem(
    face: Face,
    onClick: () -> Unit
) {
    val viewModel: PeopleViewModel = hiltViewModel()
    val serverUrl by viewModel.serverUrl.collectAsState()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 人脸图片
            val faceImageUrl = "$serverUrl${Constants.FACE_IMG_PATH}/${face.faceId}/${face.selectedFace}"
            
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(faceImageUrl)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = face.name ?: "未命名",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 名称
            Text(
                text = face.name?.ifEmpty { "未命名" } ?: "未命名",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            
            // 照片数量
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${face.count}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
