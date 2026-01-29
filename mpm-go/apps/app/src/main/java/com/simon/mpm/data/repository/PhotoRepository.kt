package com.simon.mpm.data.repository

import android.util.Log
import com.simon.mpm.common.Constants
import com.simon.mpm.common.Result
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.network.api.MpmApiService
import com.simon.mpm.network.model.*
import com.simon.mpm.network.util.NetworkErrorHandler
import com.simon.mpm.network.util.safeApiCall
import com.simon.mpm.network.util.safeApiCallUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 照片数据仓库
 * 负责照片相关的数据操作
 */
@Singleton
class PhotoRepository @Inject constructor(
    private val apiService: MpmApiService,
    private val preferencesManager: PreferencesManager
) : BaseRepository() {

    companion object {
        private const val TAG = "PhotoRepository"
    }

    /**
     * 获取完整的图片URL
     */
    private suspend fun getFullImageUrl(relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) return null
        val serverUrl = preferencesManager.serverUrl.first()
        return "$serverUrl${Constants.COS_PATH}$relativePath"
    }

    /**
     * 获取照片列表
     */
    fun getPhotos(
        star: Boolean = false,
        video: Boolean = false,
        trashed: Boolean = false,
        start: Int = 0,
        size: Int = 75,
        dateKey: String = "",
        path: String = "",
        tag: String = "",
        faceId: Int = 0,
        order: String = "id"
    ): Flow<Result<PicsResponse>> = flow {
        emit(Result.Loading)
        
        Log.d(TAG, "getPhotos: star=$star, video=$video, trashed=$trashed, dateKey=$dateKey, tag=$tag, path=$path, order=$order")
        
        val result = safeApiCall {
            apiService.getPics(
                GetPicsRequest(
                    star = if (star) true else null,  // 只有为true时才传递，false时传null表示不筛选
                    video = if (video) true else null,  // 只有为true时才传递，false时传null表示不筛选
                    trashed = trashed,
                    idOnly = false,
                    start = start,
                    size = size,
                    dateKey = dateKey.ifEmpty { null },
                    path = path.ifEmpty { null },
                    tag = tag.ifEmpty { null },
                    faceId = if (faceId > 0) faceId else null,
                    order = order,
                    idRank = null
                )
            )
        }
        
        // 为照片的thumb字段拼接完整URL
        val processedResult = when (result) {
            is Result.Success -> {
                val processedData = result.data.copy(
                    data = result.data.data?.map { photo ->
                        photo.copy(thumb = getFullImageUrl(photo.thumb))
                    } ?: emptyList()
                )
                Result.Success(processedData)
            }
            else -> result
        }
        
        emit(processedResult)
    }

    /**
     * 获取照片数量
     */
    fun getPhotoCount(trashed: Boolean = false): Flow<Result<Int>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.getCount(GetCountRequest(trashed = trashed))
        }
        
        emit(result)
    }

    /**
     * 根据ID获取照片详情
     */
    fun getPhotoById(id: Int): Flow<Result<Photo>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.getPhotoById(GetPhotoByIdRequest(id))
        }
        
        // 为照片的thumb字段拼接完整URL
        val processedResult = when (result) {
            is Result.Success -> {
                val processedPhoto = result.data.copy(
                    thumb = getFullImageUrl(result.data.thumb)
                )
                Result.Success(processedPhoto)
            }
            else -> result
        }
        
        emit(processedResult)
    }

    /**
     * 更新照片信息
     */
    fun updatePhoto(
        id: Int,
        latitude: Double? = null,
        longitude: Double? = null,
        takenDate: String? = null,
        star: Boolean? = null,
        activity: Int? = null,
        tags: String? = null,
        rotate: Int? = null
    ): Flow<Result<Photo>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.updateImage(
                UpdateImageRequest(
                    id = id,
                    latitude = latitude,
                    longitude = longitude,
                    takenDate = takenDate,
                    star = star,
                    activity = activity,
                    tags = tags,
                    rotate = rotate
                )
            )
        }
        
        // 为照片的thumb字段拼接完整URL
        val processedResult = when (result) {
            is Result.Success -> {
                Result.Success(result.data.copy(thumb = getFullImageUrl(result.data.thumb)))
            }
            else -> result
        }
        
        emit(processedResult)
    }

    /**
     * 切换照片收藏状态
     */
    suspend fun toggleStar(id: Int, currentStar: Boolean): Result<Photo> {
        val result = safeApiCall {
            apiService.updateImage(
                UpdateImageRequest(
                    id = id,
                    star = !currentStar
                )
            )
        }
        
        // 为照片的thumb字段拼接完整URL
        return when (result) {
            is Result.Success -> {
                Result.Success(result.data.copy(thumb = getFullImageUrl(result.data.thumb)))
            }
            else -> result
        }
    }

    /**
     * 移动照片到回收站
     */
    fun trashPhotos(ids: List<Int>): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.trashPhotos(ids)
        }
        
        // 将 Result<Int> 转换为 Result<Unit>
        emit(when (result) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        })
    }

    /**
     * 从回收站恢复照片
     */
    fun restorePhotos(ids: List<Int>): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.restorePhotos(ids)
        }
        
        // 将 Result<Int> 转换为 Result<Unit>
        emit(when (result) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        })
    }

    /**
     * 永久删除照片
     */
    fun deletePhotos(ids: List<Int>): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.deletePhotos(ids)
        }
        
        // 将 Result<Int> 转换为 Result<Unit>
        emit(when (result) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(result.exception, result.message)
            is Result.Loading -> Result.Loading
        })
    }

    /**
     * 获取照片数量
     */
    fun getPhotosCount(trashed: Boolean = false): Flow<Result<Int>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.getCount(GetCountRequest(trashed = trashed))
        }
        
        emit(result)
    }

    /**
     * 清空回收站
     */
    fun emptyTrash(): Flow<Result<TaskResponse>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.emptyTrash()
        }
        
        emit(result)
    }

    /**
     * 获取任务进度
     */
    fun getProgress(taskId: String): Flow<Result<ProgressResponse>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.getProgress(taskId)
        }
        
        emit(result)
    }

    /**
     * 获取照片日期树
     */
    fun getPhotosDateTree(
        trashed: Boolean = false,
        star: Boolean = false
    ): Flow<Result<List<TreeNode>>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.getPicsDate(GetPicsDateRequest(trashed = trashed, star = star))
        }
        
        emit(result)
    }

    /**
     * 获取所有活动列表
     */
    fun getActivities(): Flow<Result<List<Activity>>> = flow {
        emit(Result.Loading)
        
        val result = safeApiCall {
            apiService.getActivities(emptyMap())
        }
        
        emit(result)
    }

    /**
     * 获取所有标签列表
     */
    fun getAllTags(): Flow<Result<List<String>>> = flow {
        Log.d(TAG, "getAllTags: 开始获取标签列表")
        emit(Result.Loading)
        
        val result = safeApiCall {
            Log.d(TAG, "getAllTags: 调用 API")
            apiService.getAllTags(emptyMap())
        }
        
        Log.d(TAG, "getAllTags: API 返回结果 - ${result.javaClass.simpleName}")
        if (result is Result.Success) {
            Log.d(TAG, "getAllTags: 成功获取 ${result.data.size} 个标签")
        } else if (result is Result.Error) {
            Log.e(TAG, "getAllTags: 获取失败 - ${result.exception.message}")
        }
        
        emit(result)
    }

    /**
     * 上传照片
     * @param uri 照片URI
     * @param fileName 文件名
     * @param lastModified 最后修改时间（毫秒时间戳）
     * @param contentType 文件类型（如：image/jpeg, video/mp4）
     */
    suspend fun uploadPhoto(
        uri: android.net.Uri,
        fileName: String,
        lastModified: Long,
        context: android.content.Context,
        contentType: String = "image/*"
    ): Result<Unit> {
        return try {
            Log.d(TAG, "uploadPhoto: 开始上传")
            Log.d(TAG, "  - uri: $uri")
            Log.d(TAG, "  - fileName: $fileName")
            Log.d(TAG, "  - lastModified: $lastModified")
            Log.d(TAG, "  - contentType: $contentType")
            
            // 读取文件内容
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.Error(Exception("无法打开文件"), "无法打开文件")
            
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            Log.d(TAG, "  - 文件大小: ${bytes.size} bytes")
            
            // 创建RequestBody，使用传入的contentType
            val mediaType = contentType.toMediaTypeOrNull()
            Log.d(TAG, "  - MediaType: $mediaType")
            
            val requestBody = bytes.toRequestBody(mediaType)
            
            // 创建MultipartBody.Part，明确指定Content-Type
            val filePart = okhttp3.MultipartBody.Part.createFormData(
                "file",
                fileName,
                requestBody
            )
            
            Log.d(TAG, "  - MultipartBody.Part 创建完成")
            Log.d(TAG, "    - filename: $fileName")
            Log.d(TAG, "    - Content-Type: $contentType")
            
            // 创建lastModified的RequestBody
            val lastModifiedBody = lastModified.toString().toRequestBody(
                "text/plain".toMediaTypeOrNull()
            )
            
            // 调用API（后端返回 {"code":0,"data":0}，data 是 Int 类型）
            val result = safeApiCall {
                apiService.uploadPhoto(filePart, lastModifiedBody)
            }
            
            Log.d(TAG, "uploadPhoto: 上传完成 $fileName - ${result.javaClass.simpleName}")
            
            // 将 Result<Int> 转换为 Result<Unit>
            when (result) {
                is Result.Success -> Result.Success(Unit)
                is Result.Error -> result
                else -> result as Result<Unit>
            }
        } catch (e: Exception) {
            Log.e(TAG, "uploadPhoto: 上传失败 $fileName", e)
            Result.Error(e, "上传失败: ${e.message}")
        }
    }
}
