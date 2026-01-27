package com.simon.mpm.data.database.dao

import androidx.room.*
import com.simon.mpm.data.database.entity.SyncFile
import com.simon.mpm.data.database.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * 同步文件DAO
 */
@Dao
interface SyncFileDao {
    
    /**
     * 插入同步文件
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncFile: SyncFile): Long
    
    /**
     * 批量插入同步文件
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(syncFiles: List<SyncFile>)
    
    /**
     * 更新同步文件
     */
    @Update
    suspend fun update(syncFile: SyncFile)
    
    /**
     * 删除同步文件
     */
    @Delete
    suspend fun delete(syncFile: SyncFile)
    
    /**
     * 根据文件路径查询同步文件
     */
    @Query("SELECT * FROM sync_files WHERE filePath = :filePath LIMIT 1")
    suspend fun getByFilePath(filePath: String): SyncFile?
    
    /**
     * 查询所有同步文件
     */
    @Query("SELECT * FROM sync_files ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<SyncFile>>
    
    /**
     * 根据状态查询同步文件
     */
    @Query("SELECT * FROM sync_files WHERE syncStatus = :status ORDER BY createdAt DESC")
    fun getByStatusFlow(status: SyncStatus): Flow<List<SyncFile>>
    
    /**
     * 查询待同步的文件
     */
    @Query("SELECT * FROM sync_files WHERE syncStatus = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingFiles(): List<SyncFile>
    
    /**
     * 查询失败的文件
     */
    @Query("SELECT * FROM sync_files WHERE syncStatus = 'FAILED' ORDER BY createdAt DESC")
    suspend fun getFailedFiles(): List<SyncFile>
    
    /**
     * 统计各状态的文件数量
     */
    @Query("SELECT COUNT(*) FROM sync_files WHERE syncStatus = :status")
    suspend fun countByStatus(status: SyncStatus): Int
    
    /**
     * 清除所有同步记录
     */
    @Query("DELETE FROM sync_files")
    suspend fun clearAll()
    
    /**
     * 清除已同步的记录
     */
    @Query("DELETE FROM sync_files WHERE syncStatus = 'SYNCED'")
    suspend fun clearSynced()
    
    /**
     * 更新文件同步状态
     */
    @Query("UPDATE sync_files SET syncStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SyncStatus, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 更新文件同步失败信息
     */
    @Query("UPDATE sync_files SET syncStatus = 'FAILED', failureReason = :reason, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFailure(id: Long, reason: String, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 更新文件同步成功信息
     */
    @Query("UPDATE sync_files SET syncStatus = 'SYNCED', uploadTime = :uploadTime, serverPath = :serverPath, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSuccess(id: Long, uploadTime: Long, serverPath: String, updatedAt: Long = System.currentTimeMillis())
}
