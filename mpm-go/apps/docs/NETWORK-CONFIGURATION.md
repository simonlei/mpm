# 网络配置指南

## 问题说明

Android应用无法直接使用 `localhost` 或 `127.0.0.1` 访问开发机器上的服务，因为这些地址指向的是Android设备/模拟器本身，而不是开发机器。

## 解决方案

### 1. Android模拟器

使用特殊IP地址 `10.0.2.2` 访问开发机器的 `localhost`：

```kotlin
// 默认配置（PreferencesManager.kt）
val serverUrl: Flow<String> = dataStore.data.map { preferences ->
    preferences[PreferencesKeys.SERVER_URL] ?: "http://10.0.2.2:8080"
}
```

**说明**：
- `10.0.2.2` 是Android模拟器的特殊IP，映射到开发机器的 `127.0.0.1`
- 端口号保持不变（如 `8080`）

### 2. 真实Android设备

需要使用开发机器的局域网IP地址：

#### 步骤1：查找开发机器的IP地址

**Windows**:
```bash
ipconfig
# 查找 "IPv4 地址" 或 "IPv4 Address"
# 例如：192.168.1.100
```

**macOS/Linux**:
```bash
ifconfig
# 或
ip addr show
# 查找 inet 地址
# 例如：192.168.1.100
```

#### 步骤2：配置服务器地址

在应用的设置界面中配置服务器地址为：
```
http://192.168.1.100:8080
```

**注意**：
- 确保开发机器和Android设备在同一局域网内
- 确保防火墙允许8080端口的访问

### 3. 使用真实服务器

如果后端部署在公网服务器上：
```
http://your-server-domain.com:8080
# 或
https://your-server-domain.com
```

## 网络安全配置

### HTTP明文传输支持

Android 9 (API 28) 及以上版本默认禁止HTTP明文传输，只允许HTTPS。为了开发调试，我们添加了网络安全配置。

#### 配置文件位置
`app/src/main/res/xml/network_security_config.xml`

#### 当前配置（开发环境）
```xml
<network-security-config>
    <!-- 允许所有域名使用HTTP -->
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

#### 生产环境配置（推荐）
```xml
<network-security-config>
    <!-- 仅允许特定域名使用HTTP -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">192.168.0.0/16</domain>
    </domain-config>
    
    <!-- 其他域名强制使用HTTPS -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

#### AndroidManifest.xml配置
```xml
<application
    ...
    android:usesCleartextTraffic="true"
    android:networkSecurityConfig="@xml/network_security_config">
```

## 常见问题

### Q1: 连接超时 (ConnectException)
**原因**：
- 使用了错误的IP地址
- 后端服务未启动
- 防火墙阻止了连接

**解决**：
1. 确认后端服务正在运行：`curl http://localhost:8080/api/getPics`
2. 检查防火墙设置
3. 确认IP地址正确

### Q2: 连接被拒绝 (Connection refused)
**原因**：
- 端口号错误
- 后端服务未监听正确的端口

**解决**：
1. 确认后端监听的端口号
2. 检查后端是否监听 `0.0.0.0` 而不是 `127.0.0.1`

### Q3: 真实设备无法连接
**原因**：
- 设备和开发机器不在同一网络
- 防火墙阻止了局域网访问

**解决**：
1. 确保设备和开发机器连接到同一WiFi
2. 临时关闭防火墙测试
3. 添加防火墙规则允许8080端口

## 动态配置服务器地址

应用支持在运行时修改服务器地址（未来功能）：

```kotlin
// 在设置界面
viewModel.updateServerUrl("http://192.168.1.100:8080")

// PreferencesManager
suspend fun setServerUrl(url: String) {
    dataStore.edit { preferences ->
        preferences[PreferencesKeys.SERVER_URL] = url
    }
}
```

## 测试建议

### 1. 模拟器测试
```kotlin
// 使用默认配置
serverUrl = "http://10.0.2.2:8080"
```

### 2. 真实设备测试
```kotlin
// 在应用启动时或设置界面修改
serverUrl = "http://192.168.1.100:8080"
```

### 3. 生产环境测试
```kotlin
// 使用HTTPS
serverUrl = "https://your-domain.com"
```

## 调试技巧

### 1. 启用详细日志
OkHttp日志拦截器已配置为 `BODY` 级别，可以查看完整的请求和响应：

```
D/OkHttp: --> POST http://10.0.2.2:8080/api/checkPassword
D/OkHttp: Content-Type: application/json; charset=utf-8
D/OkHttp: {"jsonrpc":"2.0","method":"checkPassword",...}
D/OkHttp: <-- 200 OK (123ms)
```

### 2. 使用ADB端口转发（备选方案）
```bash
# 将设备的8080端口转发到开发机器的8080端口
adb reverse tcp:8080 tcp:8080

# 然后可以在应用中使用
serverUrl = "http://localhost:8080"
```

### 3. 使用Charles/Fiddler抓包
配置代理服务器，查看实际的网络请求。

## 相关文件

- `PreferencesManager.kt` - 服务器地址配置
- `NetworkModule.kt` - 网络层配置
- `network_security_config.xml` - 网络安全配置
- `AndroidManifest.xml` - 应用清单配置

## 参考资料

- [Android Network Security Configuration](https://developer.android.com/training/articles/security-config)
- [Android Emulator Networking](https://developer.android.com/studio/run/emulator-networking)
- [OkHttp Documentation](https://square.github.io/okhttp/)
