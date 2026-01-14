package com.simon.mpm.data.repository

import com.simon.mpm.common.Result
import com.simon.mpm.network.api.MpmApiService
import com.simon.mpm.network.model.*
import com.simon.mpm.network.util.NetworkErrorHandler
import com.simon.mpm.network.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 活动数据仓库
 * 负责活动相关的数据操作
 */
@Singleton
class ActivityRepository @Inject constructor(
    private val apiService: MpmApiService
) : BaseRepository() {

    /**
     * 获取活动列表
     */
    fun getActivities(): Flow<Result<List<Activity>>> = flow {
        emit(Result.Loading)
        val result = safeApiCall {
            apiService.getActivities(emptyMap())
        }
        emit(result)
    }

    /**
     * 创建活动
     */
    fun createActivity(
        name: String,
        description: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        fromPhoto: Int? = null
    ): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.createOrUpdateActivity(
                CreateOrUpdateActivityRequest(
                    activity = ActivityData(
                        id = 0,
                        name = name,
                        description = description,
                        startDate = startDate,
                        endDate = endDate,
                        latitude = latitude,
                        longitude = longitude
                    ),
                    fromPhoto = fromPhoto
                )
            )
            if (response.isSuccess()) {
                emit(Result.Success(Unit))
            } else {
                emit(Result.Error(
                    ApiException(response.code, response.message ?: "Unknown error"),
                    response.message
                ))
            }
        } catch (e: Exception) {
            emit(Result.Error(e, NetworkErrorHandler.handleException(e)))
        }
    }

    /**
     * 更新活动
     */
    fun updateActivity(
        id: Int,
        name: String,
        description: String? = null,
        startDate: String? = null,
        endDate: String? = null,
        latitude: Double? = null,
        longitude: Double? = null
    ): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val response = apiService.createOrUpdateActivity(
                CreateOrUpdateActivityRequest(
                    activity = ActivityData(
                        id = id,
                        name = name,
                        description = description,
                        startDate = startDate,
                        endDate = endDate,
                        latitude = latitude,
                        longitude = longitude
                    ),
                    fromPhoto = null
                )
            )
            if (response.isSuccess()) {
                emit(Result.Success(Unit))
            } else {
                emit(Result.Error(
                    ApiException(response.code, response.message ?: "Unknown error"),
                    response.message
                ))
            }
        } catch (e: Exception) {
            emit(Result.Error(e, NetworkErrorHandler.handleException(e)))
        }
    }

    /**
     * 删除活动
     */
    fun deleteActivity(id: Int): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        val result = safeApiCall {
            apiService.deleteActivity(DeleteActivityRequest(id))
        }
        emit(result)
    }
}
