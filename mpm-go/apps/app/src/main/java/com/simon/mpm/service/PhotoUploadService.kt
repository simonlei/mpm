package com.simon.mpm.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.simon.mpm.common.Result
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.data.repository.PhotoRepository
import com.simon.mpm.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 照片上传前台服务
 * 在后台上传照片，显示持久通知
 */
@AndroidEntryPoint
class PhotoUploadService : Service() {

    companion object {
        private const val TAG = "PhotoUploadService"
        const val EXTRA_FILE_URIS = "extra_file_uris"
        const val EXTRA_FILE_NAMES = "extra_file_names"
        const val EXTRA_FILE_SIZES = "extra_file_sizes"
        const val EXTRA_FILE_MODIFIED_TIMES = "extra_file_modified_times"

        /**
         * 启动上传服务
         */
        fun start(
            context: Context,
            fileUris: List<String>,
            fileNames: List<String>,
            fileSizes: List<Long>,
            fileModifiedTimes: List<Long>
        ) {
            val intent = Intent(context, PhotoUploadService::class.java).apply {
                putStringArrayListExtra(EXTRA_FILE_URIS, ArrayList(fileUris))
                putStringArrayListExtra(EXTRA_FILE_NAMES, ArrayList(fileNames))
                putExtra(EXTRA_FILE_SIZES, fileSizes.toLongArray())
                putExtra(EXTRA_FILE_MODIFIED_TIMES, fileModifiedTimes.toLongArray())
            }
            context.startForegroundService(intent)
        }
    }

    @Inject
    lateinit var photoRepository: PhotoRepository

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private lateinit var notificationHelper: NotificationHelper
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        Log.d(TAG, "服务创建")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "服务启动")

        // 必须在5秒内调用startForeground
        val notification = notificationHelper.createUploadProgressNotification(0, 1)
        startForeground(NotificationHelper.NOTIFICATION_ID, notification)

        // 获取上传文件信息
        val fileUris = intent?.getStringArrayListExtra(EXTRA_FILE_URIS) ?: emptyList()
        val fileNames = intent?.getStringArrayListExtra(EXTRA_FILE_NAMES) ?: emptyList()
        val fileSizes = intent?.getLongArrayExtra(EXTRA_FILE_SIZES)?.toList() ?: emptyList()
        val fileModifiedTimes = intent?.getLongArrayExtra(EXTRA_FILE_MODIFIED_TIMES)?.toList() ?: emptyList()

        if (fileUris.isEmpty()) {
            Log.w(TAG, "没有文件需要上传")
            stopSelf()
            return START_NOT_STICKY
        }

        // 开始上传
        serviceScope.launch {
            uploadFiles(fileUris, fileNames, fileSizes, fileModifiedTimes)
        }

        return START_STICKY
    }

    /**
     * 上传文件
     */
    private suspend fun uploadFiles(
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
            try {
                val uri = Uri.parse(uriString)
                val fileName = fileNames.getOrNull(index) ?: "unknown"
                val lastModified = fileModifiedTimes.getOrNull(index) ?: System.currentTimeMillis()

                // 构建目标路径
                val targetPath = buildTargetPath(account, lastModified, fileName)
                Log.d(TAG, "上传文件: $fileName -> $targetPath")

                // 更新进度通知
                notificationHelper.updateUploadProgress(uploadedCount, totalCount)

                // 上传文件
                val result = photoRepository.uploadPhoto(
                    uri = uri,
                    fileName = targetPath,
                    lastModified = lastModified,
                    context = this@PhotoUploadService
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

        Log.d(TAG, "上传完成: 成功=$successCount, 失败=$failedCount")

        // 停止服务
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

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "服务销毁")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
