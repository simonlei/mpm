package com.simon.mpm.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.simon.mpm.common.Constants
import com.simon.mpm.data.database.dao.SyncDirectoryDao
import com.simon.mpm.data.database.dao.SyncFileDao
import com.simon.mpm.data.database.entity.SyncDirectory
import com.simon.mpm.data.database.entity.SyncFile
import com.simon.mpm.data.database.entity.SyncStatus
import com.simon.mpm.data.datastore.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 同步Repository
 * 负责文件扫描、同步状态管理等
 */
@Singleton
class SyncRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val syncFileDao: SyncFileDao,
    private val syncDirectoryDao: SyncDirectoryDao,
    private val preferencesManager: PreferencesManager,
    private val photoRepository: PhotoRepository
) {
    
    companion object {
        private const val TAG = "SyncRepository"
    }
    
    /**
     * 获取所有同步文件
     */
    fun getAllSyncFiles(): Flow<List<SyncFile>> {
        return syncFileDao.getAllFlow()
    }
    
    /**
     * 根据状态获取同步文件
     */
    fun getSyncFilesByStatus(status: SyncStatus): Flow<List<SyncFile>> {
        return syncFileDao.getByStatusFlow(status)
    }
    
    /**
     * 获取所有同步目录
     */
    fun getAllSyncDirectories(): Flow<List<SyncDirectory>> {
        return syncDirectoryDao.getAllFlow()
    }
    
    /**
     * 添加同步目录
     */
    suspend fun addSyncDirectory(directoryPath: String): Result<Long> {
        return try {
            // 检查目录是否已存在
            val existing = syncDirectoryDao.getByPath(directoryPath)
            if (existing != null) {
                return Result.failure(Exception("目录已存在"))
            }
            
            // 插入新目录
            val directory = SyncDirectory(
                directoryPath = directoryPath,
                isEnabled = true
            )
            val id = syncDirectoryDao.insert(directory)
            
            // 更新PreferencesManager中的目录列表
            updateSyncDirectoriesPreference()
            
            Result.success(id)
        } catch (e: Exception) {
            Log.e(TAG, "添加同步目录失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 删除同步目录
     */
    suspend fun removeSyncDirectory(directory: SyncDirectory): Result<Unit> {
        return try {
            syncDirectoryDao.delete(directory)
            
            // 更新PreferencesManager中的目录列表
            updateSyncDirectoriesPreference()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "删除同步目录失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 更新目录启用状态
     */
    suspend fun updateDirectoryEnabled(directoryId: Long, enabled: Boolean): Result<Unit> {
        return try {
            syncDirectoryDao.updateEnabled(directoryId, enabled)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "更新目录状态失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 扫描指定目录下的媒体文件
     */
    suspend fun scanDirectory(directory: SyncDirectory): Result<List<SyncFile>> {
        return try {
            val files = mutableListOf<SyncFile>()
            val fileTypes = preferencesManager.syncFileTypes.first()
            
            // 使用MediaStore扫描媒体文件
            val projection = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_MODIFIED,
                MediaStore.Files.FileColumns.MEDIA_TYPE
            )
            
            val selection = buildString {
                append("${MediaStore.Files.FileColumns.DATA} LIKE ?")
                when (fileTypes) {
                    Constants.SYNC_FILE_TYPE_IMAGES -> {
                        append(" AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}")
                    }
                    Constants.SYNC_FILE_TYPE_VIDEOS -> {
                        append(" AND ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO}")
                    }
                    else -> {
                        append(" AND (${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}")
                        append(" OR ${MediaStore.Files.FileColumns.MEDIA_TYPE} = ${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO})")
                    }
                }
            }
            
            val selectionArgs = arrayOf("${directory.directoryPath}%")
            
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
                
                while (cursor.moveToNext()) {
                    val filePath = cursor.getString(dataColumn)
                    val fileName = cursor.getString(nameColumn)
                    val fileSize = cursor.getLong(sizeColumn)
                    val modifiedTime = cursor.getLong(modifiedColumn) * 1000 // 转换为毫秒
                    
                    // 检查文件是否已存在于数据库
                    val existingFile = syncFileDao.getByFilePath(filePath)
                    if (existingFile == null) {
                        // 新文件，添加到待同步列表
                        val syncFile = SyncFile(
                            filePath = filePath,
                            fileName = fileName,
                            fileSize = fileSize,
                            modifiedTime = modifiedTime,
                            syncStatus = SyncStatus.PENDING
                        )
                        files.add(syncFile)
                    } else if (existingFile.modifiedTime < modifiedTime) {
                        // 文件已修改，更新状态为待同步
                        val updatedFile = existingFile.copy(
                            fileSize = fileSize,
                            modifiedTime = modifiedTime,
                            syncStatus = SyncStatus.PENDING,
                            updatedAt = System.currentTimeMillis()
                        )
                        syncFileDao.update(updatedFile)
                        files.add(updatedFile)
                    }
                }
            }
            
            // 批量插入新文件
            if (files.isNotEmpty()) {
                syncFileDao.insertAll(files)
            }
            
            // 更新目录扫描时间和统计信息
            syncDirectoryDao.updateScanTime(directory.id, System.currentTimeMillis())
            syncDirectoryDao.updateStats(
                directory.id,
                totalFiles = files.size,
                syncedFiles = 0
            )
            
            Log.d(TAG, "扫描目录完成: ${directory.directoryPath}, 发现 ${files.size} 个新文件")
            Result.success(files)
        } catch (e: Exception) {
            Log.e(TAG, "扫描目录失败: ${directory.directoryPath}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 扫描所有启用的目录
     */
    suspend fun scanAllDirectories(): Result<Int> {
        return try {
            val directories = syncDirectoryDao.getEnabled()
            var totalFiles = 0
            
            for (directory in directories) {
                val result = scanDirectory(directory)
                if (result.isSuccess) {
                    totalFiles += result.getOrNull()?.size ?: 0
                }
            }
            
            Log.d(TAG, "扫描所有目录完成，共发现 $totalFiles 个新文件")
            Result.success(totalFiles)
        } catch (e: Exception) {
            Log.e(TAG, "扫描所有目录失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取待同步的文件列表
     */
    suspend fun getPendingFiles(): List<SyncFile> {
        return syncFileDao.getPendingFiles()
    }
    
    /**
     * 获取失败的文件列表
     */
    suspend fun getFailedFiles(): List<SyncFile> {
        return syncFileDao.getFailedFiles()
    }
    
    /**
     * 统计各状态的文件数量
     */
    suspend fun getStatusCounts(): Map<SyncStatus, Int> {
        return mapOf(
            SyncStatus.PENDING to syncFileDao.countByStatus(SyncStatus.PENDING),
            SyncStatus.SYNCING to syncFileDao.countByStatus(SyncStatus.SYNCING),
            SyncStatus.SYNCED to syncFileDao.countByStatus(SyncStatus.SYNCED),
            SyncStatus.FAILED to syncFileDao.countByStatus(SyncStatus.FAILED)
        )
    }
    
    /**
     * 更新文件同步状态
     */
    suspend fun updateFileStatus(fileId: Long, status: SyncStatus) {
        syncFileDao.updateStatus(fileId, status)
    }
    
    /**
     * 更新文件同步失败信息
     */
    suspend fun updateFileFailure(fileId: Long, reason: String) {
        syncFileDao.updateFailure(fileId, reason)
    }
    
    /**
     * 更新文件同步成功信息
     */
    suspend fun updateFileSuccess(fileId: Long, serverPath: String) {
        syncFileDao.updateSuccess(
            fileId,
            uploadTime = System.currentTimeMillis(),
            serverPath = serverPath
        )
    }
    
    /**
     * 清除同步历史
     */
    suspend fun clearSyncHistory(): Result<Unit> {
        return try {
            syncFileDao.clearSynced()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "清除同步历史失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 重试失败的文件
     */
    suspend fun retryFailedFiles(): Result<Int> {
        return try {
            val failedFiles = syncFileDao.getFailedFiles()
            failedFiles.forEach { file ->
                syncFileDao.updateStatus(file.id, SyncStatus.PENDING)
            }
            Result.success(failedFiles.size)
        } catch (e: Exception) {
            Log.e(TAG, "重试失败文件失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 计算文件哈希值（用于检测文件变化）
     */
    private fun calculateFileHash(filePath: String): String? {
        return try {
            val file = File(filePath)
            if (!file.exists()) return null
            
            val digest = MessageDigest.getInstance("MD5")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.e(TAG, "计算文件哈希失败: $filePath", e)
            null
        }
    }
    
    /**
     * 更新PreferencesManager中的同步目录列表
     */
    private suspend fun updateSyncDirectoriesPreference() {
        val directories = syncDirectoryDao.getAll()
        val paths = directories.map { it.directoryPath }
        preferencesManager.setSyncDirectories(paths)
    }
}
