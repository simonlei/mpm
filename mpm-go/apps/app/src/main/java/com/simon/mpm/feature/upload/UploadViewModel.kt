package com.simon.mpm.feature.upload

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * 上传文件状态
 */
data class UploadFile(
    val uri: Uri,
    val fileName: String,
    val size: Long,
    val lastModified: Long,
    val status: UploadStatus = UploadStatus.Waiting
)

/**
 * 上传状态
 */
sealed class UploadStatus {
    object Waiting : UploadStatus()
    object Uploading : UploadStatus()
    object Success : UploadStatus()
    data class Failed(val error: String) : UploadStatus()
}

/**
 * 上传UI状态
 */
data class UploadUiState(
    val files: List<UploadFile> = emptyList(),
    val isUploading: Boolean = false,
    val uploadedCount: Int = 0,
    val batchId: String = "",
    val errorMessage: String? = null
)

/**
 * 上传ViewModel
 */
@HiltViewModel
class UploadViewModel @Inject constructor(
    private val photoRepository: PhotoRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "UploadViewModel"
    }

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    /**
     * 添加文件到上传列表
     */
    fun addFiles(uris: List<Uri>, context: Context) {
        val newFiles = uris.mapNotNull { uri ->
            getFileInfo(uri, context)?.let { (fileName, size, lastModified) ->
                UploadFile(
                    uri = uri,
                    fileName = fileName,
                    size = size,
                    lastModified = lastModified
                )
            }
        }
        
        _uiState.update { state ->
            state.copy(files = state.files + newFiles)
        }
    }

    /**
     * 清空文件列表
     */
    fun clearFiles() {
        _uiState.update { state ->
            state.copy(
                files = emptyList(),
                uploadedCount = 0,
                errorMessage = null
            )
        }
    }

    /**
     * 开始上传
     */
    fun startUpload(context: Context) {
        if (_uiState.value.isUploading || _uiState.value.files.isEmpty()) {
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isUploading = true,
                    uploadedCount = 0,
                    batchId = System.currentTimeMillis().toString(),
                    errorMessage = null
                )
            }

            val account: String = preferencesManager.account.first() ?: "unknown"
            
            _uiState.value.files.forEachIndexed { index, file ->
                // 更新当前文件状态为上传中
                updateFileStatus(index, UploadStatus.Uploading)
                
                // 构建文件路径：用户名/年份/月份/原文件名
                val targetPath = buildTargetPath(account, file.lastModified, file.fileName)
                
                // 上传文件
                val result = photoRepository.uploadPhoto(
                    uri = file.uri,
                    fileName = targetPath,
                    lastModified = file.lastModified,
                    batchId = _uiState.value.batchId,
                    context = context
                )
                
                // 更新文件状态
                when (result) {
                    is Result.Success -> {
                        updateFileStatus(index, UploadStatus.Success)
                        _uiState.update { state ->
                            state.copy(uploadedCount = state.uploadedCount + 1)
                        }
                    }
                    is Result.Error -> {
                        updateFileStatus(index, UploadStatus.Failed(result.message ?: "上传失败"))
                        Log.e(TAG, "上传失败: ${file.fileName}", result.exception)
                    }
                    else -> {}
                }
            }

            _uiState.update { state ->
                state.copy(isUploading = false)
            }
        }
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

    /**
     * 更新文件状态
     */
    private fun updateFileStatus(index: Int, status: UploadStatus) {
        _uiState.update { state ->
            val updatedFiles = state.files.toMutableList()
            if (index in updatedFiles.indices) {
                updatedFiles[index] = updatedFiles[index].copy(status = status)
            }
            state.copy(files = updatedFiles)
        }
    }

    /**
     * 获取文件信息
     */
    private fun getFileInfo(uri: Uri, context: Context): Triple<String, Long, Long>? {
        return try {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE,
                    MediaStore.MediaColumns.DATE_MODIFIED
                ),
                null,
                null,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                    val dateIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                    
                    val fileName = it.getString(nameIndex)
                    val size = it.getLong(sizeIndex)
                    val lastModified = it.getLong(dateIndex) * 1000 // 转换为毫秒
                    
                    Triple(fileName, size, lastModified)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取文件信息失败", e)
            null
        }
    }
}
