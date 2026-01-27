package com.simon.mpm

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * MPM Application类
 * 使用Hilt进行依赖注入
 */
@HiltAndroidApp
class MpmApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var imageLoader: ImageLoader
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Coil
        Coil.setImageLoader(imageLoader)
    }
    
    /**
     * 提供WorkManager配置
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}