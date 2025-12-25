package com.simon.mpm.network.api

import com.simon.mpm.network.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * MPM API服务接口
 */
interface MpmApiService {
    
    /**
     * 检查密码（登录）
     */
    @POST("/api/checkPassword")
    suspend fun checkPassword(
        @Body request: LoginRequest
    ): ApiResponse<LoginResponse>
    
    /**
     * 获取照片列表
     */
    @POST("/api/getPics")
    suspend fun getPics(
        @Body request: GetPicsRequest
    ): ApiResponse<PicsResponse>
    
    /**
     * 根据ID获取照片详情
     */
    @POST("/api/getPhotoById")
    suspend fun getPhotoById(
        @Body request: GetPhotoByIdRequest
    ): ApiResponse<Photo>
    
    /**
     * 更新照片信息
     */
    @POST("/api/updateImage")
    suspend fun updateImage(
        @Body request: UpdateImageRequest
    ): ApiResponse<Photo>
    
    /**
     * 移动照片到回收站/恢复照片
     */
    @POST("/api/trashPhotos")
    suspend fun trashPhotos(
        @Body request: TrashPhotosRequest
    ): ApiResponse<Unit>
    
    /**
     * 清空回收站
     */
    @POST("/api/emptyTrash")
    suspend fun emptyTrash(): ApiResponse<TaskResponse>
    
    /**
     * 获取任务进度
     */
    @GET("/api/getProgress/{taskId}")
    suspend fun getProgress(
        @Path("taskId") taskId: String
    ): ApiResponse<ProgressResponse>
    
    /**
     * 上传照片
     */
    @Multipart
    @POST("/api/uploadPhoto")
    suspend fun uploadPhoto(
        @Part file: MultipartBody.Part,
        @Part("batchId") batchId: RequestBody
    ): ApiResponse<Unit>
    
    /**
     * 获取活动列表
     */
    @POST("/api/getActivities")
    suspend fun getActivities(@Body request: Map<String, Any> = emptyMap()): ApiResponse<List<Activity>>
    
    /**
     * 创建或更新活动
     */
    @POST("/api/createOrUpdateActivity")
    suspend fun createOrUpdateActivity(
        @Body request: CreateOrUpdateActivityRequest
    ): ApiResponse<Activity>
    
    /**
     * 删除活动
     */
    @POST("/api/deleteActivity")
    suspend fun deleteActivity(
        @Body request: DeleteActivityRequest
    ): ApiResponse<Unit>
    
    /**
     * 获取人脸列表
     */
    @POST("/api/getFaces")
    suspend fun getFaces(
        @Body request: GetFacesRequest
    ): ApiResponse<FaceListResponse>
    
    /**
     * 更新人脸信息
     */
    @POST("/api/updateFace")
    suspend fun updateFace(
        @Body request: UpdateFaceRequest
    ): ApiResponse<Unit>
    
    /**
     * 合并人脸
     */
    @POST("/api/mergeFace")
    suspend fun mergeFace(
        @Body request: MergeFaceRequest
    ): ApiResponse<Unit>
    
    /**
     * 获取照片日期树
     */
    @POST("/api/getPicsDate")
    suspend fun getPicsDate(
        @Body request: GetPicsDateRequest
    ): ApiResponse<List<TreeNode>>
    
    /**
     * 获取照片数量
     */
    @POST("/api/getCount")
    suspend fun getCount(
        @Body request: GetCountRequest
    ): ApiResponse<Int>
    
    /**
     * 恢复照片
     */
    @POST("/api/restorePhotos")
    suspend fun restorePhotos(
        @Body request: RestorePhotosRequest
    ): ApiResponse<Unit>
    
    /**
     * 删除照片
     */
    @POST("/api/deletePhotos")
    suspend fun deletePhotos(
        @Body request: DeletePhotosRequest
    ): ApiResponse<Unit>
    
    /**
     * 获取文件夹树
     */
    @POST("/api/getFoldersData")
    suspend fun getFoldersData(@Body request: GetFoldersDataRequest): ApiResponse<FolderTreeResponse>
    
    /**
     * 获取所有标签
     */
    @POST("/api/getAllTags")
    suspend fun getAllTags(@Body request: Map<String, Any> = emptyMap()): ApiResponse<List<String>>
    
    /**
     * 获取已命名的人脸列表
     */
    @POST("/api/getFacesWithName")
    suspend fun getFacesWithName(@Body request: Map<String, Any> = emptyMap()): ApiResponse<List<NamedFace>>
    
    /**
     * 获取照片的人脸信息
     */
    @POST("/api/getFacesForPhoto")
    suspend fun getFacesForPhoto(
        @Body request: GetFacesForPhotoRequest
    ): ApiResponse<List<PhotoFace>>
    
    /**
     * 删除照片的人脸信息
     */
    @POST("/api/removePhotoFaceInfo")
    suspend fun removePhotoFaceInfo(
        @Body request: RemovePhotoFaceInfoRequest
    ): ApiResponse<Unit>
    
    /**
     * 重新扫描人脸
     */
    @POST("/api/rescanFace")
    suspend fun rescanFace(
        @Body request: RescanFaceRequest
    ): ApiResponse<Unit>
    
    /**
     * 移动文件夹
     */
    @POST("/api/moveFolder")
    suspend fun moveFolder(
        @Body request: MoveFolderRequest
    ): ApiResponse<Unit>
    
    /**
     * 切换文件夹回收站状态
     */
    @POST("/api/switchTrashFolder")
    suspend fun switchTrashFolder(
        @Body request: SwitchTrashFolderRequest
    ): ApiResponse<Unit>
    
    /**
     * 批量更新文件夹日期
     */
    @POST("/api/updateFolderDate")
    suspend fun updateFolderDate(
        @Body request: UpdateFolderDateRequest
    ): ApiResponse<TaskResponse>
    
    /**
     * 批量更新文件夹位置
     */
    @POST("/api/updateFolderGis")
    suspend fun updateFolderGis(
        @Body request: UpdateFolderGisRequest
    ): ApiResponse<TaskResponse>
    
    /**
     * 加载地图标记GeoJSON
     */
    @GET("/geo_json_api/loadMarkersGeoJson")
    suspend fun loadMarkersGeoJson(): ApiResponse<GeoJsonResponse>
}