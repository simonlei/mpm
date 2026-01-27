package com.simon.mpm.data.database.dao

import androidx.room.*
import com.simon.mpm.data.database.entity.SyncDirectory
import kotlinx.coroutines.flow.Flow

/**
 * 同步目录DAO
 */
@Dao
interface SyncDirectoryDao {
    
    /**
     * 插入同步目录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncDirectory: SyncDirectory): Long
    
    /**
     * 批量插入同步目录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(syncDirectories: List<SyncDirectory>)
    
    /**
     * 更新同步目录
     */
    @Update
    suspend fun update(syncDirectory: SyncDirectory)
    
    /**
     * 删除同步目录
     */
    @Delete
    suspend fun delete(syncDirectory: SyncDirectory)
    
    /**
     * 根据路径查询同步目录
     */
    @Query("SELECT * FROM sync_directories WHERE directoryPath = :path LIMIT 1")
    suspend fun getByPath(path: String): SyncDirectory?
    
    /**
     * 查询所有同步目录
     */
    @Query("SELECT * FROM sync_directories ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<SyncDirectory>>
    
    /**
     * 查询所有同步目录（非Flow）
     */
    @Query("SELECT * FROM sync_directories ORDER BY createdAt DESC")
    suspend fun getAll(): List<SyncDirectory>
    
    /**
     * 查询启用的同步目录
     */
    @Query("SELECT * FROM sync_directories WHERE isEnabled = 1 ORDER BY createdAt DESC")
    suspend fun getEnabled(): List<SyncDirectory>
    
    /**
     * 更新目录启用状态
     */
    @Query("UPDATE sync_directories SET isEnabled = :enabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateEnabled(id: Long, enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 更新目录扫描时间
     */
    @Query("UPDATE sync_directories SET lastScanTime = :scanTime, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateScanTime(id: Long, scanTime: Long, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 更新目录统计信息
     */
    @Query("UPDATE sync_directories SET totalFiles = :totalFiles, syncedFiles = :syncedFiles, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStats(id: Long, totalFiles: Int, syncedFiles: Int, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 清除所有同步目录
     */
    @Query("DELETE FROM sync_directories")
    suspend fun clearAll()
}
