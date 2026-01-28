package com.simon.mpm.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.simon.mpm.common.Constants
import com.simon.mpm.common.Result
import com.simon.mpm.data.database.entity.SyncStatus
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.data.repository.PhotoRepository
import com.simon.mpm.data.repository.SyncRepository
import com.simon.mpm.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 照片自动同步前台服务
 * 扫描指定目录并自动上传新增和修改的照片
 */
@AndroidEntryPoint
class PhotoSyncService : Service() {

    companion object {
        private const val TAG = "PhotoSyncService"
        const val ACTION_START_SYNC = "action_start_sync"
        const val ACTION_STOP_SYNC = "action_stop_sync"
        const val ACTION_SYNC_COMPLETED = "com.simon.mpm.SYNC_COMPLETED"
        const val EXTRA_SUCCESS_COUNT = "extra_success_count"
        const val EXTRA_FAILED_COUNT = "extra_failed_count"

        /**
         * 启动同步服务
         */
        fun startSync(context: Context) {
            val intent = Intent(context, PhotoSyncService::class.java).apply {
                action = ACTION_START_SYNC
            }
            context.startForegroundService(intent)
        }

        /**
         * 停止同步服务
         */
        fun stopSync(context: Context) {
            val intent = Intent(context, PhotoSyncService::class.java).apply {
                action = ACTION_STOP_SYNC
            }
            context.startService(intent)
        }
    }

    @Inject
    lateinit var syncRepository: SyncRepository

    @Inject
    lateinit var photoRepository: PhotoRepository

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private lateinit var notificationHelper: NotificationHelper
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isSyncing = false

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        Log.d(TAG, "同步服务创建")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "同步服务启动: action=${intent?.action}")

        when (intent?.action) {
            ACTION_START_SYNC -> {
                if (!isSyncing) {
                    startSync()
                }
            }
            ACTION_STOP_SYNC -> {
                stopSync()
            }
        }

        return START_STICKY
    }

    /**
     * 开始同步
     */
    private fun startSync() {
        isSyncing = true

        // 必须在5秒内调用startForeground
        val notification = notificationHelper.createSyncProgressNotification(0, 1)
        startForeground(NotificationHelper.SYNC_NOTIFICATION_ID, notification)

        serviceScope.launch {
            try {
                // 检查是否启用自动同步
                val autoSyncEnabled = preferencesManager.autoSyncEnabled.first()
                if (!autoSyncEnabled) {
                    Log.w(TAG, "自动同步未启用")
                    notificationHelper.showSyncFailedNotification("自动同步未启用")
                    stopSelf()
                    return@launch
                }

                // 检查网络状态
                val syncWifiOnly = preferencesManager.syncWifiOnly.first()
                if (syncWifiOnly && !isWifiConnected()) {
                    Log.w(TAG, "仅WiFi同步，但当前未连接WiFi")
                    notificationHelper.showSyncFailedNotification("请连接WiFi后再同步")
                    stopSelf()
                    return@launch
                }

                // 扫描所有启用的目录
                Log.d(TAG, "开始扫描目录...")
                val scanResult = syncRepository.scanAllDirectories()
                if (scanResult.isFailure) {
                    Log.e(TAG, "扫描目录失败", scanResult.exceptionOrNull())
                    notificationHelper.showSyncFailedNotification("扫描目录失败")
                    stopSelf()
                    return@launch
                }

                val newFilesCount = scanResult.getOrNull() ?: 0
                Log.d(TAG, "扫描完成，发现 $newFilesCount 个新文件")

                if (newFilesCount == 0) {
                    Log.d(TAG, "没有新文件需要同步")
                    notificationHelper.showSyncCompleteNotification(0, 0)
                    sendSyncCompletedBroadcast(0, 0)
                    // 延迟停止服务，确保广播被接收
                    delay(500)
                    stopSelf()
                    return@launch
                }

                // 获取待同步的文件列表
                val pendingFiles = syncRepository.getPendingFiles()
                Log.d(TAG, "待同步文件数: ${pendingFiles.size}")

                // 开始上传
                uploadFiles(pendingFiles)

            } catch (e: Exception) {
                Log.e(TAG, "同步异常", e)
                notificationHelper.showSyncFailedNotification("同步异常: ${e.message}")
                stopSelf()
            }
        }
    }

    /**
     * 停止同步
     */
    private fun stopSync() {
        isSyncing = false
        notificationHelper.cancelSyncNotification()
        stopSelf()
        Log.d(TAG, "同步已停止")
    }

    /**
     * 上传文件
     */
    private suspend fun uploadFiles(files: List<com.simon.mpm.data.database.entity.SyncFile>) {
        val totalCount = files.size
        var syncedCount = 0
        var successCount = 0
        var failedCount = 0

        val account = preferencesManager.account.first() ?: "unknown"

        for (file in files) {
            if (!isSyncing) {
                Log.d(TAG, "同步已取消")
                break
            }

            try {
                // 检查文件是否存在
                val fileObj = File(file.filePath)
                if (!fileObj.exists()) {
                    Log.w(TAG, "文件不存在: ${file.filePath}")
                    syncRepository.updateFileFailure(file.id, "文件不存在")
                    failedCount++
                    syncedCount++
                    continue
                }

                // 更新状态为同步中
                syncRepository.updateFileStatus(file.id, SyncStatus.SYNCING)

                // 构建目标路径
                val targetPath = buildTargetPath(account, file.modifiedTime, file.fileName)
                Log.d(TAG, "同步文件: ${file.fileName} -> $targetPath")

                // 更新进度通知
                notificationHelper.updateSyncProgress(syncedCount, totalCount)

                // 上传文件
                val uri = Uri.fromFile(fileObj)
                val result = photoRepository.uploadPhoto(
                    uri = uri,
                    fileName = targetPath,
                    lastModified = file.modifiedTime,
                    context = this@PhotoSyncService
                )

                when (result) {
                    is Result.Success -> {
                        // 更新为已同步
                        syncRepository.updateFileSuccess(file.id, targetPath)
                        successCount++
                        Log.d(TAG, "同步成功: ${file.fileName}")
                    }
                    is Result.Error -> {
                        // 更新为失败
                        syncRepository.updateFileFailure(file.id, result.message ?: "未知错误")
                        failedCount++
                        Log.e(TAG, "同步失败: ${file.fileName} - ${result.message}")
                    }
                    else -> {}
                }

                syncedCount++

            } catch (e: Exception) {
                syncRepository.updateFileFailure(file.id, e.message ?: "未知异常")
                failedCount++
                Log.e(TAG, "同步异常: ${file.fileName}", e)
            }
        }

        // 更新最后同步时间
        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())
        preferencesManager.setLastSyncTime(currentTime)

        // 显示完成通知
        if (successCount > 0 || failedCount > 0) {
            notificationHelper.showSyncCompleteNotification(successCount, failedCount)
        }

        Log.d(TAG, "同步完成: 成功=$successCount, 失败=$failedCount")

        // 发送同步完成广播
        sendSyncCompletedBroadcast(successCount, failedCount)

        // 延迟停止服务，确保广播被接收
        delay(500)
        stopSelf()
    }

    /**
     * 构建目标路径：用户名/年份/月份/原文件名
     */
    private fun buildTargetPath(account: String, lastModified: Long, fileName: String): String {
        val date = Date(lastModified)
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MM", Locale.getDefault())

        val year = yearFormat.format(date)
        val month = monthFormat.format(date)

        return "$account/$year/$month/$fileName"
    }

    /**
     * 发送同步完成广播
     */
    private fun sendSyncCompletedBroadcast(successCount: Int, failedCount: Int) {
        val intent = Intent(ACTION_SYNC_COMPLETED).apply {
            putExtra(EXTRA_SUCCESS_COUNT, successCount)
            putExtra(EXTRA_FAILED_COUNT, failedCount)
        }
        // 使用LocalBroadcastManager发送应用内广播
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d(TAG, "已发送同步完成广播(LocalBroadcast): success=$successCount, failed=$failedCount")
    }

    /**
     * 检查是否连接WiFi
     */
    private fun isWifiConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    override fun onDestroy() {
        super.onDestroy()
        isSyncing = false
        serviceScope.cancel()
        Log.d(TAG, "同步服务销毁")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
