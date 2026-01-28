package com.simon.mpm.feature.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.data.database.entity.SyncDirectory
import com.simon.mpm.data.database.entity.SyncFile
import com.simon.mpm.data.database.entity.SyncStatus
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.data.repository.SyncRepository
import com.simon.mpm.service.MediaContentObserver
import com.simon.mpm.service.PhotoSyncService
import com.simon.mpm.util.SyncWorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 同步UI状态
 */
data class SyncUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val syncDirectories: List<SyncDirectory> = emptyList(),
    val syncFiles: List<SyncFile> = emptyList(),
    val pendingCount: Int = 0,
    val syncingCount: Int = 0,
    val syncedCount: Int = 0,
    val failedCount: Int = 0,
    val autoSyncEnabled: Boolean = false,
    val syncWifiOnly: Boolean = false,
    val syncInterval: String = "24",
    val lastSyncTime: String = "",
    val errorMessage: String? = null,
    val showAddDirectoryDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val directoryToDelete: SyncDirectory? = null
)

/**
 * 同步ViewModel
 */
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncRepository: SyncRepository,
    private val preferencesManager: PreferencesManager,
    private val syncWorkManager: SyncWorkManager,
    private val mediaContentObserver: MediaContentObserver
) : ViewModel() {

    companion object {
        private const val TAG = "SyncViewModel"
    }

    private val _uiState = MutableStateFlow(SyncUiState())
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    init {
        loadSyncConfig()
        loadSyncDirectories()
        loadSyncStatistics()
    }

    /**
     * 加载同步配置
     */
    private fun loadSyncConfig() {
        viewModelScope.launch {
            try {
                val autoSyncEnabled = preferencesManager.autoSyncEnabled.first()
                val syncWifiOnly = preferencesManager.syncWifiOnly.first()
                val syncInterval = preferencesManager.syncInterval.first()
                val lastSyncTime = preferencesManager.lastSyncTime.first() ?: ""

                _uiState.update { state ->
                    state.copy(
                        autoSyncEnabled = autoSyncEnabled,
                        syncWifiOnly = syncWifiOnly,
                        syncInterval = syncInterval,
                        lastSyncTime = lastSyncTime
                    )
                }

                Log.d(TAG, "同步配置已加载: autoSync=$autoSyncEnabled, wifiOnly=$syncWifiOnly, interval=$syncInterval")
            } catch (e: Exception) {
                Log.e(TAG, "加载同步配置失败", e)
                _uiState.update { state ->
                    state.copy(errorMessage = "加载配置失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 加载同步目录列表
     */
    private fun loadSyncDirectories() {
        viewModelScope.launch {
            try {
                syncRepository.getAllSyncDirectories().collect { directories ->
                    _uiState.update { state ->
                        state.copy(syncDirectories = directories)
                    }
                    Log.d(TAG, "同步目录已加载: ${directories.size} 个")
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载同步目录失败", e)
                _uiState.update { state ->
                    state.copy(errorMessage = "加载目录失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 加载同步统计信息
     */
    private fun loadSyncStatistics() {
        viewModelScope.launch {
            try {
                val counts = syncRepository.getStatusCounts()
                _uiState.update { state ->
                    state.copy(
                        pendingCount = counts[SyncStatus.PENDING] ?: 0,
                        syncingCount = counts[SyncStatus.SYNCING] ?: 0,
                        syncedCount = counts[SyncStatus.SYNCED] ?: 0,
                        failedCount = counts[SyncStatus.FAILED] ?: 0
                    )
                }
                Log.d(TAG, "同步统计已加载: pending=${counts[SyncStatus.PENDING]}, synced=${counts[SyncStatus.SYNCED]}, failed=${counts[SyncStatus.FAILED]}")
            } catch (e: Exception) {
                Log.e(TAG, "加载同步统计失败", e)
            }
        }
    }

    /**
     * 切换自动同步开关
     */
    fun toggleAutoSync(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setAutoSyncEnabled(enabled)
                _uiState.update { state ->
                    state.copy(autoSyncEnabled = enabled)
                }

                // 根据开关状态调度或取消同步任务
                if (enabled) {
                    val interval = preferencesManager.syncInterval.first().toLongOrNull() ?: 24
                    val wifiOnly = preferencesManager.syncWifiOnly.first()
                    syncWorkManager.scheduleSyncWork(
                        intervalHours = interval,
                        requireWifi = wifiOnly,
                        requireCharging = false,
                        requireBatteryNotLow = true
                    )
                    // 注册媒体内容观察者
                    mediaContentObserver.register()
                    Log.d(TAG, "自动同步已启用，任务已调度，观察者已注册")
                } else {
                    syncWorkManager.cancelSyncWork()
                    // 取消注册媒体内容观察者
                    mediaContentObserver.unregister()
                    Log.d(TAG, "自动同步已禁用，任务已取消，观察者已取消注册")
                }
            } catch (e: Exception) {
                Log.e(TAG, "切换自动同步失败", e)
                _uiState.update { state ->
                    state.copy(errorMessage = "操作失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 切换仅WiFi同步
     */
    fun toggleSyncWifiOnly(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setSyncWifiOnly(enabled)
                _uiState.update { state ->
                    state.copy(syncWifiOnly = enabled)
                }

                // 如果自动同步已启用，重新调度任务
                if (_uiState.value.autoSyncEnabled) {
                    val interval = preferencesManager.syncInterval.first().toLongOrNull() ?: 24
                    syncWorkManager.scheduleSyncWork(
                        intervalHours = interval,
                        requireWifi = enabled,
                        requireCharging = false,
                        requireBatteryNotLow = true
                    )
                    Log.d(TAG, "WiFi限制已更新，任务已重新调度")
                }
            } catch (e: Exception) {
                Log.e(TAG, "切换WiFi限制失败", e)
                _uiState.update { state ->
                    state.copy(errorMessage = "操作失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 设置同步间隔
     */
    fun setSyncInterval(hours: String) {
        viewModelScope.launch {
            try {
                preferencesManager.setSyncInterval(hours)
                _uiState.update { state ->
                    state.copy(syncInterval = hours)
                }

                // 如果自动同步已启用，重新调度任务
                if (_uiState.value.autoSyncEnabled) {
                    val wifiOnly = preferencesManager.syncWifiOnly.first()
                    val intervalHours = hours.toLongOrNull() ?: 24
                    syncWorkManager.scheduleSyncWork(
                        intervalHours = intervalHours,
                        requireWifi = wifiOnly,
                        requireCharging = false,
                        requireBatteryNotLow = true
                    )
                    Log.d(TAG, "同步间隔已更新为 $intervalHours 小时，任务已重新调度")
                }
            } catch (e: Exception) {
                Log.e(TAG, "设置同步间隔失败", e)
                _uiState.update { state ->
                    state.copy(errorMessage = "操作失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 手动触发同步
     */
    fun startManualSync(context: Context) {
        if (_uiState.value.isSyncing) {
            Log.w(TAG, "同步正在进行中，忽略请求")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { state ->
                    state.copy(isSyncing = true, errorMessage = null)
                }

                // 启动同步服务
                PhotoSyncService.startSync(context)
                Log.d(TAG, "手动同步已启动")

                // 刷新统计信息
                loadSyncStatistics()
            } catch (e: Exception) {
                Log.e(TAG, "启动手动同步失败", e)
                _uiState.update { state ->
                    state.copy(
                        isSyncing = false,
                        errorMessage = "启动同步失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 停止同步
     */
    fun stopSync(context: Context) {
        viewModelScope.launch {
            try {
                PhotoSyncService.stopSync(context)
                _uiState.update { state ->
                    state.copy(isSyncing = false)
                }
                Log.d(TAG, "同步已停止")
            } catch (e: Exception) {
                Log.e(TAG, "停止同步失败", e)
            }
        }
    }

    /**
     * 添加同步目录
     */
    fun addSyncDirectory(directoryPath: String) {
        viewModelScope.launch {
            try {
                _uiState.update { state ->
                    state.copy(isLoading = true, errorMessage = null)
                }

                val result = syncRepository.addSyncDirectory(directoryPath)
                if (result.isSuccess) {
                    Log.d(TAG, "同步目录已添加: $directoryPath")
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            showAddDirectoryDialog = false
                        )
                    }
                    // 刷新目录列表
                    loadSyncDirectories()
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "添加目录失败: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "添加同步目录失败", e)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "添加目录失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 显示删除确认对话框
     */
    fun showDeleteConfirmDialog(directory: SyncDirectory) {
        _uiState.update { state ->
            state.copy(
                showDeleteConfirmDialog = true,
                directoryToDelete = directory
            )
        }
    }

    /**
     * 删除同步目录
     */
    fun deleteSyncDirectory() {
        val directory = _uiState.value.directoryToDelete ?: return

        viewModelScope.launch {
            try {
                _uiState.update { state ->
                    state.copy(isLoading = true, errorMessage = null)
                }

                val result = syncRepository.removeSyncDirectory(directory)
                if (result.isSuccess) {
                    Log.d(TAG, "同步目录已删除: ${directory.directoryPath}")
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            showDeleteConfirmDialog = false,
                            directoryToDelete = null
                        )
                    }
                    // 刷新目录列表
                    loadSyncDirectories()
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "删除目录失败: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "删除同步目录失败", e)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "删除目录失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 切换目录启用状态
     */
    fun toggleDirectoryEnabled(directory: SyncDirectory, enabled: Boolean) {
        viewModelScope.launch {
            try {
                val result = syncRepository.updateDirectoryEnabled(directory.id, enabled)
                if (result.isSuccess) {
                    Log.d(TAG, "目录状态已更新: ${directory.directoryPath}, enabled=$enabled")
                } else {
                    _uiState.update { state ->
                        state.copy(errorMessage = "更新目录状态失败: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "更新目录状态失败", e)
                _uiState.update { state ->
                    state.copy(errorMessage = "更新目录状态失败: ${e.message}")
                }
            }
        }
    }

    /**
     * 重试失败的文件
     */
    fun retryFailedFiles() {
        viewModelScope.launch {
            try {
                _uiState.update { state ->
                    state.copy(isLoading = true, errorMessage = null)
                }

                val result = syncRepository.retryFailedFiles()
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    Log.d(TAG, "已重置 $count 个失败文件为待同步状态")
                    _uiState.update { state ->
                        state.copy(isLoading = false)
                    }
                    // 刷新统计信息
                    loadSyncStatistics()
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "重试失败: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "重试失败文件失败", e)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "重试失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 清除同步历史
     */
    fun clearSyncHistory() {
        viewModelScope.launch {
            try {
                _uiState.update { state ->
                    state.copy(isLoading = true, errorMessage = null)
                }

                val result = syncRepository.clearSyncHistory()
                if (result.isSuccess) {
                    Log.d(TAG, "同步历史已清除")
                    _uiState.update { state ->
                        state.copy(isLoading = false)
                    }
                    // 刷新统计信息
                    loadSyncStatistics()
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "清除历史失败: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "清除同步历史失败", e)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = "清除历史失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 显示添加目录对话框
     */
    fun showAddDirectoryDialog() {
        _uiState.update { state ->
            state.copy(showAddDirectoryDialog = true)
        }
    }

    /**
     * 隐藏添加目录对话框
     */
    fun hideAddDirectoryDialog() {
        _uiState.update { state ->
            state.copy(showAddDirectoryDialog = false)
        }
    }

    /**
     * 隐藏删除确认对话框
     */
    fun hideDeleteConfirmDialog() {
        _uiState.update { state ->
            state.copy(
                showDeleteConfirmDialog = false,
                directoryToDelete = null
            )
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { state ->
            state.copy(errorMessage = null)
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadSyncConfig()
        loadSyncDirectories()
        loadSyncStatistics()
    }
}
