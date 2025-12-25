package com.simon.mpm.data.repository

import com.simon.mpm.common.Result
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.network.api.MpmApiService
import com.simon.mpm.network.model.GetPicsRequest
import com.simon.mpm.network.model.LoginRequest
import com.simon.mpm.network.util.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证Repository
 * 处理用户登录、登出、认证状态管理
 */
@Singleton
class AuthRepository @Inject constructor(
    private val apiService: MpmApiService,
    private val preferencesManager: PreferencesManager
) : BaseRepository() {
    
    /**
     * 登录
     * @param account 账号
     * @param password 密码
     * @return Result<Unit> 登录结果
     */
    suspend fun login(account: String, password: String): Result<Unit> {
        return safeCall {
            // 调用登录API
            val result = safeApiCall {
                apiService.checkPassword(LoginRequest(account, password))
            }
            
            when (result) {
                is Result.Success -> {
                    // 保存认证信息
                    preferencesManager.setAccount(result.data.account)
                    preferencesManager.setSignature(result.data.signature)
                }
                is Result.Error -> {
                    throw result.exception
                }
                is Result.Loading -> {
                    // 不应该到这里
                }
            }
        }
    }
    
    /**
     * 登出
     * 清除本地认证信息
     */
    suspend fun logout() {
        preferencesManager.clearAuth()
    }
    
    /**
     * 检查是否已登录
     * @return Flow<Boolean> 是否已登录
     */
    fun isLoggedIn(): Flow<Boolean> {
        return preferencesManager.signature.map { signature ->
            !signature.isNullOrEmpty()
        }
    }
    
    /**
     * 获取当前账号
     * @return Flow<String?> 当前账号
     */
    fun getCurrentAccount(): Flow<String?> {
        return preferencesManager.account
    }
    
    /**
     * 检查认证信息是否有效
     * 通过尝试获取照片列表来验证
     */
    suspend fun checkAuthValid(): Result<Boolean> {
        return safeCall {
            val result = safeApiCall {
                apiService.getPics(GetPicsRequest(start = 0, size = 1))
            }
            
            when (result) {
                is Result.Success -> true
                is Result.Error -> {
                    // 如果是401错误，说明认证失效
                    if (result.exception.message?.contains("401") == true) {
                        logout()
                        false
                    } else {
                        // 其他错误不影响认证状态
                        true
                    }
                }
                is Result.Loading -> true
            }
        }
    }
    
    /**
     * 获取服务器地址
     */
    fun getServerUrl(): Flow<String> {
        return preferencesManager.serverUrl
    }
    
    /**
     * 设置服务器地址
     */
    suspend fun setServerUrl(url: String) {
        preferencesManager.setServerUrl(url)
    }
}