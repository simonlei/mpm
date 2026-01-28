package com.simon.mpm.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.simon.mpm.common.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore扩展属性
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mpm_preferences")

/**
 * 应用配置管理类
 * 使用DataStore存储应用配置
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    
    // 定义所有的Key
    private object PreferencesKeys {
        val SERVER_URL = stringPreferencesKey(Constants.PREF_SERVER_URL)
        val ACCOUNT = stringPreferencesKey(Constants.PREF_ACCOUNT)
        val SIGNATURE = stringPreferencesKey(Constants.PREF_SIGNATURE)
        val IMAGE_QUALITY = stringPreferencesKey(Constants.PREF_IMAGE_QUALITY)
        val THEME_MODE = stringPreferencesKey(Constants.PREF_THEME_MODE)
        val UPLOAD_WIFI_ONLY = booleanPreferencesKey(Constants.PREF_UPLOAD_WIFI_ONLY)
        
        // 自动同步相关
        val AUTO_SYNC_ENABLED = booleanPreferencesKey(Constants.PREF_AUTO_SYNC_ENABLED)
        val SYNC_DIRECTORIES = stringPreferencesKey(Constants.PREF_SYNC_DIRECTORIES)
        val SYNC_INTERVAL = stringPreferencesKey(Constants.PREF_SYNC_INTERVAL)
        val SYNC_WIFI_ONLY = booleanPreferencesKey(Constants.PREF_SYNC_WIFI_ONLY)
        val SYNC_FILE_TYPES = stringPreferencesKey(Constants.PREF_SYNC_FILE_TYPES)
        val LAST_SYNC_TIME = stringPreferencesKey(Constants.PREF_LAST_SYNC_TIME)
        val SYNC_CONFLICT_STRATEGY = stringPreferencesKey(Constants.PREF_SYNC_CONFLICT_STRATEGY)
    }
    
    // 服务器地址
    // 注意：Android模拟器使用 10.0.2.2 访问开发机器的 localhost
    // 真实设备需要使用开发机器的局域网IP地址
    val serverUrl: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SERVER_URL] ?: "http://10.0.2.2:8080"
    }
    
    suspend fun setServerUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SERVER_URL] = url
        }
    }
    
    // 账号
    val account: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACCOUNT]
    }
    
    suspend fun setAccount(account: String?) {
        dataStore.edit { preferences ->
            if (account != null) {
                preferences[PreferencesKeys.ACCOUNT] = account
            } else {
                preferences.remove(PreferencesKeys.ACCOUNT)
            }
        }
    }
    
    // 签名
    val signature: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SIGNATURE]
    }
    
    suspend fun setSignature(signature: String?) {
        dataStore.edit { preferences ->
            if (signature != null) {
                preferences[PreferencesKeys.SIGNATURE] = signature
            } else {
                preferences.remove(PreferencesKeys.SIGNATURE)
            }
        }
    }
    
    // 图片质量
    val imageQuality: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IMAGE_QUALITY] ?: Constants.IMAGE_QUALITY_STANDARD
    }
    
    suspend fun setImageQuality(quality: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IMAGE_QUALITY] = quality
        }
    }
    
    // 主题模式
    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: Constants.THEME_MODE_SYSTEM
    }
    
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }
    
    // 仅WiFi上传
    val uploadWifiOnly: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.UPLOAD_WIFI_ONLY] ?: true
    }
    
    suspend fun setUploadWifiOnly(wifiOnly: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.UPLOAD_WIFI_ONLY] = wifiOnly
        }
    }
    
    // 清除所有认证信息
    suspend fun clearAuth() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACCOUNT)
            preferences.remove(PreferencesKeys.SIGNATURE)
        }
    }
    
    // 自动同步开关
    val autoSyncEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTO_SYNC_ENABLED] ?: false
    }
    
    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SYNC_ENABLED] = enabled
        }
    }
    
    // 同步目录列表（使用分号分隔）
    val syncDirectories: Flow<List<String>> = dataStore.data.map { preferences ->
        val directoriesString = preferences[PreferencesKeys.SYNC_DIRECTORIES] ?: ""
        if (directoriesString.isEmpty()) emptyList() else directoriesString.split(";")
    }
    
    suspend fun setSyncDirectories(directories: List<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_DIRECTORIES] = directories.joinToString(";")
        }
    }
    
    // 同步间隔
    val syncInterval: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SYNC_INTERVAL] ?: Constants.SYNC_INTERVAL_WIFI_ONLY
    }
    
    suspend fun setSyncInterval(interval: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_INTERVAL] = interval
        }
    }
    
    // 仅WiFi同步
    val syncWifiOnly: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SYNC_WIFI_ONLY] ?: true
    }
    
    suspend fun setSyncWifiOnly(wifiOnly: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_WIFI_ONLY] = wifiOnly
        }
    }
    
    // 同步文件类型
    val syncFileTypes: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SYNC_FILE_TYPES] ?: Constants.SYNC_FILE_TYPE_ALL
    }
    
    suspend fun setSyncFileTypes(fileTypes: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_FILE_TYPES] = fileTypes
        }
    }
    
    // 最后同步时间
    val lastSyncTime: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LAST_SYNC_TIME]
    }
    
    suspend fun setLastSyncTime(time: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_SYNC_TIME] = time
        }
    }
    
    // 同步冲突策略
    val syncConflictStrategy: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SYNC_CONFLICT_STRATEGY] ?: "SKIP"
    }
    
    suspend fun setSyncConflictStrategy(strategy: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_CONFLICT_STRATEGY] = strategy
        }
    }
    
    // 清除所有配置
    suspend fun clearAll() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}