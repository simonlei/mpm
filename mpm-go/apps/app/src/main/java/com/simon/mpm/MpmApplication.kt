package com.simon.mpm

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoader
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.service.MediaContentObserver
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    
    @Inject
    lateinit var mediaContentObserver: MediaContentObserver
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Coil
        Coil.setImageLoader(imageLoader)
        
        // 异步初始化媒体内容观察者（不阻塞应用启动）
        initMediaObserver()
    }
    
    /**
     * 初始化媒体内容观察者
     * 使用IO线程异步初始化，避免阻塞主线程
     */
    private fun initMediaObserver() {
        applicationScope.launch(Dispatchers.IO) {
            try {
                // 检查是否启用自动同步
                val autoSyncEnabled = preferencesManager.autoSyncEnabled.first()
                if (autoSyncEnabled) {
                    mediaContentObserver.register()
                }
            } catch (e: Exception) {
                // 忽略错误，不影响应用启动
                android.util.Log.e("MpmApplication", "Failed to init media observer", e)
            }
        }
    }
    
    override fun onTerminate() {
        super.onTerminate()
        // 取消注册观察者
        mediaContentObserver.unregister()
    }
    
    /**
     * 提供WorkManager配置
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}