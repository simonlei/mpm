package com.simon.mpm.network.model

import com.google.gson.annotations.SerializedName

/**
 * 登录请求参数
 */
data class LoginRequest(
    @SerializedName("account")
    val account: String,
    
    @SerializedName("passwd")
    val passwd: String
)

/**
 * 根据ID获取照片详情请求参数
 */
data class GetPhotoByIdRequest(
    @SerializedName("id")
    val id: Int
)

/**
 * 获取照片列表请求参数
 */
data class GetPicsRequest(
    @SerializedName("star")
    val star: Boolean? = null,
    
    @SerializedName("video")
    val video: Boolean? = null,
    
    @SerializedName("trashed")
    val trashed: Boolean? = null,
    
    @SerializedName("idOnly")
    val idOnly: Boolean? = null,
    
    @SerializedName("start")
    val start: Int = 0,
    
    @SerializedName("size")
    val size: Int = 75,
    
    @SerializedName("dateKey")
    val dateKey: String? = null,
    
    @SerializedName("path")
    val path: String? = null,
    
    @SerializedName("tag")
    val tag: String? = null,
    
    @SerializedName("faceId")
    val faceId: Int? = null,
    
    @SerializedName("order")
    val order: String? = null,
    
    @SerializedName("idRank")
    val idRank: Int? = null
)

/**
 * 更新照片信息请求参数
 */
data class UpdateImageRequest(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null,
    
    @SerializedName("takenDate")
    val takenDate: String? = null,
    
    @SerializedName("star")
    val star: Boolean? = null,
    
    @SerializedName("activity")
    val activity: Int? = null,
    
    @SerializedName("tags")
    val tags: String? = null,
    
    @SerializedName("rotate")
    val rotate: Int? = null
)

/**
 * 移动照片到回收站请求参数
 */
data class TrashPhotosRequest(
    @SerializedName("ids")
    val ids: List<Int>
)

/**
 * 创建或更新活动请求参数
 */
data class CreateOrUpdateActivityRequest(
    @SerializedName("activity")
    val activity: ActivityData,
    
    @SerializedName("fromPhoto")
    val fromPhoto: Int? = null
)

data class ActivityData(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("startDate")
    val startDate: String? = null,
    
    @SerializedName("endDate")
    val endDate: String? = null,
    
    @SerializedName("latitude")
    val latitude: Double? = null,
    
    @SerializedName("longitude")
    val longitude: Double? = null
)

/**
 * 删除活动请求参数
 */
data class DeleteActivityRequest(
    @SerializedName("id")
    val id: Int
)

/**
 * 获取人脸列表请求参数
 */
data class GetFacesRequest(
    @SerializedName("showHidden")
    val showHidden: Boolean? = null,
    
    @SerializedName("page")
    val page: Int = 1,
    
    @SerializedName("size")
    val size: Int = 20,
    
    @SerializedName("nameFilter")
    val nameFilter: String? = null
)

/**
 * 更新人脸信息请求参数
 */
data class UpdateFaceRequest(
    @SerializedName("faceId")
    val faceId: Int,
    
    @SerializedName("name")
    val name: String? = null,
    
    @SerializedName("selectedFace")
    val selectedFace: Int? = null,
    
    @SerializedName("hidden")
    val hidden: Boolean? = null,
    
    @SerializedName("collected")
    val collected: Boolean? = null
)

/**
 * 合并人脸请求参数
 */
data class MergeFaceRequest(
    @SerializedName("from")
    val from: Int,
    
    @SerializedName("to")
    val to: Int
)

/**
 * 获取照片日期树请求参数
 */
data class GetPicsDateRequest(
    @SerializedName("trashed")
    val trashed: Boolean? = null,
    
    @SerializedName("star")
    val star: Boolean? = null
)

/**
 * 获取文件夹数据请求参数
 */
data class GetFoldersDataRequest(
    @SerializedName("trashed")
    val trashed: Boolean? = null,
    
    @SerializedName("star")
    val star: Boolean? = null,
    
    @SerializedName("parentId")
    val parentId: Int? = null
)

/**
 * 切换文件夹回收站状态请求参数
 */
data class SwitchTrashFolderRequest(
    @SerializedName("to")
    val to: Boolean,
    
    @SerializedName("path")
    val path: String
)

/**
 * 更新文件夹日期请求参数
 */
data class UpdateFolderDateRequest(
    @SerializedName("path")
    val path: String,
    
    @SerializedName("toDate")
    val toDate: String
)

/**
 * 更新文件夹地理位置请求参数
 */
data class UpdateFolderGisRequest(
    @SerializedName("path")
    val path: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double
)

/**
 * 移动文件夹请求参数
 */
data class MoveFolderRequest(
    @SerializedName("fromPath")
    val fromPath: String,
    
    @SerializedName("toId")
    val toId: String,
    
    @SerializedName("merge")
    val merge: Boolean = false
)

/**
 * 获取照片中的人脸请求参数
 */
data class GetFacesForPhotoRequest(
    @SerializedName("id")
    val id: Int
)

/**
 * 删除照片人脸信息请求参数
 */
data class RemovePhotoFaceInfoRequest(
    @SerializedName("id")
    val id: Int
)

/**
 * 重新扫描人脸请求参数
 */
data class RescanFaceRequest(
    @SerializedName("id")
    val id: Int
)

/**
 * 获取照片数量请求参数
 */
data class GetCountRequest(
    @SerializedName("trashed")
    val trashed: Boolean? = null
)

/**
 * 加载用户请求参数
 */
data class LoadUserRequest(
    @SerializedName("id")
    val id: Int
)

/**
 * 删除用户请求参数
 */
data class DeleteUserRequest(
    @SerializedName("id")
    val id: Int
)

/**
 * 创建或更新用户请求参数
 */
data class CreateOrUpdateUserRequest(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("account")
    val account: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("passwd")
    val passwd: String? = null,
    
    @SerializedName("isAdmin")
    val isAdmin: Boolean = false,
    
    @SerializedName("faceId")
    val faceId: Int? = null
)

/**
 * 恢复照片请求参数
 */
data class RestorePhotosRequest(
    @SerializedName("ids")
    val ids: List<Int>
)

/**
 * 删除照片请求参数
 */
data class DeletePhotosRequest(
    @SerializedName("ids")
    val ids: List<Int>
)
