package com.simon.mpm.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.service.PhotoSyncService
import com.simon.mpm.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * 照片自动同步Worker
 * 使用WorkManager实现定期后台同步任务
 */
@HiltWorker
class PhotoSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "PhotoSyncWorker"
        const val WORK_NAME = "photo_sync_work"
    }

    private val notificationHelper by lazy { NotificationHelper(applicationContext) }

    override suspend fun doWork(): Result {
        Log.d(TAG, "同步Worker开始执行")

        return try {
            // 检查是否启用自动同步
            val autoSyncEnabled = preferencesManager.autoSyncEnabled.first()
            if (!autoSyncEnabled) {
                Log.w(TAG, "自动同步未启用，跳过执行")
                return Result.success()
            }

            // 启动前台服务执行同步
            PhotoSyncService.startSync(applicationContext)
            
            Log.d(TAG, "同步Worker执行成功")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "同步Worker执行失败", e)
            
            // 如果是可重试的错误，返回retry
            if (runAttemptCount < 3) {
                Log.d(TAG, "将重试，当前尝试次数: $runAttemptCount")
                Result.retry()
            } else {
                Log.e(TAG, "已达到最大重试次数，标记为失败")
                notificationHelper.showSyncFailedNotification("自动同步失败: ${e.message}")
                Result.failure()
            }
        }
    }

    /**
     * 提供前台信息（Android 12+需要）
     */
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = notificationHelper.createSyncProgressNotification(0, 1)
        return ForegroundInfo(NotificationHelper.SYNC_NOTIFICATION_ID, notification)
    }
}
