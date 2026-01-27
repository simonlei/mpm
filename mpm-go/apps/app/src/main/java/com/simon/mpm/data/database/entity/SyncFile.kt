package com.simon.mpm.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 同步文件实体
 * 记录文件的同步状态和信息
 */
@Entity(tableName = "sync_files")
data class SyncFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 文件信息
    val filePath: String,           // 本地文件路径
    val fileName: String,           // 文件名
    val fileSize: Long,             // 文件大小（字节）
    val modifiedTime: Long,         // 文件修改时间（毫秒时间戳）
    val fileHash: String? = null,   // 文件哈希值（用于检测文件变化）
    
    // 同步状态
    val syncStatus: SyncStatus,     // 同步状态
    val uploadTime: Long? = null,   // 上传时间（毫秒时间戳）
    val failureReason: String? = null, // 失败原因
    val serverPath: String? = null, // 服务器路径
    
    // 元数据
    val createdAt: Long = System.currentTimeMillis(), // 创建时间
    val updatedAt: Long = System.currentTimeMillis()  // 更新时间
)

/**
 * 同步状态枚举
 */
enum class SyncStatus {
    PENDING,    // 待同步
    SYNCING,    // 同步中
    SYNCED,     // 已同步
    FAILED      // 失败
}
