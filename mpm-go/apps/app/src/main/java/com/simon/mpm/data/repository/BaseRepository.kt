package com.simon.mpm.data.repository

import com.simon.mpm.common.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository基类
 * 提供通用的数据操作方法
 */
abstract class BaseRepository {
    
    /**
     * 执行网络请求并返回Flow<Result<T>>
     */
    protected fun <T> flowResult(
        apiCall: suspend () -> T
    ): Flow<Result<T>> = flow {
        emit(Result.Loading)
        val result = apiCall()
        emit(Result.Success(result))
    }.catch { e ->
        emit(Result.Error(e, e.message))
    }.flowOn(Dispatchers.IO)
    
    /**
     * 在IO线程执行操作
     */
    protected suspend fun <T> ioOperation(
        operation: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        operation()
    }
    
    /**
     * 安全执行操作，捕获异常并返回Result
     */
    protected suspend fun <T> safeCall(
        call: suspend () -> T
    ): Result<T> {
        return try {
            Result.Success(call())
        } catch (e: Exception) {
            Result.Error(e, e.message)
        }
    }
}