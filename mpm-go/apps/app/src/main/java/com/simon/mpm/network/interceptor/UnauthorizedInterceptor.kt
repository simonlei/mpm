package com.simon.mpm.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 401错误拦截器
 * 当收到401响应时，触发登出回调
 */
@Singleton
class UnauthorizedInterceptor @Inject constructor(
    private val unauthorizedHandler: UnauthorizedHandler
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        
        // 检查是否为401未授权
        if (response.code == 401) {
            // 触发登出处理
            unauthorizedHandler.onUnauthorized()
        }
        
        return response
    }
}

/**
 * 401未授权处理器接口
 * 由应用层实现，处理登出逻辑
 */
interface UnauthorizedHandler {
    fun onUnauthorized()
}