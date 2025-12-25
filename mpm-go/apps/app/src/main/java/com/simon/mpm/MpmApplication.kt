package com.simon.mpm

import android.app.Application
import coil.Coil
import coil.ImageLoader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * MPM Application类
 * 使用Hilt进行依赖注入
 */
@HiltAndroidApp
class MpmApplication : Application() {
    
    @Inject
    lateinit var imageLoader: ImageLoader
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化Coil
        Coil.setImageLoader(imageLoader)
    }
}