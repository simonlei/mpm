package com.simon.mpm.di

import android.content.Context
import coil.ImageLoader
import coil.util.DebugLogger
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.network.api.MpmApiService
import com.simon.mpm.network.interceptor.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 应用依赖注入模块
 * 整合了网络层和数据层的依赖
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    private const val DEFAULT_TIMEOUT = 30L
    
    // ========== 网络层依赖 ==========
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .serializeNulls()
            .create()
    }
    
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideAuthInterceptor(preferencesManager: PreferencesManager): AuthInterceptor {
        val authProvider = object : AuthProvider {
            override fun getSignature(): String? {
                return runBlocking { preferencesManager.signature.first() }
            }
            
            override fun getAccount(): String? {
                return runBlocking { preferencesManager.account.first() }
            }
        }
        return AuthInterceptor(authProvider)
    }
    
    @Provides
    @Singleton
    fun provideDynamicBaseUrlInterceptor(preferencesManager: PreferencesManager): DynamicBaseUrlInterceptor {
        val baseUrlProvider = object : BaseUrlProvider {
            override fun getBaseUrl(): String {
                val url = runBlocking {
                    try {
                        preferencesManager.serverUrl.first()
                    } catch (e: Exception) {
                        "http://10.0.2.2:8080"
                    }
                }
                
                return when {
                    url.isBlank() -> "http://10.0.2.2:8080"
                    url.startsWith("http://") || url.startsWith("https://") -> url
                    else -> "http://$url"
                }
            }
        }
        return DynamicBaseUrlInterceptor(baseUrlProvider)
    }
    
    @Provides
    @Singleton
    fun provideUnauthorizedInterceptor(preferencesManager: PreferencesManager): UnauthorizedInterceptor {
        val unauthorizedHandler = object : UnauthorizedHandler {
            override fun onUnauthorized() {
                runBlocking { preferencesManager.clearAuth() }
            }
        }
        return UnauthorizedInterceptor(unauthorizedHandler)
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        dynamicBaseUrlInterceptor: DynamicBaseUrlInterceptor,
        authInterceptor: AuthInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        // 配置信任所有SSL证书（仅用于开发环境）
        val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(
            object : javax.net.ssl.X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                    // 信任所有客户端证书
                }
                
                override fun checkServerTrusted(
                    chain: Array<out java.security.cert.X509Certificate>?,
                    authType: String?
                ) {
                    // 信任所有服务器证书
                }
                
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return emptyArray()
                }
            }
        )
        
        val sslSocketFactory = javax.net.ssl.SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, java.security.SecureRandom())
        }.socketFactory
        
        return OkHttpClient.Builder()
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as javax.net.ssl.X509TrustManager)
            .hostnameVerifier { _, _ -> true } // 信任所有主机名
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://placeholder.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMpmApiService(retrofit: Retrofit): MpmApiService {
        return retrofit.create(MpmApiService::class.java)
    }
    
    // ========== 图片加载 ==========
    
    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .logger(DebugLogger())
            .build()
    }
}

// BaseUrlProvider接口定义
interface BaseUrlProvider {
    fun getBaseUrl(): String
}
