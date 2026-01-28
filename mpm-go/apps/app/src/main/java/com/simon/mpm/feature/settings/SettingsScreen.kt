package com.simon.mpm.feature.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.simon.mpm.data.database.entity.SyncDirectory
import com.simon.mpm.feature.sync.SyncViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SyncViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // 目录选择器
    val directoryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // 授予持久化权限
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            // 添加同步目录
            viewModel.addSyncDirectory(it.toString())
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 自动同步设置区域
            item {
                SyncSettingsSection(
                    uiState = uiState,
                    onAutoSyncToggle = viewModel::toggleAutoSync,
                    onWifiOnlyToggle = viewModel::toggleSyncWifiOnly,
                    onIntervalChange = viewModel::setSyncInterval,
                    onConflictStrategyChange = viewModel::setSyncConflictStrategy,
                    onManualSync = { viewModel.startManualSync(context) },
                    onAddDirectory = { directoryPicker.launch(null) },
                    onDeleteDirectory = viewModel::showDeleteConfirmDialog,
                    onToggleDirectoryEnabled = viewModel::toggleDirectoryEnabled
                )
            }
            
            // 同步统计区域
            item {
                SyncStatisticsSection(
                    uiState = uiState,
                    onRetryFailed = viewModel::retryFailedFiles,
                    onClearHistory = viewModel::clearSyncHistory
                )
            }
            
            // 其他设置区域
            item {
                OtherSettingsSection()
            }
        }
    }
    
    // 删除确认对话框
    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteConfirmDialog,
            title = { Text("删除同步目录") },
            text = { Text("确定要删除此同步目录吗？已同步的文件不会被删除。") },
            confirmButton = {
                TextButton(onClick = viewModel::deleteSyncDirectory) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteConfirmDialog) {
                    Text("取消")
                }
            }
        )
    }
    
    // 错误提示
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // 可以显示Snackbar或Toast
            viewModel.clearError()
        }
    }
}

/**
 * 自动同步设置区域
 */
@Composable
private fun SyncSettingsSection(
    uiState: com.simon.mpm.feature.sync.SyncUiState,
    onAutoSyncToggle: (Boolean) -> Unit,
    onWifiOnlyToggle: (Boolean) -> Unit,
    onIntervalChange: (String) -> Unit,
    onConflictStrategyChange: (String) -> Unit,
    onManualSync: () -> Unit,
    onAddDirectory: () -> Unit,
    onDeleteDirectory: (SyncDirectory) -> Unit,
    onToggleDirectoryEnabled: (SyncDirectory, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 标题
            Text(
                text = "自动同步",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 自动同步开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("启用自动同步")
                    Text(
                        text = "自动备份照片到服务器",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.autoSyncEnabled,
                    onCheckedChange = onAutoSyncToggle
                )
            }
            
            Divider()
            
            // 仅WiFi同步
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("仅WiFi同步")
                    Text(
                        text = "节省移动数据流量",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.syncWifiOnly,
                    onCheckedChange = onWifiOnlyToggle,
                    enabled = uiState.autoSyncEnabled
                )
            }
            
            Divider()
            
            // 同步间隔
            Column {
                Text("同步间隔")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1", "6", "12", "24").forEach { hours ->
                        FilterChip(
                            selected = uiState.syncInterval == hours,
                            onClick = { onIntervalChange(hours) },
                            label = { Text("${hours}小时") },
                            enabled = uiState.autoSyncEnabled
                        )
                    }
                }
            }
            
            Divider()
            
            // 冲突策略
            Column {
                Text("文件冲突处理")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "SKIP" to "跳过",
                        "OVERWRITE" to "覆盖",
                        "RENAME" to "重命名"
                    ).forEach { (strategy, label) ->
                        FilterChip(
                            selected = uiState.syncConflictStrategy == strategy,
                            onClick = { onConflictStrategyChange(strategy) },
                            label = { Text(label) },
                            enabled = uiState.autoSyncEnabled
                        )
                    }
                }
                Text(
                    text = when (uiState.syncConflictStrategy) {
                        "SKIP" -> "服务器已存在相同文件时跳过上传"
                        "OVERWRITE" -> "服务器已存在相同文件时覆盖"
                        "RENAME" -> "服务器已存在相同文件时重命名新文件"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Divider()
            
            // 同步目录
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("同步目录")
                    TextButton(onClick = onAddDirectory) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加")
                    }
                }
                
                if (uiState.syncDirectories.isEmpty()) {
                    Text(
                        text = "未配置同步目录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    uiState.syncDirectories.forEach { directory ->
                        SyncDirectoryItem(
                            directory = directory,
                            onDelete = { onDeleteDirectory(directory) },
                            onToggleEnabled = { enabled ->
                                onToggleDirectoryEnabled(directory, enabled)
                            }
                        )
                    }
                }
            }
            
            Divider()
            
            // 最后同步时间
            if (uiState.lastSyncTime.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("最后同步时间")
                    Text(
                        text = formatSyncTime(uiState.lastSyncTime),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // 手动同步按钮
            Button(
                onClick = onManualSync,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSyncing && uiState.syncDirectories.isNotEmpty()
            ) {
                if (uiState.isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("同步中...")
                } else {
                    Icon(Icons.Default.Sync, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("立即同步")
                }
            }
        }
    }
}

/**
 * 同步目录项
 */
@Composable
private fun SyncDirectoryItem(
    directory: SyncDirectory,
    onDelete: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = directory.directoryPath.substringAfterLast("/"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = directory.directoryPath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = directory.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.height(32.dp)
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 同步统计区域
 */
@Composable
private fun SyncStatisticsSection(
    uiState: com.simon.mpm.feature.sync.SyncUiState,
    onRetryFailed: () -> Unit,
    onClearHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "同步统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // 统计信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("待同步", uiState.pendingCount, MaterialTheme.colorScheme.primary)
                StatItem("同步中", uiState.syncingCount, MaterialTheme.colorScheme.tertiary)
                StatItem("已同步", uiState.syncedCount, MaterialTheme.colorScheme.secondary)
                StatItem("失败", uiState.failedCount, MaterialTheme.colorScheme.error)
            }
            
            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRetryFailed,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.failedCount > 0
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("重试失败")
                }
                OutlinedButton(
                    onClick = onClearHistory,
                    modifier = Modifier.weight(1f),
                    enabled = uiState.syncedCount > 0 || uiState.failedCount > 0
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清除历史")
                }
            }
        }
    }
}

/**
 * 统计项
 */
@Composable
private fun StatItem(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 其他设置区域
 */
@Composable
private fun OtherSettingsSection() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "其他设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "更多设置功能开发中...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 格式化同步时间
 */
private fun formatSyncTime(timeStr: String): String {
    return try {
        val time = timeStr.toLongOrNull() ?: return "未同步"
        if (time == 0L) return "未同步"
        
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(time))
    } catch (e: Exception) {
        "未同步"
    }
}
