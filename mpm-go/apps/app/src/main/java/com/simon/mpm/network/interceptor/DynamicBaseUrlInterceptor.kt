package com.simon.mpm.network.interceptor

import com.simon.mpm.di.BaseUrlProvider
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 动态BaseUrl拦截器
 * 在每次请求时动态读取服务器地址，支持运行时修改服务器地址
 * 
 * 工作原理：
 * 1. 拦截每个HTTP请求
 * 2. 从BaseUrlProvider获取当前配置的服务器地址
 * 3. 替换请求的host和scheme
 * 4. 保持原始的path和query参数不变
 */
@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
    private val baseUrlProvider: BaseUrlProvider
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 获取当前配置的服务器地址
        val newBaseUrl = try {
            baseUrlProvider.getBaseUrl().toHttpUrl()
        } catch (e: Exception) {
            // 如果URL解析失败，使用原始请求
            return chain.proceed(originalRequest)
        }
        
        // 构建新的URL，替换host和scheme，保持path和query不变
        val newUrl = originalRequest.url.newBuilder()
            .scheme(newBaseUrl.scheme)
            .host(newBaseUrl.host)
            .port(newBaseUrl.port)
            .build()
        
        // 构建新的请求
        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()
        
        return chain.proceed(newRequest)
    }
}
