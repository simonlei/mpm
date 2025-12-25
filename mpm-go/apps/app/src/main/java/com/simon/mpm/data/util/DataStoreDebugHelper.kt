package com.simon.mpm.data.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.simon.mpm.data.datastore.PreferencesManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore调试工具
 * 用于开发阶段调试和清除缓存数据
 */
@Singleton
class DataStoreDebugHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    /**
     * 打印所有存储的配置
     */
    suspend fun printAllPreferences(): String {
        val signature = preferencesManager.signature.first()
        val account = preferencesManager.account.first()
        val serverUrl = preferencesManager.serverUrl.first()
        
        return buildString {
            appendLine("=== DataStore Preferences ===")
            appendLine("Server URL: $serverUrl")
            appendLine("Account: $account")
            appendLine("Signature: ${signature?.take(20)}...")
            appendLine("===========================")
        }
    }
    
    /**
     * 清除所有数据
     */
    suspend fun clearAll() {
        preferencesManager.clearAll()
    }
    
    /**
     * 重置为默认配置
     */
    suspend fun resetToDefault() {
        clearAll()
        preferencesManager.setServerUrl("http://10.0.2.2:8080")
    }
    
    /**
     * 验证服务器URL格式
     */
    suspend fun validateServerUrl(): Pair<Boolean, String> {
        val url = preferencesManager.serverUrl.first()
        
        return when {
            url.isBlank() -> false to "URL is blank"
            !url.startsWith("http://") && !url.startsWith("https://") -> 
                false to "URL missing http:// or https:// scheme"
            url.length < 10 -> false to "URL too short: $url"
            else -> true to "URL is valid: $url"
        }
    }
}
