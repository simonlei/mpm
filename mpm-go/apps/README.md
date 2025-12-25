# MPM Android 应用

MPM (My Photo Manager) 是一个功能完善的照片管理Android应用，提供照片浏览、上传、人脸识别、地理位置、活动管理等核心功能。

## 技术栈

- **开发语言**: Kotlin
- **最低支持版本**: Android 8.0 (API 26)
- **目标版本**: Android 14 (API 34)
- **架构模式**: MVVM + Repository
- **UI框架**: Jetpack Compose + Material Design 3
- **依赖注入**: Hilt
- **网络框架**: Retrofit + OkHttp
- **图片加载**: Coil
- **本地存储**: Room + DataStore
- **异步处理**: Kotlin Coroutines + Flow

## 项目结构

```
apps/
├── app/                          # 主应用模块
│   └── src/main/java/com/simon/mpm/
│       ├── MpmApplication.kt     # Application类
│       ├── MainActivity.kt       # 主Activity
│       └── ui/theme/             # 主题配置
│
├── core-common/                  # 通用模块
│   └── src/main/java/com/simon/mpm/core/common/
│       ├── Result.kt             # 通用Result类
│       ├── Constants.kt          # 应用常量
│       └── Extensions.kt         # 扩展函数
│
├── core-network/                 # 网络层模块
│   └── src/main/java/com/simon/mpm/core/network/
│       ├── api/                  # API接口定义
│       ├── model/                # 网络数据模型
│       ├── interceptor/          # 网络拦截器
│       └── di/                   # 网络层依赖注入
│
├── core-data/                    # 数据层模块
│   └── src/main/java/com/simon/mpm/core/data/
│       ├── repository/           # Repository实现
│       ├── local/                # 本地数据源
│       ├── datastore/            # DataStore配置
│       └── di/                   # 数据层依赖注入
│
└── core-ui/                      # UI组件模块
    └── src/main/java/com/simon/mpm/core/ui/
        ├── components/           # 通用UI组件
        ├── theme/                # 主题配置
        └── utils/                # UI工具类
```

## 模块说明

### app
主应用模块，包含Application类、MainActivity和应用级配置。

### core-common
通用模块，提供跨模块共享的工具类、常量和扩展函数。

### core-network
网络层模块，封装所有网络请求相关的代码，包括：
- Retrofit配置
- API接口定义
- 网络拦截器（认证、日志等）
- 网络数据模型

### core-data
数据层模块，实现Repository模式，管理数据源：
- Repository实现
- Room数据库
- DataStore配置
- 数据缓存策略

### core-ui
UI组件模块，提供可复用的Compose组件和主题配置：
- 通用UI组件
- Material Design 3主题
- UI工具函数

## 构建项目

### 前置要求
- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17
- Android SDK 34

### 构建步骤

1. 克隆项目
```bash
git clone <repository-url>
cd mpm-go/apps
```

2. 打开Android Studio，选择 "Open an Existing Project"，选择 `apps` 目录

3. 等待Gradle同步完成

4. 运行应用
   - 点击 Run 按钮或按 Shift+F10
   - 选择目标设备（模拟器或真机）

## 开发规范

### 代码风格
- 遵循 Kotlin 官方编码规范
- 使用 ktlint 进行代码格式化
- 类名使用大驼峰命名法
- 函数和变量使用小驼峰命名法

### Git提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建/工具链相关

### 分支管理
- main: 主分支，保持稳定
- develop: 开发分支
- feature/*: 功能分支
- bugfix/*: 修复分支

## 配置说明

### 服务器地址配置
应用首次启动时需要配置后端服务器地址，可以在设置页面修改。

### 网络权限
应用需要以下权限：
- INTERNET: 网络访问
- ACCESS_NETWORK_STATE: 网络状态检测

## 许可证

[待定]

## 联系方式

[待定]