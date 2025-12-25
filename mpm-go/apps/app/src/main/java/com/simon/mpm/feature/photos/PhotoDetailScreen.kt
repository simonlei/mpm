package com.simon.mpm.feature.photos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PhotoDetailScreen(
    onBack: () -> Unit,
    viewModel: PhotoDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPhoto by viewModel.photo.collectAsState()
    val photoList by viewModel.photoList.collectAsState()
    val currentPhotoIndex by viewModel.currentPhotoIndex.collectAsState()

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

    // 创建 Pager 状态
    val pagerState = rememberPagerState(
        initialPage = currentPhotoIndex,
        pageCount = { photoList.size }
    )

    // 监听 Pager 页面变化
    LaunchedEffect(pagerState.currentPage) {
        android.util.Log.d("PhotoDetail", "Pager currentPage changed to: ${pagerState.currentPage}, photoList size: ${photoList.size}")
        if (pagerState.currentPage != currentPhotoIndex && photoList.isNotEmpty()) {
            android.util.Log.d("PhotoDetail", "Switching to photo at index: ${pagerState.currentPage}")
            viewModel.switchToPhoto(pagerState.currentPage)
        }
    }

    // 当 ViewModel 中的索引变化时，同步 Pager
    LaunchedEffect(currentPhotoIndex) {
        android.util.Log.d("PhotoDetail", "ViewModel currentPhotoIndex changed to: $currentPhotoIndex")
        if (pagerState.currentPage != currentPhotoIndex && photoList.isNotEmpty()) {
            android.util.Log.d("PhotoDetail", "Scrolling pager to: $currentPhotoIndex")
            pagerState.scrollToPage(currentPhotoIndex)
        }
    }

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
                        // 旋转功能（所有照片都可用）
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
                        
                        // 根据来源显示不同的操作
                        if (viewModel.fromTrash) {
                            // 回收站照片：显示恢复和永久删除
                            DropdownMenuItem(
                                text = { Text("恢复照片") },
                                onClick = {
                                    viewModel.restorePhoto()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.RestoreFromTrash, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("永久删除") },
                                onClick = {
                                    viewModel.permanentlyDelete()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteForever, null)
                                }
                            )
                        } else {
                            // 普通照片：显示移到回收站
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
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 使用 HorizontalPager 实现左右滑动切换
            if (photoList.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val photo = photoList.getOrNull(page)
                    if (photo != null) {
                        // 照片查看器（支持缩放和拖动）
                        ZoomableImage(
                            imageUrl = photo.thumb?.replace(Regex("/thumb\\d*$"), "") ?: "",  // 使用原图，移除/thumb参数
                            contentDescription = photo.name,
                            rotate = photo.rotate.toFloat(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                // 如果列表还没加载，显示单张照片
                ZoomableImage(
                    imageUrl = displayPhoto.thumb?.replace(Regex("/thumb\\d*$"), "") ?: "",
                    contentDescription = displayPhoto.name,
                    rotate = displayPhoto.rotate.toFloat(),
                    modifier = Modifier.fillMaxSize()
                )
            }

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
 * 支持双指缩放和拖动，不拦截HorizontalPager的滑动手势
 */
@Composable
fun ZoomableImage(
    imageUrl: String,
    contentDescription: String?,
    rotate: Float = 0f,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
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
                    translationY = offset.y,
                    rotationZ = rotate
                )
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        
                        do {
                            val event = awaitPointerEvent()
                            
                            // 只有在双指或更多手指时才处理缩放
                            if (event.changes.size >= 2) {
                                // 消费事件，防止传递给 Pager
                                event.changes.forEach { it.consume() }
                                
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                
                                // 更新缩放
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                
                                // 更新偏移
                                if (scale > 1f) {
                                    offset += pan
                                } else {
                                    offset = Offset.Zero
                                }
                            } else if (scale > 1f) {
                                // 如果已经缩放了，单指拖动也要消费事件
                                event.changes.forEach { it.consume() }
                                val pan = event.calculatePan()
                                offset += pan
                            }
                            // 如果是单指且未缩放，不消费事件，让 Pager 处理
                            
                        } while (event.changes.any { it.pressed })
                    }
                }
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
