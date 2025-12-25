# MPM Android 应用 - 快速开始指南

## 📱 应用功能

MPM (My Photo Manager) 是一个功能完整的照片管理Android应用，目前已实现：

### ✅ 已实现功能
- **用户认证**：登录、自动登录、登出
- **启动检查**：自动检测登录状态并导航
- **401处理**：认证失效自动清除并返回登录页

### 🚧 开发中功能
- 照片列表浏览
- 照片详情查看
- 照片编辑和管理
- 更多功能...

---

## 🚀 快速开始

### 1. 环境要求

- **Android Studio**: Hedgehog | 2023.1.1 或更高版本
- **JDK**: 17
- **Android SDK**: 34
- **最低支持设备**: Android 8.0 (API 26)

### 2. 克隆项目

```bash
git clone <repository-url>
cd mpm-go/apps
```

### 3. 打开项目

1. 启动 Android Studio
2. 选择 "Open an Existing Project"
3. 选择 `mpm-go/apps` 目录
4. 等待 Gradle 同步完成

### 4. 运行应用

1. 连接 Android 设备或启动模拟器
2. 点击 Run 按钮 (或按 Shift+F10)
3. 等待应用安装并启动

---

## 🔧 配置后端服务器

### 方式1：使用本地服务器

如果你有本地运行的MPM后端服务：

1. 确保后端服务正在运行（默认端口：8080）
2. 启动应用
3. 在登录页输入服务器地址：
   - 模拟器访问本机：`http://10.0.2.2:8080`
   - 真机访问局域网：`http://192.168.x.x:8080`

### 方式2：使用远程服务器

如果你有远程部署的MPM后端服务：

1. 启动应用
2. 在登录页输入服务器地址：`http://your-server.com:8080`

---

## 📝 测试登录功能

### 测试步骤

1. **首次启动**
   - 应用显示启动画面（SplashScreen）
   - 自动检测未登录，跳转到登录页

2. **输入服务器信息**
   - 服务器地址：`http://your-server:8080`
   - 账号：你的账号
   - 密码：你的密码

3. **登录**
   - 点击"登录"按钮
   - 等待验证（显示加载动画）
   - 登录成功后自动跳转到主页

4. **主页**
   - 显示欢迎信息
   - 可以点击右上角图标或底部按钮登出

5. **登出**
   - 点击登出按钮
   - 自动清除认证信息
   - 返回登录页

6. **自动登录**
   - 关闭应用
   - 重新打开应用
   - 应用自动检测到已登录
   - 直接跳转到主页（无需重新登录）

---

## 🐛 常见问题

### 1. 无法连接到服务器

**问题**：登录时提示"无法连接到服务器"

**解决方案**：
- 检查服务器地址是否正确
- 确保后端服务正在运行
- 检查网络连接
- 如果使用模拟器，确保使用 `10.0.2.2` 而不是 `localhost`
- 检查防火墙设置

### 2. 登录失败

**问题**：提示"登录失败"或"账号密码错误"

**解决方案**：
- 检查账号和密码是否正确
- 确认后端API正常工作
- 查看Android Studio的Logcat日志

### 3. 应用闪退

**问题**：应用启动后立即闪退

**解决方案**：
- 查看Android Studio的Logcat日志
- 确保所有依赖都已正确同步
- 尝试 Clean Project 后重新构建
- 检查是否有编译错误

### 4. Gradle同步失败

**问题**：打开项目后Gradle同步失败

**解决方案**：
- 检查网络连接
- 尝试使用VPN（如果在国内）
- 清除Gradle缓存：`./gradlew clean`
- 删除 `.gradle` 目录后重新同步

---

## 📂 项目结构

```
apps/
├── app/                                    # 主应用模块
│   └── src/main/java/com/simon/mpm/
│       ├── MpmApplication.kt              # Application类
│       ├── MainActivity.kt                # 主Activity
│       ├── feature/                       # 功能模块
│       │   ├── auth/                      # 认证功能
│       │   │   ├── LoginScreen.kt        # 登录页面
│       │   │   └── LoginViewModel.kt     # 登录ViewModel
│       │   ├── splash/                    # 启动页
│       │   │   ├── SplashScreen.kt       # 启动页面
│       │   │   └── SplashViewModel.kt    # 启动ViewModel
│       │   └── home/                      # 主页
│       │       ├── HomeScreen.kt         # 主页面
│       │       └── HomeViewModel.kt      # 主页ViewModel
│       ├── navigation/                    # 导航
│       │   ├── Routes.kt                 # 路由定义
│       │   └── MpmNavGraph.kt            # 导航图
│       └── ui/theme/                      # 主题
│
├── core-common/                           # 通用模块
│   └── src/main/java/com/simon/mpm/core/common/
│       ├── Result.kt                     # 结果封装
│       ├── Constants.kt                  # 常量
│       └── Extensions.kt                 # 扩展函数
│
├── core-network/                          # 网络层
│   └── src/main/java/com/simon/mpm/core/network/
│       ├── api/MpmApiService.kt          # API接口
│       ├── model/ApiResponse.kt          # 响应模型
│       ├── interceptor/                  # 拦截器
│       │   ├── AuthInterceptor.kt       # 认证拦截器
│       │   └── UnauthorizedInterceptor.kt # 401拦截器
│       ├── util/NetworkErrorHandler.kt   # 错误处理
│       └── di/NetworkModule.kt           # 依赖注入
│
└── core-data/                             # 数据层
    └── src/main/java/com/simon/mpm/core/data/
        ├── datastore/PreferencesManager.kt # 配置管理
        ├── repository/                    # Repository
        │   ├── BaseRepository.kt         # 基类
        │   └── AuthRepository.kt         # 认证Repository
        └── di/DataModule.kt              # 依赖注入
```

---

## 🔍 调试技巧

### 查看网络请求

1. 打开 Android Studio 的 Logcat
2. 过滤标签：`OkHttp`
3. 可以看到所有的网络请求和响应

### 查看应用日志

1. 打开 Android Studio 的 Logcat
2. 过滤包名：`com.simon.mpm`
3. 可以看到应用的所有日志

### 调试登录流程

在以下位置设置断点：
- `LoginViewModel.login()` - 登录逻辑
- `AuthRepository.login()` - API调用
- `SplashViewModel.checkLoginStatus()` - 启动检查

---

## 📚 相关文档

- [README.md](README.md) - 项目说明
- [PROGRESS.md](PROGRESS.md) - 开发进度
- [API文档.md](../API文档.md) - 后端API文档

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 📄 许可证

[待定]

---

*最后更新: 2025-12-19*