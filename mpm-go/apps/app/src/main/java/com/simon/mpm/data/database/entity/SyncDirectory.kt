package com.simon.mpm.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 同步目录实体
 * 记录需要同步的目录配置
 */
@Entity(tableName = "sync_directories")
data class SyncDirectory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 目录信息
    val directoryPath: String,      // 目录路径
    val isEnabled: Boolean = true,  // 是否启用
    val lastScanTime: Long? = null, // 最后扫描时间（毫秒时间戳）
    
    // 统计信息
    val totalFiles: Int = 0,        // 目录下的文件总数
    val syncedFiles: Int = 0,       // 已同步的文件数
    
    // 元数据
    val createdAt: Long = System.currentTimeMillis(), // 创建时间
    val updatedAt: Long = System.currentTimeMillis()  // 更新时间
)
