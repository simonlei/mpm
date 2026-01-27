package com.simon.mpm.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.simon.mpm.MainActivity
import com.simon.mpm.R

/**
 * 通知帮助类
 * 管理照片上传相关的通知
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "photo_upload_channel"
        const val CHANNEL_NAME = "照片上传"
        const val NOTIFICATION_ID = 1001
        
        // 自动同步相关
        const val SYNC_CHANNEL_ID = "photo_sync_channel"
        const val SYNC_CHANNEL_NAME = "照片同步"
        const val SYNC_NOTIFICATION_ID = 1002
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
        createSyncNotificationChannel()
    }

    /**
     * 创建通知渠道（Android 8.0+）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // 低优先级，不发出声音
            ).apply {
                description = "显示照片上传进度"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * 创建上传进度通知
     */
    fun createUploadProgressNotification(
        uploadedCount: Int,
        totalCount: Int
    ): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("正在上传照片")
            .setContentText("已上传 $uploadedCount / $totalCount 张")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setProgress(totalCount, uploadedCount, false)
            .setOngoing(true) // 不可滑动关闭
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * 更新上传进度
     */
    fun updateUploadProgress(uploadedCount: Int, totalCount: Int) {
        val notification = createUploadProgressNotification(uploadedCount, totalCount)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 创建上传完成通知
     */
    fun showUploadCompleteNotification(successCount: Int, failedCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (failedCount == 0) {
            "成功上传 $successCount 张照片"
        } else {
            "成功 $successCount 张，失败 $failedCount 张"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("上传完成")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setAutoCancel(true) // 点击后自动消失
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 创建上传失败通知
     */
    fun showUploadFailedNotification(failedCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("上传失败")
            .setContentText("$failedCount 张照片上传失败")
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * 取消通知
     */
    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    /**
     * 创建同步通知渠道（Android 8.0+）
     */
    private fun createSyncNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SYNC_CHANNEL_ID,
                SYNC_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示照片自动同步进度"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * 创建同步进度通知
     */
    fun createSyncProgressNotification(
        syncedCount: Int,
        totalCount: Int
    ): android.app.Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setContentTitle("正在同步照片")
            .setContentText("已同步 $syncedCount / $totalCount 个文件")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setProgress(totalCount, syncedCount, false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    /**
     * 更新同步进度
     */
    fun updateSyncProgress(syncedCount: Int, totalCount: Int) {
        val notification = createSyncProgressNotification(syncedCount, totalCount)
        notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
    }
    
    /**
     * 显示同步完成通知
     */
    fun showSyncCompleteNotification(successCount: Int, failedCount: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (failedCount == 0) {
            "成功同步 $successCount 个文件"
        } else {
            "成功 $successCount 个，失败 $failedCount 个"
        }

        val notification = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setContentTitle("同步完成")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
    }
    
    /**
     * 显示同步失败通知
     */
    fun showSyncFailedNotification(message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setContentTitle("同步失败")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(SYNC_NOTIFICATION_ID, notification)
    }
    
    /**
     * 取消同步通知
     */
    fun cancelSyncNotification() {
        notificationManager.cancel(SYNC_NOTIFICATION_ID)
    }
}
