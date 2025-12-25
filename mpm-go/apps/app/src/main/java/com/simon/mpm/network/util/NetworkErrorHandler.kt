package com.simon.mpm.network.util

import com.simon.mpm.common.Result
import com.simon.mpm.network.model.ApiException
import com.simon.mpm.network.model.ApiResponse
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 网络错误处理工具
 */
object NetworkErrorHandler {
    
    /**
     * 处理网络异常，转换为友好的错误消息
     */
    fun handleException(throwable: Throwable): String {
        return when (throwable) {
            is ApiException -> throwable.message
            is HttpException -> handleHttpException(throwable)
            is SocketTimeoutException -> "网络连接超时，请检查网络设置"
            is UnknownHostException -> "无法连接到服务器，请检查网络设置"
            is IOException -> "网络连接失败，请检查网络设置"
            else -> throwable.message ?: "未知错误"
        }
    }
    
    private fun handleHttpException(exception: HttpException): String {
        return when (exception.code()) {
            400 -> "请求参数错误"
            401 -> "未授权，请重新登录"
            403 -> "禁止访问"
            404 -> "请求的资源不存在"
            408 -> "请求超时"
            500 -> "服务器内部错误"
            502 -> "网关错误"
            503 -> "服务不可用"
            504 -> "网关超时"
            else -> "网络错误 (${exception.code()})"
        }
    }
    
    /**
     * 判断是否为认证错误
     */
    fun isAuthError(throwable: Throwable): Boolean {
        return when (throwable) {
            is HttpException -> throwable.code() == 401
            is ApiException -> throwable.code == 401
            else -> false
        }
    }
}

/**
 * 安全执行API调用，自动处理异常
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> ApiResponse<T>
): Result<T> {
    return try {
        val response = apiCall()
        if (response.isSuccess()) {
            Result.Success(response.getDataOrThrow())
        } else {
            Result.Error(
                ApiException(response.code, response.message ?: "Unknown error"),
                response.message
            )
        }
    } catch (e: Exception) {
        Result.Error(e, NetworkErrorHandler.handleException(e))
    }
}

/**
 * 安全执行API调用（无返回数据）
 */
suspend fun safeApiCallUnit(
    apiCall: suspend () -> ApiResponse<Unit>
): Result<Unit> {
    return try {
        val response = apiCall()
        if (response.isSuccess()) {
            Result.Success(Unit)
        } else {
            Result.Error(
                ApiException(response.code, response.message ?: "Unknown error"),
                response.message
            )
        }
    } catch (e: Exception) {
        Result.Error(e, NetworkErrorHandler.handleException(e))
    }
}