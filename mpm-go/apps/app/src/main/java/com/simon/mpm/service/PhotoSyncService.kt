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
        const val ACTION_MANUAL_UPLOAD = "action_manual_upload"
        const val ACTION_SYNC_COMPLETED = "com.simon.mpm.SYNC_COMPLETED"
        const val EXTRA_SUCCESS_COUNT = "extra_success_count"
        const val EXTRA_FAILED_COUNT = "extra_failed_count"
        const val EXTRA_FILE_URIS = "extra_file_uris"
        const val EXTRA_FILE_NAMES = "extra_file_names"
        const val EXTRA_FILE_SIZES = "extra_file_sizes"
        const val EXTRA_FILE_MODIFIED_TIMES = "extra_file_modified_times"

        /**
         * 根据URI和文件名获取Content-Type
         * 优先读取文件头判断真实MIME类型（可以正确识别动态照片等特殊情况）
         * 如果读取失败，则根据文件扩展名判断
         */
        private fun getContentType(context: Context, uri: Uri, fileName: String): String {
            // 优先尝试读取文件头来判断真实的MIME类型
            val mimeTypeFromHeader = try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    // 读取文件头（前12个字节足够判断大部分格式）
                    val header = ByteArray(12)
                    val bytesRead = inputStream.read(header)
                    if (bytesRead > 0) {
                        detectMimeTypeFromHeader(header, bytesRead)
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "无法读取文件头: $uri", e)
                null
            }
            
            if (!mimeTypeFromHeader.isNullOrBlank()) {
                Log.d(TAG, "从文件头检测到MIME类型: $mimeTypeFromHeader")
                return mimeTypeFromHeader
            }
            
            // 回退到基于文件扩展名的判断
            Log.d(TAG, "使用文件扩展名判断MIME类型")
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return when (extension) {
                // 图片格式
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                "bmp" -> "image/bmp"
                "heic", "heif" -> "image/heic"
                // 视频格式
                "mp4" -> "video/mp4"
                "mov" -> "video/quicktime"
                "avi" -> "video/x-msvideo"
                "mkv" -> "video/x-matroska"
                "webm" -> "video/webm"
                "3gp" -> "video/3gpp"
                "m4v" -> "video/x-m4v"
                "wmv" -> "video/x-ms-wmv"
                "flv" -> "video/x-flv"
                // 默认
                else -> "application/octet-stream"
            }
        }

        /**
         * 根据文件头字节判断MIME类型
         */
        private fun detectMimeTypeFromHeader(header: ByteArray, size: Int): String? {
            if (size < 4) return null
            
            // JPEG: FF D8 FF
            if (header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() && header[2] == 0xFF.toByte()) {
                return "image/jpeg"
            }
            
            // PNG: 89 50 4E 47
            if (header[0] == 0x89.toByte() && header[1] == 0x50.toByte() && 
                header[2] == 0x4E.toByte() && header[3] == 0x47.toByte()) {
                return "image/png"
            }
            
            // GIF: 47 49 46 38
            if (header[0] == 0x47.toByte() && header[1] == 0x49.toByte() && 
                header[2] == 0x46.toByte() && header[3] == 0x38.toByte()) {
                return "image/gif"
            }
            
            // WebP: 52 49 46 46 ... 57 45 42 50
            if (size >= 12 && header[0] == 0x52.toByte() && header[1] == 0x49.toByte() && 
                header[2] == 0x46.toByte() && header[3] == 0x46.toByte() &&
                header[8] == 0x57.toByte() && header[9] == 0x45.toByte() && 
                header[10] == 0x42.toByte() && header[11] == 0x50.toByte()) {
                return "image/webp"
            }
            
            // MP4/MOV: 检查 ftyp box (偏移4字节处)
            if (size >= 12) {
                val ftyp = String(header, 4, 4, Charsets.ISO_8859_1)
                when {
                    ftyp.startsWith("ftyp") -> {
                        // 进一步检查品牌
                        val brand = if (size >= 12) String(header, 8, 4, Charsets.ISO_8859_1) else ""
                        return when {
                            brand.startsWith("mp4") || brand.startsWith("isom") || 
                            brand.startsWith("M4V") || brand.startsWith("M4A") -> "video/mp4"
                            brand.startsWith("qt") -> "video/quicktime"
                            else -> "video/mp4" // 默认为MP4
                        }
                    }
                }
            }
            
            // AVI: 52 49 46 46 ... 41 56 49 20
            if (size >= 12 && header[0] == 0x52.toByte() && header[1] == 0x49.toByte() && 
                header[2] == 0x46.toByte() && header[3] == 0x46.toByte() &&
                header[8] == 0x41.toByte() && header[9] == 0x56.toByte() && 
                header[10] == 0x49.toByte() && header[11] == 0x20.toByte()) {
                return "video/x-msvideo"
            }
            
            // HEIC/HEIF: ftyp heic/heix/hevc/hevx/mif1
            if (size >= 12) {
                val ftyp = String(header, 4, 4, Charsets.ISO_8859_1)
                if (ftyp.startsWith("ftyp")) {
                    val brand = String(header, 8, 4, Charsets.ISO_8859_1)
                    if (brand.startsWith("heic") || brand.startsWith("heix") || 
                        brand.startsWith("hevc") || brand.startsWith("hevx") || 
                        brand.startsWith("mif1")) {
                        return "image/heic"
                    }
                }
            }
            
            return null
        }

        /**
         * 启动自动同步服务
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

        /**
         * 启动手动上传服务
         */
        fun startManualUpload(
            context: Context,
            fileUris: List<String>,
            fileNames: List<String>,
            fileSizes: List<Long>,
            fileModifiedTimes: List<Long>
        ) {
            val intent = Intent(context, PhotoSyncService::class.java).apply {
                action = ACTION_MANUAL_UPLOAD
                putStringArrayListExtra(EXTRA_FILE_URIS, ArrayList(fileUris))
                putStringArrayListExtra(EXTRA_FILE_NAMES, ArrayList(fileNames))
                putExtra(EXTRA_FILE_SIZES, fileSizes.toLongArray())
                putExtra(EXTRA_FILE_MODIFIED_TIMES, fileModifiedTimes.toLongArray())
            }
            context.startForegroundService(intent)
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
            ACTION_MANUAL_UPLOAD -> {
                if (!isSyncing) {
                    startManualUpload(intent)
                }
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
     * 开始手动上传
     */
    private fun startManualUpload(intent: Intent) {
        isSyncing = true

        // 必须在5秒内调用startForeground
        val notification = notificationHelper.createUploadProgressNotification(0, 1)
        startForeground(NotificationHelper.NOTIFICATION_ID, notification)

        // 获取上传文件信息
        val fileUris = intent.getStringArrayListExtra(EXTRA_FILE_URIS) ?: emptyList()
        val fileNames = intent.getStringArrayListExtra(EXTRA_FILE_NAMES) ?: emptyList()
        val fileSizes = intent.getLongArrayExtra(EXTRA_FILE_SIZES)?.toList() ?: emptyList()
        val fileModifiedTimes = intent.getLongArrayExtra(EXTRA_FILE_MODIFIED_TIMES)?.toList() ?: emptyList()

        if (fileUris.isEmpty()) {
            Log.w(TAG, "没有文件需要上传")
            stopSelf()
            return
        }

        Log.d(TAG, "开始手动上传 ${fileUris.size} 个文件")

        serviceScope.launch {
            try {
                uploadManualFiles(fileUris, fileNames, fileSizes, fileModifiedTimes)
            } catch (e: Exception) {
                Log.e(TAG, "手动上传异常", e)
                notificationHelper.showUploadFailedNotification(fileUris.size)
                stopSelf()
            }
        }
    }

    /**
     * 上传手动选择的文件
     */
    private suspend fun uploadManualFiles(
        fileUris: List<String>,
        fileNames: List<String>,
        fileSizes: List<Long>,
        fileModifiedTimes: List<Long>
    ) {
        val totalCount = fileUris.size
        var uploadedCount = 0
        var successCount = 0
        var failedCount = 0

        val account = preferencesManager.account.first() ?: "unknown"

        fileUris.forEachIndexed { index, uriString ->
            if (!isSyncing) {
                Log.d(TAG, "上传已取消")
                return@forEachIndexed
            }

            try {
                val uri = Uri.parse(uriString)
                val fileName = fileNames.getOrNull(index) ?: "unknown"
                val lastModified = fileModifiedTimes.getOrNull(index) ?: System.currentTimeMillis()

                // 构建目标路径
                val targetPath = buildTargetPath(account, lastModified, fileName)
                Log.d(TAG, "上传文件: $fileName -> $targetPath")

                // 更新进度通知
                notificationHelper.updateUploadProgress(uploadedCount, totalCount)

                // 获取文件类型（优先从URI获取真实MIME类型）
                val contentType = getContentType(this@PhotoSyncService, uri, fileName)
                Log.d(TAG, "文件类型: $contentType")

                // 上传文件
                val result = photoRepository.uploadPhoto(
                    uri = uri,
                    fileName = targetPath,
                    lastModified = lastModified,
                    context = this@PhotoSyncService,
                    contentType = contentType
                )

                when (result) {
                    is Result.Success -> {
                        successCount++
                        Log.d(TAG, "上传成功: $fileName")
                    }
                    is Result.Error -> {
                        failedCount++
                        Log.e(TAG, "上传失败: $fileName - ${result.message}", result.exception)
                    }
                    else -> {}
                }

                uploadedCount++

            } catch (e: Exception) {
                failedCount++
                Log.e(TAG, "上传异常", e)
            }
        }

        // 显示完成通知
        if (successCount > 0 || failedCount > 0) {
            notificationHelper.showUploadCompleteNotification(successCount, failedCount)
        }

        Log.d(TAG, "手动上传完成: 成功=$successCount, 失败=$failedCount")

        // 停止服务
        stopSelf()
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
                // 解析URI（支持content URI和文件路径）
                val uri = if (file.filePath.startsWith("content://")) {
                    Uri.parse(file.filePath)
                } else {
                    Uri.fromFile(File(file.filePath))
                }
                
                // 检查文件是否存在
                val fileExists = try {
                    contentResolver.openInputStream(uri)?.use { true } ?: false
                } catch (e: Exception) {
                    Log.w(TAG, "无法打开文件: ${file.filePath}", e)
                    false
                }
                
                if (!fileExists) {
                    Log.w(TAG, "文件不存在或无法访问: ${file.filePath}")
                    syncRepository.updateFileFailure(file.id, "文件不存在或无法访问")
                    failedCount++
                    syncedCount++
                    continue
                }

                // 更新状态为同步中
                syncRepository.updateFileStatus(file.id, SyncStatus.SYNCING)

                // 重新获取最佳日期时间（优先EXIF拍摄日期）
                // 这样可以确保即使数据库中的旧记录没有更新，也能使用正确的EXIF日期
                val bestDateTime = com.simon.mpm.util.FileMetadataHelper.getBestDateTimeFromPath(
                    context = this@PhotoSyncService,
                    filePath = file.filePath,
                    fallbackModifiedTime = file.modifiedTime
                )
                
                // 构建目标路径
                val targetPath = buildTargetPath(account, bestDateTime, file.fileName)
                Log.d(TAG, "同步文件: ${file.fileName} -> $targetPath")
                Log.d(TAG, "  使用日期: $bestDateTime (数据库中的日期: ${file.modifiedTime})")

                // 更新进度通知
                notificationHelper.updateSyncProgress(syncedCount, totalCount)

                // 获取文件类型（优先从URI获取真实MIME类型）
                val contentType = getContentType(this@PhotoSyncService, uri, file.fileName)
                Log.d(TAG, "文件类型: $contentType")

                // 上传文件
                val result = photoRepository.uploadPhoto(
                    uri = uri,
                    fileName = targetPath,
                    lastModified = file.modifiedTime,
                    context = this@PhotoSyncService,
                    contentType = contentType
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
