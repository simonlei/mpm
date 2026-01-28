package com.simon.mpm.service

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.util.SyncWorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 媒体内容观察者
 * 监听MediaStore的变化，当检测到新增照片或视频时自动触发同步
 */
@Singleton
class MediaContentObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager,
    private val syncWorkManager: SyncWorkManager
) : ContentObserver(Handler(Looper.getMainLooper())) {

    companion object {
        private const val TAG = "MediaContentObserver"
        private const val DEBOUNCE_DELAY = 3000L // 3秒防抖延迟
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastChangeTime = 0L
    private var isRegistered = false

    /**
     * 注册观察者
     */
    fun register() {
        if (isRegistered) {
            Log.d(TAG, "Observer already registered")
            return
        }

        try {
            // 监听图片变化
            context.contentResolver.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                this
            )

            // 监听视频变化
            context.contentResolver.registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                true,
                this
            )

            isRegistered = true
            Log.d(TAG, "MediaContentObserver registered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register observer", e)
        }
    }

    /**
     * 取消注册观察者
     */
    fun unregister() {
        if (!isRegistered) {
            return
        }

        try {
            context.contentResolver.unregisterContentObserver(this)
            isRegistered = false
            Log.d(TAG, "MediaContentObserver unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister observer", e)
        }
    }

    /**
     * 当内容变化时调用
     */
    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        
        Log.d(TAG, "Media content changed: $uri")

        // 防抖处理：避免短时间内多次触发
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastChangeTime < DEBOUNCE_DELAY) {
            Log.d(TAG, "Debouncing change event")
            return
        }
        lastChangeTime = currentTime

        // 检查是否启用自动同步
        scope.launch {
            try {
                val autoSyncEnabled = preferencesManager.autoSyncEnabled.first()
                if (!autoSyncEnabled) {
                    Log.d(TAG, "Auto sync is disabled, skipping")
                    return@launch
                }

                // 延迟一段时间，确保文件写入完成
                delay(DEBOUNCE_DELAY)

                // 触发同步任务
                Log.d(TAG, "Triggering sync due to media change")
                triggerSync()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling media change", e)
            }
        }
    }

    /**
     * 触发同步任务
     */
    private fun triggerSync() {
        scope.launch {
            try {
                // 获取同步配置
                val syncInterval = preferencesManager.syncInterval.first().toIntOrNull() ?: 24
                val syncWifiOnly = preferencesManager.syncWifiOnly.first()

                // 调度同步任务（使用较短的间隔，因为是由变化触发的）
                syncWorkManager.scheduleSyncWork(
                    intervalHours = syncInterval.toLong(),
                    requireWifi = syncWifiOnly,
                    requireCharging = false,
                    requireBatteryNotLow = true
                )

                Log.d(TAG, "Sync work scheduled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule sync work", e)
            }
        }
    }

    /**
     * 检查观察者是否已注册
     */
    fun isRegistered(): Boolean = isRegistered
}