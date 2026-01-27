package com.simon.mpm.util

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.simon.mpm.worker.PhotoSyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 同步任务管理器
 * 负责调度和管理WorkManager同步任务
 */
@Singleton
class SyncWorkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SyncWorkManager"
    }

    private val workManager = WorkManager.getInstance(context)

    /**
     * 调度定期同步任务
     * @param intervalHours 同步间隔（小时）
     * @param requireWifi 是否仅WiFi同步
     * @param requireCharging 是否仅充电时同步
     * @param requireBatteryNotLow 是否要求电量充足
     */
    fun scheduleSyncWork(
        intervalHours: Long = 24,
        requireWifi: Boolean = false,
        requireCharging: Boolean = false,
        requireBatteryNotLow: Boolean = true
    ) {
        Log.d(TAG, "调度同步任务: 间隔=${intervalHours}小时, WiFi=$requireWifi, 充电=$requireCharging")

        // 构建约束条件
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (requireWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .setRequiresCharging(requireCharging)
            .setRequiresBatteryNotLow(requireBatteryNotLow)
            .build()

        // 创建定期任务（最小间隔15分钟）
        val syncWorkRequest = PeriodicWorkRequestBuilder<PhotoSyncWorker>(
            repeatInterval = maxOf(intervalHours, 1),
            repeatIntervalTimeUnit = TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(PhotoSyncWorker.WORK_NAME)
            .build()

        // 使用REPLACE策略，确保只有一个同步任务在运行
        workManager.enqueueUniquePeriodicWork(
            PhotoSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncWorkRequest
        )

        Log.d(TAG, "同步任务已调度")
    }

    /**
     * 取消同步任务
     */
    fun cancelSyncWork() {
        Log.d(TAG, "取消同步任务")
        workManager.cancelUniqueWork(PhotoSyncWorker.WORK_NAME)
    }

    /**
     * 检查同步任务是否正在运行
     */
    fun isSyncWorkScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(PhotoSyncWorker.WORK_NAME).get()
        return workInfos.any { !it.state.isFinished }
    }
}
