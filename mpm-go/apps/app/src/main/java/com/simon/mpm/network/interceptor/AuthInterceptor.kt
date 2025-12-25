package com.simon.mpm.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证拦截器
 * 自动在请求头中添加Signature和Account
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val authProvider: AuthProvider
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 获取认证信息
        val signature = authProvider.getSignature()
        val account = authProvider.getAccount()
        
        // 如果没有认证信息，直接返回原始请求
        if (signature.isNullOrEmpty() || account.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // 添加认证头
        val newRequest = originalRequest.newBuilder()
            .addHeader("Signature", signature)
            .addHeader("Account", account)
            .build()
        
        return chain.proceed(newRequest)
    }
}

/**
 * 认证信息提供者接口
 * 由数据层实现
 */
interface AuthProvider {
    fun getSignature(): String?
    fun getAccount(): String?
}