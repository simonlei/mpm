package com.simon.mpm.network.model

import com.google.gson.annotations.SerializedName

/**
 * API统一响应封装
 * 
 * 后端返回格式：
 * - 成功: {"code": 0, "data": {...}}
 * - 失败: {"code": -20001, "message": "错误信息"}
 */
data class ApiResponse<T>(
    @SerializedName("code")
    val code: Int,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("data")
    val data: T?
) {
    /**
     * 判断请求是否成功
     * 后端约定：code == 0 表示成功
     */
    fun isSuccess(): Boolean = code == 0
    
    /**
     * 获取数据或抛出异常
     * 如果请求失败或数据为null，抛出ApiException
     */
    fun getDataOrThrow(): T {
        if (!isSuccess()) {
            throw ApiException(code, message ?: "Unknown error")
        }
        return data ?: throw ApiException(code, "Data is null")
    }
}

/**
 * API异常
 */
class ApiException(
    val code: Int,
    override val message: String
) : Exception(message)

/**
 * 照片对象
 */
data class Photo(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("taken_date")
    val takenDate: String?,
    
    @SerializedName("latitude")
    val latitude: Double?,
    
    @SerializedName("longitude")
    val longitude: Double?,
    
    @SerializedName("address")
    val address: String?,
    
    @SerializedName("width")
    val width: Int,
    
    @SerializedName("height")
    val height: Int,
    
    @SerializedName("rotate")
    val rotate: Int,
    
    @SerializedName("star")
    val star: Boolean,
    
    @SerializedName("trashed")
    val trashed: Boolean,
    
    @SerializedName("thumb")
    val thumb: String?,
    
    @SerializedName("activity")
    val activity: Int?,
    
    @SerializedName("activity_desc")
    val activityDesc: String?,
    
    @SerializedName("tag")
    val tag: String?,
    
    @SerializedName("media_type")
    val mediaType: String? = null
)

/**
 * 照片列表响应
 */
data class PicsResponse(
    @SerializedName("totalRows")
    val totalRows: Int,
    
    @SerializedName("startRow")
    val startRow: Int,
    
    @SerializedName("endRow")
    val endRow: Int,
    
    @SerializedName("data")
    val data: List<Photo>?
)

/**
 * 树节点（用于日期树、文件夹树等）
 */
data class TreeNode(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("year")
    val year: Int?,
    
    @SerializedName("month")
    val month: Int?,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("children")
    val children: List<TreeNode>?
)

/**
 * 登录响应
 */
data class LoginResponse(
    @SerializedName("signature")
    val signature: String,
    
    @SerializedName("account")
    val account: String
)

/**
 * 任务响应
 */
data class TaskResponse(
    @SerializedName("taskId")
    val taskId: String
)

/**
 * 进度响应
 */
data class ProgressResponse(
    @SerializedName("progress")
    val progress: Int,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("completed")
    val completed: Boolean
)

/**
 * 活动
 */
data class Activity(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("start_date")
    val startDate: String?,
    
    @SerializedName("end_date")
    val endDate: String?,
    
    @SerializedName("latitude")
    val latitude: Double?,
    
    @SerializedName("longitude")
    val longitude: Double?,
    
    @SerializedName("photo_count")
    val photoCount: Int
)

/**
 * 人脸列表响应
 */
data class FaceListResponse(
    @SerializedName("faces")
    val faces: List<Face>,
    
    @SerializedName("total")
    val total: Int
)

/**
 * 人脸
 */
data class Face(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("hidden")
    val hidden: Boolean,
    
    @SerializedName("star")
    val star: Boolean,
    
    @SerializedName("photoCount")
    val photoCount: Int
)

/**
 * 已命名的人脸
 */
data class NamedFace(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("photoCount")
    val photoCount: Int
)

/**
 * 照片人脸
 */
data class PhotoFace(
    @SerializedName("face_id")
    val faceId: Int,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("x")
    val x: Int,
    
    @SerializedName("y")
    val y: Int,
    
    @SerializedName("width")
    val width: Int,
    
    @SerializedName("height")
    val height: Int
)

/**
 * 文件夹树响应
 */
data class FolderTreeResponse(
    @SerializedName("folders")
    val folders: List<FolderNode>
)

/**
 * 文件夹节点
 */
data class FolderNode(
    @SerializedName("path")
    val path: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("photoCount")
    val photoCount: Int,
    
    @SerializedName("children")
    val children: List<FolderNode>?
)

/**
 * GeoJSON响应
 */
data class GeoJsonResponse(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("features")
    val features: List<GeoFeature>
)

/**
 * GeoJSON特征
 */
data class GeoFeature(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("geometry")
    val geometry: GeoGeometry,
    
    @SerializedName("properties")
    val properties: GeoProperties
)

/**
 * GeoJSON几何
 */
data class GeoGeometry(
    @SerializedName("type")
    val type: String,
    
    @SerializedName("coordinates")
    val coordinates: List<Double>
)

/**
 * GeoJSON属性
 */
data class GeoProperties(
    @SerializedName("photo_id")
    val photoId: Int,
    
    @SerializedName("thumbnailUrl")
    val thumbnailUrl: String
)