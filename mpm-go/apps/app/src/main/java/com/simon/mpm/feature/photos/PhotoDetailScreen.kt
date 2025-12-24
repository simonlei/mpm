package com.simon.mpm.feature.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.simon.mpm.network.model.Photo
import java.text.SimpleDateFormat
import java.util.*

/**
 * 照片详情屏幕
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    onBack: () -> Unit,
    viewModel: PhotoDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPhoto by viewModel.photo.collectAsState()

    // 如果照片被删除，返回上一页
    LaunchedEffect(uiState.photoDeleted) {
        if (uiState.photoDeleted) {
            onBack()
        }
    }

    // 如果照片为空且不在加载中，显示错误或返回
    if (currentPhoto == null && !uiState.isLoading) {
        LaunchedEffect(Unit) {
            onBack()
        }
        return
    }

    val displayPhoto = currentPhoto ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(displayPhoto.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    // 收藏按钮
                    IconButton(onClick = { viewModel.toggleStar() }) {
                        Icon(
                            imageVector = if (displayPhoto.star) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.StarBorder
                            },
                            contentDescription = if (displayPhoto.star) "取消收藏" else "收藏",
                            tint = if (displayPhoto.star) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    // 信息按钮
                    IconButton(onClick = { viewModel.toggleInfoPanel() }) {
                        Icon(Icons.Default.Info, "信息")
                    }

                    // 更多菜单
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("向右旋转") },
                            onClick = {
                                viewModel.rotatePhoto(90)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.RotateRight, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("向左旋转") },
                            onClick = {
                                viewModel.rotatePhoto(-90)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.RotateLeft, null)
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("移到回收站") },
                            onClick = {
                                viewModel.moveToTrash()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null)
                            }
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
            // 照片查看器（支持缩放和拖动）
            ZoomableImage(
                imageUrl = displayPhoto.thumb?.replace(Regex("/thumb\\d*$"), "") ?: "",  // 使用原图，移除/thumb及rotate参数
                contentDescription = displayPhoto.name,
                modifier = Modifier.fillMaxSize()
            )

            // 信息面板
            if (uiState.showInfoPanel) {
                PhotoInfoPanel(
                    photo = displayPhoto,
                    onDismiss = { viewModel.toggleInfoPanel() },
                    modifier = Modifier.align(Alignment.BottomCenter)
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
 * 可缩放的图片组件
 */
@Composable
fun ZoomableImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    
                    if (scale > 1f) {
                        offset = Offset(
                            x = (offset.x + pan.x).coerceIn(
                                -size.width * (scale - 1) / 2,
                                size.width * (scale - 1) / 2
                            ),
                            y = (offset.y + pan.y).coerceIn(
                                -size.height * (scale - 1) / 2,
                                size.height * (scale - 1) / 2
                            )
                        )
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        )
    }
}

/**
 * 照片信息面板
 */
@Composable
fun PhotoInfoPanel(
    photo: Photo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "照片信息",
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "关闭")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 照片信息列表
            InfoRow("文件名", photo.name)
            photo.takenDate?.let { InfoRow("拍摄时间", formatDate(it)) }
            InfoRow("尺寸", "${photo.width} × ${photo.height}")
            
            photo.address?.takeIf { it.isNotEmpty() }?.let {
                InfoRow("位置", it)
            }
            
            photo.activityDesc?.takeIf { it.isNotEmpty() }?.let {
                InfoRow("活动", it)
            }
            
            photo.tag?.takeIf { it.isNotEmpty() }?.let {
                InfoRow("标签", it)
            }
        }
    }
}

/**
 * 信息行
 */
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 格式化日期
 */
private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}
