# URL配置问题修复指南

## 问题描述

应用闪退，错误信息：
```
java.lang.IllegalArgumentException: Expected URL scheme 'http' or 'https' but no scheme was found for 127.0....
```

## 根本原因

1. **DataStore中存储了损坏的URL数据**：可能是旧版本的 `localhost` 配置被截断
2. **URL格式验证不足**：没有在使用前验证URL格式的完整性
3. **同步读取Flow数据**：使用 `runBlocking` 可能在DataStore未完全初始化时读取数据

## 已实施的修复

### 1. ✅ 增强BaseUrlProvider的URL验证

**文件**: `core-data/di/DataModule.kt`

```kotlin
@Provides
@Singleton
fun provideBaseUrlProvider(
    preferencesManager: PreferencesManager
): BaseUrlProvider {
    return object : BaseUrlProvider {
        override fun getBaseUrl(): String {
            val url = runBlocking {
                try {
                    preferencesManager.serverUrl.first()
                } catch (e: Exception) {
                    // 如果读取失败，返回默认值
                    "http://10.0.2.2:8080"
                }
            }
            
            // 验证URL格式，确保包含http://或https://
            return when {
                url.isBlank() -> "http://10.0.2.2:8080"
                url.startsWith("http://") || url.startsWith("https://") -> url
                else -> "http://$url" // 自动添加http://前缀
            }
        }
    }
}
```

**修复内容**：
- ✅ 添加异常捕获，读取失败时返回默认值
- ✅ 验证URL是否为空
- ✅ 验证URL是否包含正确的scheme（http://或https://）
- ✅ 自动添加缺失的http://前缀

### 2. ✅ 在应用启动时验证和修复URL

**文件**: `feature/splash/SplashViewModel.kt`

```kotlin
private suspend fun validateAndFixServerUrl() {
    try {
        val currentUrl = preferencesManager.serverUrl.first()
        Log.d("SplashViewModel", "Current server URL: $currentUrl")
        
        // 验证URL格式
        val isValid = currentUrl.isNotBlank() && 
                     (currentUrl.startsWith("http://") || currentUrl.startsWith("https://")) &&
                     currentUrl.length > 10
        
        if (!isValid) {
            // URL格式不正确，重置为默认值
            val defaultUrl = "http://10.0.2.2:8080"
            Log.w("SplashViewModel", "Invalid URL detected: $currentUrl, resetting to: $defaultUrl")
            preferencesManager.setServerUrl(defaultUrl)
        }
    } catch (e: Exception) {
        Log.e("SplashViewModel", "Error validating server URL", e)
        // 发生错误时，重置为默认值
        preferencesManager.setServerUrl("http://10.0.2.2:8080")
    }
}
```

**修复内容**：
- ✅ 应用启动时自动检查URL格式
- ✅ 发现无效URL时自动重置为默认值
- ✅ 添加详细的日志记录，便于调试

### 3. ✅ 创建DataStore调试工具

**文件**: `core-data/util/DataStoreDebugHelper.kt`

提供以下调试方法：
- `printAllPreferences()` - 打印所有配置
- `clearAll()` - 清除所有数据
- `resetToDefault()` - 重置为默认配置
- `validateServerUrl()` - 验证URL格式

## 手动修复方法

如果问题仍然存在，可以手动清除应用数据：

### 方法1：清除应用数据（推荐）

1. 打开Android设置
2. 进入 应用管理 → MPM
3. 点击 "存储" → "清除数据"
4. 重新启动应用

### 方法2：卸载重装

1. 卸载应用
2. 重新安装
3. 首次启动会使用默认配置

### 方法3：使用ADB命令

```bash
# 清除应用数据
adb shell pm clear com.simon.mpm

# 或者删除DataStore文件
adb shell run-as com.simon.mpm rm -rf /data/data/com.simon.mpm/files/datastore/
```

## 验证修复

### 1. 查看Logcat日志

启动应用后，在Logcat中搜索 `SplashViewModel`，应该看到：

```
D/SplashViewModel: Current server URL: http://10.0.2.2:8080
```

如果看到警告信息：
```
W/SplashViewModel: Invalid URL detected: 127.0...., resetting to: http://10.0.2.2:8080
```

说明修复逻辑已生效，URL已被自动修复。

### 2. 检查网络请求

在Logcat中搜索 `OkHttp`，应该看到：

```
D/OkHttp: --> POST http://10.0.2.2:8080/api/checkPassword
```

确认URL格式正确。

### 3. 测试登录功能

尝试登录，如果能正常发送请求（即使失败也没关系），说明URL配置已修复。

## 预防措施

### 1. 添加URL格式验证

在设置界面允许用户修改服务器地址时，应该添加格式验证：

```kotlin
fun validateUrl(url: String): Boolean {
    return url.isNotBlank() &&
           (url.startsWith("http://") || url.startsWith("https://")) &&
           url.length > 10 &&
           !url.contains(" ")
}
```

### 2. 提供URL输入提示

在设置界面提供示例：
- Android模拟器：`http://10.0.2.2:8080`
- 真实设备：`http://192.168.1.100:8080`
- 生产环境：`https://your-domain.com`

### 3. 添加URL测试功能

在设置界面添加"测试连接"按钮，验证URL是否可用。

## 技术细节

### DataStore数据存储位置

```
/data/data/com.simon.mpm/files/datastore/preferences.preferences_pb
```

### URL验证规则

1. ✅ 不能为空
2. ✅ 必须以 `http://` 或 `https://` 开头
3. ✅ 长度至少10个字符
4. ✅ 不能包含空格

### 默认URL配置

- **Android模拟器**: `http://10.0.2.2:8080`
- **真实设备**: 需要手动配置为开发机器的局域网IP
- **生产环境**: 配置为实际的服务器地址

## 相关文档

- [网络配置指南](NETWORK-CONFIGURATION.md) - 完整的网络配置说明
- [JSON-RPC实现](JSON-RPC-IMPLEMENTATION.md) - API协议说明
- [项目进度](../PROGRESS.md) - 开发进度

## 总结

此次修复通过以下三层防护确保URL配置的正确性：

1. **BaseUrlProvider层**：读取时验证和修复
2. **应用启动层**：启动时检查和修复
3. **用户输入层**：输入时验证（待实现）

现在应用应该能够正常启动，不会再出现URL格式错误导致的闪退问题。
