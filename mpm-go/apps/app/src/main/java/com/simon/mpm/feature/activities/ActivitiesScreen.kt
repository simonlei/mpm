package com.simon.mpm.feature.activities

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import com.simon.mpm.network.model.Activity

/**
 * 活动列表页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    viewModel: ActivitiesViewModel = hiltViewModel(),
    onActivityClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Log.d(TAG, "[ActivitiesScreen] Composable 重组, activities.size=${uiState.activities.size}, isLoading=${uiState.isLoading}")
    
    // 页面显示时自动加载列表
    LaunchedEffect(Unit) {
        Log.d(TAG, "[ActivitiesScreen] LaunchedEffect(Unit) 触发")
        viewModel.loadActivities()
        Log.d(TAG, "[ActivitiesScreen] LaunchedEffect(Unit) viewModel.loadActivities() 已调用")
    }
    
    // 显示错误消息
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // 这里可以显示Snackbar
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("活动 (${uiState.activities.size})") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onActivityClick(0) } // 0表示创建新活动
            ) {
                Icon(Icons.Default.Add, contentDescription = "创建活动")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.activities.isEmpty() -> {
                    // 首次加载显示进度条
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.activities.isEmpty() -> {
                    // 空状态
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "暂无活动",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "点击右下角按钮创建活动",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    // 活动列表
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.activities,
                            key = { it.id }
                        ) { activity ->
                            ActivityItem(
                                activity = activity,
                                onClick = { onActivityClick(activity.id) },
                                onDelete = { viewModel.deleteActivity(activity.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 活动列表项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityItem(
    activity: Activity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除活动",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // 描述
            if (!activity.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // 日期和位置信息
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 日期
                if (!activity.start_date.isNullOrBlank() || !activity.end_date.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when {
                                !activity.start_date.isNullOrBlank() && !activity.end_date.isNullOrBlank() ->
                                    "${activity.start_date} ~ ${activity.end_date}"
                                !activity.start_date.isNullOrBlank() -> activity.start_date
                                else -> activity.end_date ?: ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 位置
                if (activity.latitude != null && activity.longitude != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "%.4f, %.4f".format(activity.latitude, activity.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // 照片数量
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${activity.photoCount} 张照片",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除活动") },
            text = { Text("确定要删除活动「${activity.name}」吗？此操作不会删除照片。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        Log.d(TAG, "[ActivityItem] 用户确认删除活动: ${activity.name} (ID: ${activity.id})")
                        showDeleteDialog = false
                        onDelete()
                        Log.d(TAG, "[ActivityItem] onDelete() 已调用")
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

private const val TAG = "ActivitiesScreen"