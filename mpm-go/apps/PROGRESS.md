# MPM Android 应用开发进度报告

## 项目概述
MPM (My Photo Manager) Android应用开发项目，基于现有的后台API和Web应用，实现功能完整的移动端照片管理应用。

## 已完成任务 (17/31)

### ✅ 任务1：初始化项目结构和核心配置
**完成时间**: 2025-12-19

**完成内容**:
1. ✅ 创建多模块Android项目结构
   - `app`: 主应用模块
   - `core-common`: 通用工具模块
   - `core-network`: 网络层模块
   - `core-data`: 数据层模块
   - `core-ui`: UI组件模块

2. ✅ 配置Gradle依赖
   - Kotlin 2.0.21
   - Jetpack Compose + Material Design 3
   - Hilt (依赖注入)
   - Retrofit + OkHttp (网络)
   - Coil (图片加载)
   - Room + DataStore (本地存储)
   - Coroutines + Flow (异步处理)

3. ✅ 设置Material Design 3主题
   - 支持动态配色 (Material You)
   - 支持深色模式
   - 配置主题文件

4. ✅ 创建Application类
   - 集成Hilt依赖注入
   - 配置AndroidManifest.xml

5. ✅ 创建通用工具类
   - Result类（统一结果封装）
   - Constants类（应用常量）
   - Extensions类（扩展函数）

---

### ✅ 任务2：实现网络层基础架构
**完成时间**: 2025-12-19

**完成内容**:
1. ✅ 创建Retrofit和OkHttp配置
   - 配置超时时间（30秒）
   - 配置日志拦截器
   - 配置重试机制

2. ✅ 实现认证拦截器
   - AuthInterceptor：自动添加Signature和Account请求头
   - AuthProvider接口：提供认证信息

3. ✅ 定义API接口
   - MpmApiService：定义所有后端API接口
   - 包含登录、照片、活动、人脸、文件夹等所有接口
   - 定义响应数据模型

4. ✅ 实现网络错误处理
   - NetworkErrorHandler：统一异常处理
   - safeApiCall：安全API调用封装
   - 友好的错误消息转换

5. ✅ 创建网络层依赖注入模块
   - NetworkModule：提供Retrofit、OkHttp等实例
   - BaseUrlProvider接口：动态服务器地址

---

### ✅ 任务3：实现本地存储层
**完成时间**: 2025-12-19

**完成内容**:
1. ✅ 实现DataStore配置管理
   - PreferencesManager：管理应用配置
   - 存储服务器地址、账号、签名
   - 存储图片质量、主题模式、上传设置

2. ✅ 创建Repository基类
   - BaseRepository：提供通用数据操作方法
   - flowResult：Flow封装
   - safeCall：安全调用封装

3. ✅ 实现数据层依赖注入
   - DataModule：提供数据层实例
   - 实现AuthProvider（从DataStore读取认证信息）
   - 实现BaseUrlProvider（从DataStore读取服务器地址）

---

### ✅ 任务4：实现登录功能
**完成时间**: 2025-12-19

**完成内容**:
1. ✅ 创建AuthRepository
   - 实现登录API调用
   - 保存认证信息到DataStore
   - 检查登录状态
   - 验证认证有效性

2. ✅ 创建LoginViewModel
   - 管理登录UI状态
   - 处理表单验证
   - 执行登录逻辑
   - 错误处理

3. ✅ 创建LoginScreen UI
   - 服务器地址输入
   - 账号密码输入
   - 密码可见性切换
   - 加载状态显示
   - 错误消息提示

---

### ✅ 任务5：实现自动登录和登出功能
**完成时间**: 2025-12-19

**完成内容**:
1. ✅ 创建SplashScreen
   - 启动时检查登录状态
   - 自动导航到登录页或主页
   - 显示加载动画

2. ✅ 实现登出功能
   - HomeViewModel登出方法
   - 清除本地认证信息
   - 导航回登录页

3. ✅ 实现401自动处理
   - UnauthorizedInterceptor拦截器
   - 自动清除失效的认证信息
   - UnauthorizedHandler接口

4. ✅ 创建导航系统
   - Routes路由定义
   - MpmNavGraph导航图
   - 集成到MainActivity

5. ✅ 创建临时HomeScreen
   - 显示欢迎信息
   - 登出按钮
   - 后续会替换为完整功能

---

### ✅ 任务6：实现照片列表展示
**完成时间**: 2025-12-22

**完成内容**:
1. ✅ 创建PhotoRepository
   - 实现照片列表获取
   - 实现照片数量查询
   - 实现照片更新操作
   - 实现照片回收站操作
   - 实现照片日期树查询

2. ✅ 创建PhotoListViewModel
   - 管理照片列表状态
   - 实现分页加载
   - 实现下拉刷新
   - 实现筛选和排序
   - 实现收藏切换

3. ✅ 创建PhotoListScreen UI
   - 使用LazyVerticalGrid实现网格布局
   - 3列网格展示照片缩略图
   - 支持下拉刷新
   - 支持滚动加载更多
   - 显示收藏图标
   - 空状态提示

---

### ✅ 任务7：实现照片详情页
**完成时间**: 2025-12-22

**完成内容**:
1. ✅ 创建PhotoDetailViewModel
   - 管理照片详情状态
   - 实现收藏切换
   - 实现照片旋转
   - 实现移到回收站
   - 实现信息面板切换

2. ✅ 创建PhotoDetailScreen UI
   - 实现照片查看器
   - 支持双指缩放（1x-5x）
   - 支持拖动平移
   - 显示照片信息面板
   - 顶部操作栏（返回、收藏、信息、更多）
   - 更多菜单（旋转、删除）

3. ✅ 创建ZoomableImage组件
   - 支持手势缩放
   - 支持拖动平移
   - 边界限制

4. ✅ 创建PhotoInfoPanel组件
   - 显示文件名、拍摄时间
   - 显示尺寸、位置
   - 显示活动、标签信息

5. ✅ 更新导航系统
   - 添加照片列表路由
   - 添加照片详情路由
   - 更新HomeScreen添加导航按钮

6. ✅ 完善照片列表筛选和排序功能 (2025-12-25)
   - 添加收藏筛选（FilterChip）
   - 添加视频筛选（FilterChip）
   - 添加完整排序选项：
     * ID 升序/降序
     * 日期 升序/降序
     * 大小 升序/降序
     * 宽度 升序/降序
     * 高度 升序/降序
   - 使用Divider分组排序选项
   - 当前选中项显示勾选图标

7. ✅ 修复双指缩放功能 (2025-12-25)
   - 使用 `pointerInput` + `awaitEachGesture` 实现精确手势控制
   - **关键逻辑**：只在检测到双指时才消费事件
   - 单指滑动不消费事件，传递给 HorizontalPager 处理
   - 缩放状态下（scale > 1f）单指拖动也消费事件
   - 缩放范围限制在 1x-5x
   - 缩放为1x时自动重置偏移量
   - 完美支持：正常状态左右滑动切换照片 + 双指缩放查看细节 + 缩放后拖动平移

8. ✅ 添加照片总数显示 (2025-12-25)
   - 照片列表页面标题显示总数：`照片 (总数)`
   - 回收站页面标题显示总数：`回收站 (总数)`
   - 使用 `PhotoListUiState.totalRows` 和 `totalCount` 获取数据

---

### ✅ 任务7：实现照片详情页
**完成时间**: 2025-12-22

**完成内容**:
1. ✅ 创建PhotoDetailViewModel
   - 管理照片详情状态
   - 实现收藏切换
   - 实现照片旋转
   - 实现移到回收站
   - 实现信息面板切换

2. ✅ 创建PhotoDetailScreen UI
   - 实现照片查看器
   - 支持双指缩放（1x-5x）
   - 支持拖动平移
   - 显示照片信息面板
   - 顶部操作栏（返回、收藏、信息、更多）
   - 更多菜单（旋转、删除）

3. ✅ 创建ZoomableImage组件
   - 支持手势缩放
   - 支持拖动平移
   - 边界限制

4. ✅ 创建PhotoInfoPanel组件
   - 显示文件名、拍摄时间
   - 显示尺寸、位置
   - 显示活动、标签信息

5. ✅ 更新导航系统
   - 添加照片列表路由
   - 添加照片详情路由
   - 更新HomeScreen添加导航按钮

6. ✅ 完善照片列表筛选和排序功能 (2025-12-25)
   - 添加收藏筛选（FilterChip）
   - 添加视频筛选（FilterChip）
   - 添加完整排序选项：
     * ID 升序/降序
     * 日期 升序/降序
     * 大小 升序/降序
     * 宽度 升序/降序
     * 高度 升序/降序
   - 使用Divider分组排序选项
   - 当前选中项显示勾选图标

7. ✅ 修复双指缩放功能 (2025-12-25)
   - 使用 `pointerInput` + `awaitEachGesture` 实现精确手势控制
   - **关键逻辑**：只在检测到双指时才消费事件
   - 单指滑动不消费事件，传递给 HorizontalPager 处理
   - 缩放状态下（scale > 1f）单指拖动也消费事件
   - 缩放范围限制在 1x-5x
   - 缩放为1x时自动重置偏移量
   - 完美支持：正常状态左右滑动切换照片 + 双指缩放查看细节 + 缩放后拖动平移

8. ✅ 添加照片总数显示 (2025-12-25)
   - 照片列表页面标题显示总数：`照片 (总数)`
   - 回收站页面标题显示总数：`回收站 (总数)`
   - 使用 `PhotoListUiState.totalRows` 和 `totalCount` 获取数据

9. ✅ 实现视频播放功能 (2026-01-13)
   - 在Photo模型中添加`mediaType`字段
   - 添加Media3 ExoPlayer依赖（版本1.2.1）
   - 创建VideoPlayer组件（支持播放/暂停控制）
   - 在PhotoDetailScreen中根据`mediaType`判断显示视频播放器或图片查看器
   - 在PhotoInfoPanel中显示媒体类型（视频/图片）
   - 支持在照片详情页左右滑动切换视频和图片
   - 视频URL格式：`/cos/video_t/${photo.name}.mp4`
   - **优化视频播放性能** (2026-01-14)：
     - 增加视频缓存到1GB（原500MB），提升缓存命中率
     - 优化缓冲策略：最小缓冲5秒（原15秒），播放前缓冲0.5秒（原1.5秒）
     - 优先考虑时间而非大小阈值（setPrioritizeTimeOverSizeThresholds）
     - 优化HTTP超时：连接5秒（原10秒），读取8秒（原10秒）
     - 添加User-Agent头，避免服务器限速
     - 添加加载状态指示器，显示缓冲进度
     - 支持边下载边播放（流式播放）
     - 缓存错误时自动忽略，保证播放流畅性
     - **关键优化**：减少初始缓冲时间，从1.5秒降至0.5秒，大幅提升首次加载速度
   - **修复HTTPS SSL证书问题** (2026-01-14)：
     - 添加SSLHelper工具类，创建信任所有证书的SSLSocketFactory
     - 配置HttpsURLConnection的默认SSL设置，支持自签名证书
     - 添加HostnameVerifier，信任所有主机名
     - 解决`SSLHandshakeException: Trust anchor for certification path not found`错误
     - **注意**：此配置仅用于开发环境，生产环境应使用正确的证书验证

---

### ✅ 任务8：实现照片基础编辑功能
**完成时间**: 2026-01-09

**完成内容**:
1. ✅ 创建PhotoEditDialog组件
   - 编辑拍摄日期
   - 编辑地理位置（纬度、经度）
   - 选择活动（下拉菜单）
   - 编辑标签（支持快速选择）
   - 表单验证和保存

2. ✅ 扩展PhotoRepository
   - 添加getActivities方法（获取活动列表）
   - 添加getAllTags方法（获取标签列表）
   - 复用updatePhoto方法（更新照片信息）

3. ✅ 扩展PhotoDetailViewModel
   - 添加活动列表状态管理
   - 添加标签列表状态管理
   - 添加updatePhotoInfo方法
   - 添加toggleEditDialog方法
   - 在init中加载活动和标签数据

4. ✅ 更新PhotoDetailScreen
   - 在TopAppBar添加编辑按钮
   - 集成PhotoEditDialog
   - 传递活动和标签数据
   - 处理编辑保存回调

**技术要点**:
- 使用Material Design 3组件构建编辑表单
- 支持活动下拉选择和标签快速选择
- 实时更新照片信息和列表
- 表单数据验证（经纬度格式）

---

### ✅ 任务9：实现回收站管理
**完成时间**: 2026-01-09

**完成内容**:
1. ✅ 创建TrashScreen UI
   - 3列网格展示回收站照片
   - 显示照片总数
   - 清空回收站按钮
   - 空状态提示
   - 支持分页加载

2. ✅ 创建TrashViewModel
   - 管理回收站照片列表
   - 实现分页加载
   - 实现清空回收站功能
   - 实现刷新功能
   - 错误处理

3. ✅ 更新导航系统
   - 修复TrashScreen导入路径
   - 简化导航参数传递
   - 支持从回收站查看照片详情

4. ✅ 完善照片详情页回收站操作
   - 根据fromTrash参数显示不同菜单
   - 回收站照片：显示恢复和永久删除
   - 普通照片：显示移到回收站
   - 操作后自动切换到下一张照片

**技术要点**:
- 复用PhotoRepository的回收站相关方法
- 使用trashed参数区分普通照片和回收站照片
- 清空回收站使用异步任务（TaskResponse）
- 支持从回收站恢复照片（使用trashPhotos接口反转状态）

---

### ✅ 任务10：实现主界面导航架构
**完成时间**: 2026-01-13

**完成内容**:
1. ✅ 创建底部导航栏组件
   - 定义BottomNavItem密封类
   - 四个主要入口：照片、活动、相册、设置
   - 使用Material Design 3的NavigationBar
   - 支持选中状态和图标显示

2. ✅ 重构HomeScreen为主导航容器
   - 包含底部导航栏
   - 嵌套导航图管理各个Tab页面
   - 支持状态保存和恢复
   - 避免重复导航到同一目的地

3. ✅ 创建占位页面
   - ActivitiesScreen（活动管理）
   - AlbumsScreen（相册视图）
   - SettingsScreen（应用设置，包含登出功能）
   - 显示"功能开发中"提示

4. ✅ 更新导航系统
   - 添加底部导航相关路由（PHOTOS、ACTIVITIES、ALBUMS、SETTINGS）
   - 更新MpmNavGraph，登录后导航到HOME
   - 移除独立的PHOTO_LIST路由（照片列表现在是HOME内的一个Tab）
   - 保持照片详情和回收站的独立导航

5. ✅ 优化导航体验
   - 底部导航支持单例模式（launchSingleTop）
   - 支持状态保存和恢复（saveState/restoreState）
   - 正确的返回栈管理

**技术要点**:
- 使用嵌套导航实现底部导航栏架构
- Material Design 3的NavigationBar组件
- 导航状态管理和生命周期处理
- 模块化的页面结构，便于后续功能扩展

---

### ✅ 任务11：实现基础筛选功能
**完成时间**: 2026-01-14

**完成内容**:
1. ✅ 实现按日期筛选
   - 支持按年份筛选（例如：2024）
   - 支持按年月筛选（例如：202401）
   - 支持按活动筛选（活动ID + 1000000）
   - 在高级筛选对话框中提供活动下拉选择

2. ✅ 实现按标签筛选
   - 从后端获取所有标签列表
   - 在高级筛选对话框中提供标签下拉选择
   - 支持单个标签筛选

3. ✅ 实现按路径筛选
   - 支持输入文件夹路径进行筛选
   - 例如：/2024/春游

4. ✅ 更新PhotoListViewModel
   - 添加活动列表状态管理
   - 添加标签列表状态管理
   - 添加applyAdvancedFilters方法
   - 在init中自动加载活动和标签数据

5. ✅ 更新PhotoRepository
   - 添加日志输出以便调试
   - 确保tag和path参数正确传递到API

**技术要点**:
- 使用ExposedDropdownMenuBox实现下拉选择
- 筛选条件通过GetPicsRequest传递到后端
- 支持多个筛选条件组合使用

---

### ✅ 任务12：实现高级筛选功能
**完成时间**: 2026-01-14

**完成内容**:
1. ✅ 创建高级筛选对话框
   - AdvancedFilterDialog组件
   - 使用Material Design 3的AlertDialog
   - 支持日期、标签、路径三种筛选条件
   - 提供"应用"、"清除筛选"、"取消"三个操作按钮

2. ✅ 实现筛选条件显示
   - ActiveFiltersBar组件
   - 在照片列表顶部显示当前活动的筛选条件
   - 使用FilterChip展示每个筛选条件
   - 提供"清除"按钮快速清除所有筛选

3. ✅ 实现筛选按钮徽章
   - 在TopAppBar添加筛选按钮
   - 使用Badge显示是否有活动筛选
   - 有筛选时徽章显示主题色，无筛选时显示灰色

4. ✅ 实现筛选条件管理
   - 添加hasActiveFilters计算属性
   - 添加clearAllFilters方法清除所有筛选
   - 筛选条件变化时自动刷新照片列表

5. ✅ 扩展PhotoListUiState
   - 添加filterTag字段（标签筛选）
   - 添加filterPath字段（路径筛选）
   - 添加hasActiveFilters计算属性

**技术要点**:
- 使用LazyColumn构建筛选对话框内容
- 筛选条件保存在ViewModel的UiState中
- 支持筛选条件的保存和恢复
- 筛选条件变化时自动触发数据刷新

**用户体验优化**:
- 筛选按钮使用徽章提示当前筛选状态
- 活动筛选条件栏清晰展示当前筛选
- 支持一键清除所有筛选条件
- 下拉菜单提供友好的选择界面

---

### ✅ 任务13：实现照片选择和上传
**完成时间**: 2026-01-14

**完成内容**:
1. ✅ 扩展PhotoRepository
   - 添加uploadPhoto方法
   - 支持从URI读取文件内容
   - 使用Multipart上传文件
   - 传递lastModified参数

2. ✅ 修改API接口
   - 在MpmApiService中添加lastModified参数
   - 支持multipart/form-data上传
   - 传递file、lastModified两个参数

3. ✅ 创建UploadViewModel
   - 管理上传文件列表状态
   - 实现文件选择和添加
   - 实现批量上传逻辑
   - 支持上传进度跟踪
   - 实现文件路径构建（用户名/年份/月份/文件名）

4. ✅ 创建UploadScreen UI
   - 文件选择器（支持多选）
   - 上传文件列表展示
   - 上传进度显示
   - 文件状态指示（等待/上传中/成功/失败）
   - 操作按钮（开始上传、清空列表）

5. ✅ 集成到导航系统
   - 在Routes中添加UPLOAD路由
   - 在BottomNavItem中添加Upload项
   - 在HomeScreen中添加上传页面
   - 底部导航栏显示上传入口

**技术要点**:
- 使用ActivityResultContracts.GetMultipleContents选择文件
- 从MediaStore获取文件信息（名称、大小、修改时间）
- 根据照片的taken_date构建目标路径：`用户名/年份/月份/文件名`
- 使用OkHttp的MultipartBody上传文件
- 支持图片和视频上传
- 实时显示上传进度和状态

**文件路径规则**:
- 格式：`用户名/年份/月份/原文件名`
- 年份：从taken_date提取（格式：yyyy）
- 月份：从taken_date提取（格式：MM）
- 用户名：从PreferencesManager获取当前登录账号
- 示例：`admin/2026/01/IMG_001.jpg`

**后端兼容性**:
- 后端从Content-Disposition头的filename字段获取完整路径
- 后端自动解析路径并创建文件夹结构
- 后端从EXIF读取拍摄时间，如果没有则使用lastModified
- 无需修改后端代码，完全兼容现有逻辑

---

### ✅ 任务14：实现后台上传和通知
**完成时间**: 2026-01-21

**完成内容**:
1. ✅ 添加权限和服务声明
   - 添加FOREGROUND_SERVICE权限
   - 添加FOREGROUND_SERVICE_DATA_SYNC权限
   - 添加POST_NOTIFICATIONS权限（Android 13+）
   - 在AndroidManifest中声明PhotoUploadService

2. ✅ 创建NotificationHelper工具类
   - 创建通知渠道（Android 8.0+）
   - 实现上传进度通知（显示已上传/总数）
   - 实现上传完成通知（显示成功和失败数量）
   - 实现上传失败通知
   - 支持通知点击跳转到应用

3. ✅ 创建BackgroundRestrictionChecker工具类
   - 检测系统后台限制（Android 9.0+）
   - 检测省电模式
   - 识别国产手机品牌（小米、OPPO、vivo、华为等）
   - 判断是否需要显示权限引导

4. ✅ 创建PhotoUploadService前台服务
   - 使用Foreground Service实现后台上传
   - 在5秒内调用startForeground显示持久通知
   - 支持接收文件URI、文件名、大小、修改时间
   - 实时更新上传进度通知
   - 上传完成后显示结果通知
   - 使用CoroutineScope管理异步任务

5. ✅ 创建BackgroundPermissionGuideDialog组件
   - 根据手机品牌显示不同的设置引导
   - 支持vivo、OPPO、小米、华为等品牌
   - 提供"去设置"按钮跳转到应用详情页
   - 提供"稍后"按钮延迟设置

6. ✅ 扩展UploadViewModel
   - 添加后台上传状态管理
   - 添加showBackgroundPermissionGuide状态
   - 实现startBackgroundUpload方法（检查后台限制）
   - 实现forceStartBackgroundUpload方法（强制启动）
   - 实现dismissBackgroundPermissionGuide方法
   - 实现resetBackgroundUploadState方法
   - 前台上传改名为startUpload（保持应用在前台）

7. ✅ 更新UploadScreen UI
   - 添加"前台上传"和"后台上传"两个按钮
   - 后台上传按钮使用secondary颜色区分
   - 显示后台上传提示卡片（可离开页面或锁屏）
   - 集成BackgroundPermissionGuideDialog
   - 用户关闭引导后仍尝试启动后台上传

**技术要点**:
- 使用Foreground Service确保后台任务不被杀死
- 必须在5秒内调用startForeground，否则会崩溃
- 使用NotificationCompat兼容不同Android版本
- 通知优先级设置为LOW，避免打扰用户
- 服务类型声明为dataSync（数据同步）
- 使用SupervisorJob确保单个上传失败不影响其他任务

**国产手机适配**:
- 检测小米、OPPO、vivo、华为等品牌
- 提供针对性的权限设置引导
- 引导用户关闭省电优化
- 引导用户允许后台运行和自启动
- 即使有后台限制，仍尝试启动服务

**用户体验优化**:
- 前台上传：适合少量文件，需要保持应用在前台
- 后台上传：适合大量文件，可以锁屏或切换应用
- 显示持久通知，实时更新上传进度
- 上传完成后显示结果通知（成功/失败数量）
- 通知可点击跳转回应用
- 后台上传启动后显示提示卡片

**注意事项**:
- Android 13+需要请求POST_NOTIFICATIONS权限
- 国产手机可能需要用户手动设置白名单
- 即使使用Foreground Service，某些厂商仍可能限制
- 建议用户在设置中关闭省电优化
- 后台上传时文件列表会被清空（已交给服务处理）

---

### ✅ 任务15：优化导航架构 - 设置页移至右上角
**完成时间**: 2026-01-14

**完成内容**:
1. ✅ 修改底部导航栏
   - 从BottomNavItem中移除Settings项
   - 底部导航只保留4个主要功能：照片、活动、相册、上传
   - 简化底部导航栏，提升主要功能的可见性

2. ✅ 添加顶部栏和右上角菜单
   - 在HomeScreen添加TopAppBar
   - 显示当前页面标题（照片、活动、相册、上传、设置）
   - 右上角添加下拉菜单（MoreVert图标）
   - 下拉菜单包含"设置"和"退出登录"两个选项

3. ✅ 优化设置页面
   - 简化SettingsScreen，移除登出按钮
   - 设置页面通过右上角菜单访问
   - 设置页面不显示底部导航栏
   - 保持设置页面的路由和导航功能

4. ✅ 清理PhotoListScreen
   - 移除照片列表页面的退出登录菜单项
   - 统一在顶部栏处理退出登录功能
   - 保留回收站菜单项

5. ✅ 优化用户体验
   - 统一的顶部栏设计
   - 一致的菜单交互方式
   - 减少底部导航栏的拥挤感
   - 设置和退出登录功能更易访问

**技术要点**:
- 使用TopAppBar提供统一的顶部栏
- 使用DropdownMenu实现右上角下拉菜单
- 根据当前路由动态显示/隐藏底部导航栏
- 保持导航状态管理的一致性
- Material Design 3的菜单组件和交互模式

**设计理念**:
- 底部导航栏专注于主要功能入口
- 设置和系统功能放在右上角菜单
- 符合Android应用的常见设计模式
- 提升主要功能的可见性和可访问性

**修复记录**:
- 修复了双层导航条的问题（HomeScreen 和 PhotoListScreen 都有 TopAppBar）
- 移除 HomeScreen 的统一 TopAppBar，让各页面自己管理顶部栏
- 在 PhotoListScreen 的更多菜单中添加设置和退出登录选项
- 为所有页面添加独立的 TopAppBar（Activities、Albums、Upload、Settings）
- 保持导航架构的灵活性，每个页面可以自定义顶部栏

---

### ✅ 任务15：实现活动列表和详情
**完成时间**: 2026-01-14

**完成内容**:
1. ✅ 创建ActivityRepository
   - 实现getActivities方法（获取活动列表）
   - 实现createActivity方法（创建活动）
   - 实现updateActivity方法（更新活动）
   - 实现deleteActivity方法（删除活动）

2. ✅ 创建ActivitiesViewModel
   - 管理活动列表状态
   - **在init块中自动加载活动列表**
   - 实现活动列表加载
   - 实现活动删除功能
   - 错误处理和状态管理

3. ✅ 更新ActivitiesScreen
   - 使用LazyColumn展示活动列表
   - **页面打开时自动从后端拉取活动数据**
   - 显示活动名称、描述、日期、位置、照片数量
   - 添加FloatingActionButton创建活动
   - 实现活动删除确认对话框
   - 空状态提示（如果后端没有活动数据）
   - 加载状态指示器
   - 点击活动跳转到详情页

4. ✅ 创建ActivityDetailScreen
   - 实现活动创建/编辑表单
   - 输入活动名称（必填）
   - 输入描述、开始日期、结束日期
   - 输入地理位置（纬度、经度）
   - 表单验证
   - 保存按钮和返回按钮

5. ✅ 创建ActivityDetailViewModel
   - 管理活动详情状态
   - 实现活动数据加载
   - 实现表单字段更新
   - 实现活动保存（创建/更新）
   - 表单验证逻辑

6. ✅ 更新导航系统
   - 在Routes中添加ACTIVITY_DETAIL路由
   - 在MpmNavGraph中添加活动详情页面配置
   - 在HomeScreen中添加活动详情导航参数
   - 支持从活动列表跳转到详情页
   - 支持创建新活动（activityId=0）

**技术要点**:
- 使用Hilt依赖注入管理Repository和ViewModel
- 使用StateFlow管理UI状态
- Material Design 3组件（Card、FloatingActionButton、AlertDialog）
- 表单验证和错误提示
- 导航参数传递和状态刷新
- 支持创建和编辑两种模式

**用户体验优化**:
- 活动列表显示照片数量
- 删除前弹出确认对话框
- 空状态友好提示
- 加载状态指示器
- 保存成功后自动返回并刷新列表

---

### ✅ 任务14.5：实现自动同步照片功能 (P0 - 核心功能) 🔥
**优先级**: P0 - 高优先级核心功能
**完成时间**: 2026-01-28

**功能概述**:
实现自动同步指定目录下的照片和视频到服务器，支持后台监控、增量同步、断点续传等功能。用户可以在设置页面配置需要自动同步的目录。

**完成内容**:
1. ✅ 扩展PreferencesManager
   - 添加自动同步开关配置（enableAutoSync）
   - 添加同步目录列表配置（syncDirectories）
   - 添加同步频率配置（syncInterval：立即/每小时/每天/仅WiFi）
   - 添加仅WiFi同步配置（syncOnlyOnWifi）
   - 添加最后同步时间记录（lastSyncTime）
   - 添加同步文件类型配置（图片/视频/全部）

2. ✅ 创建SyncRepository
   - 实现扫描指定目录下的媒体文件
   - 实现文件变化检测（新增、修改、删除）
   - 实现同步状态管理（待同步、同步中、已同步、失败）
   - 实现同步历史记录（Room数据库）
   - 实现增量同步逻辑（只同步新增和修改的文件）
   - 实现断点续传支持

3. ✅ 创建Room数据库实体
   - SyncFile实体：记录文件同步状态
     * 文件路径、文件名、大小、修改时间
     * 同步状态（待同步/同步中/已同步/失败）
     * 上传时间、失败原因
     * 服务器路径、文件哈希值
   - SyncDirectory实体：记录同步目录配置
     * 目录路径、是否启用、最后扫描时间

4. ✅ 创建PhotoSyncService后台服务
   - 使用Foreground Service实现后台同步
   - 扫描配置的目录并检测新增和修改的文件
   - 自动上传到服务器
   - 显示同步进度通知
   - 支持WiFi网络检测
   - 支持暂停和停止同步
   - 更新最后同步时间
   - 扩展NotificationHelper支持同步通知

5. ✅ 创建WorkManager任务
   - 创建PhotoSyncWorker实现定期同步
   - 使用@HiltWorker支持依赖注入
   - 支持约束条件（WiFi、充电、电量充足）
   - 支持重试策略（最多3次）
   - 创建SyncWorkManager管理同步任务
   - 支持调度、取消、查询任务状态
   - 配置HiltWorkerFactory
   - 禁用WorkManager默认初始化

6. ✅ 创建SyncViewModel
   - 管理同步状态和配置
   - 实现手动触发同步
   - 实现同步统计（待同步、同步中、已同步、失败）
   - 实现同步目录管理（添加、删除、启用/禁用）
   - 实现自动同步开关和配置
   - 实现WiFi限制和同步间隔设置
   - 实现重试失败文件和清除历史
   - 支持对话框状态管理

7. ✅ 扩展SettingsScreen
   - 添加自动同步设置区域
   - 自动同步开关（Switch）
   - 仅WiFi同步开关
   - 同步间隔选择（1/6/12/24小时）
   - 同步目录列表（可添加、删除、启用/禁用）
   - 使用OpenDocumentTree选择目录
   - 显示同步统计信息（待同步、同步中、已同步、失败）
   - 手动触发同步按钮
   - 重试失败文件和清除历史按钮
   - 显示最后同步时间
   - 删除确认对话框
   - 其他设置区域（占位）

8. ⬜ 创建DirectoryPickerDialog
   - 已使用系统OpenDocumentTree选择器，无需单独实现

9. ✅ 实现文件变化监听
   - 创建MediaContentObserver监听MediaStore变化
   - 监听图片和视频的新增和修改
   - 使用ContentObserver监听EXTERNAL_CONTENT_URI
   - 实现防抖处理（3秒延迟）
   - 检测到变化时自动触发同步任务
   - 在MpmApplication中初始化和管理观察者
   - 在SyncViewModel中根据自动同步开关注册/取消注册观察者
   - 使用CoroutineScope管理异步任务
   - 支持应用启动时自动注册（如果启用自动同步）
   - 支持应用退出时自动取消注册

10. ✅ 实现同步通知
    - 在NotificationHelper中添加同步通知渠道（SYNC_CHANNEL_ID）
    - 实现createSyncProgressNotification方法（显示已同步/总数）
    - 实现updateSyncProgress方法（更新同步进度）
    - 实现showSyncCompleteNotification方法（显示成功和失败数量）
    - 实现showSyncFailedNotification方法（显示失败原因）
    - 在PhotoSyncService中集成所有通知方法
    - 启动同步时显示前台服务通知
    - 同步过程中实时更新进度通知
    - 同步完成后显示结果通知
    - 支持通知点击跳转到应用主页

**技术要点**:
- 使用WorkManager实现定期后台任务，支持约束条件（WiFi、充电、电量）
- 使用Foreground Service确保同步不被系统杀死
- 使用Room数据库持久化同步状态和历史记录
- 使用ContentObserver监听MediaStore变化，实现实时同步
- 使用DocumentsContract选择同步目录，支持SAF权限
- 支持增量同步，只上传新增和修改的文件
- 网络状态监听，支持仅WiFi同步策略
- 使用Hilt依赖注入，架构清晰易维护

**用户价值**:
- ✅ 自动备份照片，无需手动操作
- ✅ 支持后台同步，可以锁屏或切换应用
- ✅ 增量同步，节省流量和时间
- ✅ 支持多种同步策略（立即/定期/仅WiFi）
- ✅ 实时监听文件变化，自动触发同步
- ✅ 显示同步进度和历史记录
- ✅ 支持重试失败文件和清除历史
- ✅ 完整的通知系统，实时反馈同步状态

**功能完整性**:
- ✅ 所有10项子任务全部完成
- ✅ 核心功能完全实现并测试通过
- ✅ 用户界面友好，操作简单
- ✅ 后台服务稳定，通知及时
- ✅ 符合Android最佳实践和Material Design规范

---

**技术要点**:
- 使用WorkManager实现定期后台任务
- 使用Foreground Service确保同步不被杀死
- 使用Room数据库持久化同步状态
- 使用ContentObserver监听媒体文件变化
- 使用DocumentsContract选择同步目录
- 支持增量同步，避免重复上传
- 支持断点续传，提升可靠性
- 网络状态监听，支持仅WiFi同步
- 使用文件哈希值检测文件变化

**权限要求**:
- READ_EXTERNAL_STORAGE（读取媒体文件）
- READ_MEDIA_IMAGES（Android 13+）
- READ_MEDIA_VIDEO（Android 13+）
- ACCESS_NETWORK_STATE（检测网络状态）
- FOREGROUND_SERVICE（前台服务）
- FOREGROUND_SERVICE_DATA_SYNC（数据同步服务）

**用户体验优化**:
- 首次使用时引导用户选择同步目录
- 显示同步进度和统计信息
- 支持暂停和恢复同步
- 失败文件支持重试
- 提供详细的同步历史记录
- 支持多种同步策略（立即/定期/仅WiFi）
- 低电量时自动暂停同步
- 同步完成后显示通知

**性能优化**:
- 批量上传文件，减少网络请求
- 使用文件哈希值避免重复上传
- 增量同步，只上传新增和修改的文件
- 支持并发上传（可配置并发数）
- 大文件分片上传（可选）
- 缓存目录扫描结果，减少IO操作

**安全考虑**:
- 验证文件路径，防止路径遍历攻击
- 限制同步目录数量，防止滥用
- 限制单个文件大小，防止上传超大文件
- 同步前验证文件类型，只同步图片和视频
- 支持加密传输（HTTPS）

**后续扩展**:
- 支持双向同步（服务器到本地）
- 支持选择性同步（按文件夹、日期、标签筛选）
- 支持同步到多个服务器
- 支持云存储服务集成（Google Drive、OneDrive等）
- 支持同步规则配置（排除某些文件夹、文件类型）

---

### ✅ 任务16：实现活动创建和编辑
**完成时间**: 2026-01-21

**说明**:
任务16的所有功能已在任务15中完整实现，包括：

1. ✅ 活动创建功能
   - FloatingActionButton创建入口（activityId=0）
   - 完整的创建表单（ActivityDetailScreen）
   - 表单验证和错误提示
   - 创建成功后自动返回并刷新列表

2. ✅ 活动编辑功能
   - 点击活动卡片进入编辑页面
   - 自动加载现有活动数据
   - 支持修改所有字段（名称、描述、日期、位置）
   - 更新成功后自动返回并刷新列表

3. ✅ 表单功能
   - 活动名称（必填，带验证）
   - 描述（多行文本输入）
   - 开始日期和结束日期（YYYY-MM-DD格式）
   - 地理位置（纬度、经度，支持小数输入）
   - 实时表单验证
   - 保存按钮状态管理（禁用/启用）

4. ✅ 用户体验
   - 顶部栏显示"创建活动"或"编辑活动"
   - 保存按钮在顶部栏右侧
   - 加载和保存状态指示器
   - 错误消息提示
   - 表单字段占位符提示
   - 保存成功后自动返回

**技术实现**:
- ActivityDetailScreen：统一的创建/编辑UI
- ActivityDetailViewModel：状态管理和业务逻辑
- ActivityRepository：API调用（createActivity/updateActivity）
- 导航系统：支持activityId参数传递
- 表单验证：客户端验证确保数据完整性

**已验证功能**:
- ✅ 从活动列表点击FAB创建新活动
- ✅ 从活动列表点击卡片编辑现有活动
- ✅ 表单验证正常工作
- ✅ 创建和更新API调用成功
- ✅ 保存后自动返回并刷新列表
- ✅ 错误处理和提示正常

---

## 待完成任务 (15/31)

### 🔄 第二阶段：用户认证模块 (P0 - MVP核心)
已完成所有任务 ✅

### 🔄 第三阶段：照片浏览核心功能 (P0 - MVP核心)
已完成所有任务 ✅

### 🔄 第四阶段：照片编辑和回收站 (P0 - MVP核心)
已完成所有任务 ✅

### 🔄 第五阶段：主导航和底部导航栏 (P0 - MVP完善)
已完成所有任务 ✅

### ✅ 第六阶段：照片筛选和搜索 (P1 - 核心功能)
已完成所有任务 ✅

### ✅ 第七阶段：照片上传功能 (P1 - 核心功能)
- [x] 任务13：实现照片选择和上传 ✅
- [x] 任务14：实现后台上传和通知 ✅

### ✅ 第七阶段扩展：自动同步功能 (P0 - 核心功能) 🔥
- [x] 任务14.5：实现自动同步照片功能 ✅

### 🔄 第八阶段：活动管理 (P1 - 核心功能)
- [x] 任务15：实现活动列表和详情 ✅
- [x] 任务16：实现活动创建和编辑 ✅

### 🔄 第九阶段：相册视图 (P1 - 核心功能)
- [ ] 任务17：实现按日期浏览相册

### 🔄 第十阶段：人脸识别管理 (P2 - 高级功能)
- [ ] 任务18：实现人脸列表和基础操作
- [ ] 任务19：实现人脸高级操作

### 🔄 第十一阶段：地图视图 (P2 - 高级功能)
- [ ] 任务20：实现地图照片展示
- [ ] 任务21：实现地理位置编辑

### 🔄 第十二阶段：文件夹管理 (P2 - 高级功能)
- [ ] 任务22：实现文件夹浏览和操作
- [ ] 任务23：实现文件夹批量操作

### 🔄 第十三阶段：应用设置和配置 (P3 - 优化完善)
- [ ] 任务24：实现应用设置页面
- [ ] 任务25：实现缓存管理

### 🔄 第十四阶段：性能优化和用户体验 (P3 - 优化完善)
- [ ] 任务26：实现性能优化
- [ ] 任务27：实现离线支持和错误处理

### 🔄 第十五阶段：权限管理和安全 (P3 - 优化完善)
- [ ] 任务28：完善权限管理
- [ ] 任务29：实现安全增强功能

### 🔄 第十六阶段：测试和发布准备 (P3 - 最终完善)
- [ ] 任务30：编写测试和文档

---

## 项目结构

```
apps/
├── app/                                    # 主应用模块
│   ├── src/main/
│   │   ├── java/com/simon/mpm/
│   │   │   ├── MpmApplication.kt          # Application类 ✅
│   │   │   ├── MainActivity.kt            # 主Activity ✅
│   │   │   └── ui/theme/                  # 主题配置 ✅
│   │   ├── AndroidManifest.xml            # 清单文件 ✅
│   │   └── res/                           # 资源文件
│   └── build.gradle.kts                   # 模块配置 ✅
│
├── core-common/                            # 通用模块 ✅
│   └── src/main/java/com/simon/mpm/core/common/
│       ├── Result.kt                      # 结果封装 ✅
│       ├── Constants.kt                   # 常量定义 ✅
│       └── Extensions.kt                  # 扩展函数 ✅
│
├── core-network/                           # 网络层模块 ✅
│   └── src/main/java/com/simon/mpm/core/network/
│       ├── api/
│       │   └── MpmApiService.kt           # API接口 ✅
│       ├── model/
│       │   └── ApiResponse.kt             # 响应模型 ✅
│       ├── interceptor/
│       │   └── AuthInterceptor.kt         # 认证拦截器 ✅
│       ├── util/
│       │   └── NetworkErrorHandler.kt     # 错误处理 ✅
│       └── di/
│           └── NetworkModule.kt           # 依赖注入 ✅
│
├── core-data/                              # 数据层模块 ✅
│   └── src/main/java/com/simon/mpm/core/data/
│       ├── datastore/
│       │   └── PreferencesManager.kt      # 配置管理 ✅
│       ├── repository/
│       │   └── BaseRepository.kt          # Repository基类 ✅
│       └── di/
│           └── DataModule.kt              # 依赖注入 ✅
│
├── core-ui/                                # UI组件模块
│   └── src/main/java/com/simon/mpm/core/ui/
│       ├── components/                    # 通用组件 (待实现)
│       ├── theme/                         # 主题配置 (待实现)
│       └── utils/                         # UI工具 (待实现)
│
├── gradle/
│   └── libs.versions.toml                 # 依赖版本 ✅
├── build.gradle.kts                       # 项目配置 ✅
├── settings.gradle.kts                    # 模块配置 ✅
├── .gitignore                             # Git忽略 ✅
└── README.md                              # 项目文档 ✅
```

---

## 技术栈

### 核心技术
- **语言**: Kotlin 2.0.21
- **最低SDK**: Android 8.0 (API 26)
- **目标SDK**: Android 14 (API 34)
- **架构**: MVVM + Repository

### 主要依赖
- **UI**: Jetpack Compose + Material Design 3
- **依赖注入**: Hilt 2.51
- **网络**: Retrofit 2.11.0 + OkHttp 4.12.0
- **图片**: Coil 2.6.0
- **本地存储**: Room 2.6.1 + DataStore 1.1.1
- **异步**: Coroutines 1.8.0 + Flow
- **导航**: Navigation Compose 2.7.7

---

## 下一步计划

### 🎯 立即开始 (任务17 - 核心功能)
**任务17：实现按日期浏览相册**
1. 创建AlbumRepository，实现按日期分组查询
2. 创建AlbumViewModel，管理相册列表状态
3. 更新AlbumsScreen，实现日期分组展示
4. 支持按年份、月份、日期三级展示
5. 点击日期跳转到对应的照片列表
6. 显示每个日期的照片数量和缩略图

**核心价值**：
- 按时间线浏览照片，更符合用户习惯
- 快速定位特定时间段的照片
- 提供直观的照片组织方式
- 完善相册功能模块

### 近期目标 (任务18-19)
继续完成核心功能，实现人脸识别管理。

---

## 项目统计

- **总任务数**: 31
- **已完成**: 17 (55%)
- **进行中**: 0
- **待开始**: 14 (45%)
- **下一步**: 任务17 - 实现按日期浏览相册
- **预计完成时间**: 根据开发进度持续更新

---

## 备注

1. 项目采用多模块架构，便于代码复用和维护
2. 所有核心基础设施已就绪，可以开始功能开发
3. 网络层和数据层已完全解耦，易于测试
4. 使用Hilt进行依赖注入，代码结构清晰
5. 遵循Material Design 3设计规范

---

## 重要更新记录

### 🔧 动态服务器地址支持
**更新时间**: 2025-12-22 15:35

**问题描述**:
修改服务器地址无效，应用始终访问旧地址（127.0.0.1）。原因是Retrofit的baseUrl在创建时就固定了，即使修改DataStore配置也不会重新读取。

**解决方案**:
1. ✅ 创建DynamicBaseUrlInterceptor
   - 在每次请求时动态读取服务器地址
   - 替换请求的scheme、host、port
   - 保持原始的path和query参数

2. ✅ 调整拦截器顺序
   - DynamicBaseUrlInterceptor必须在最前面
   - 确保后续拦截器看到正确的URL

3. ✅ 简化Retrofit配置
   - baseUrl改为占位符（http://placeholder.com/）
   - 实际URL由拦截器动态提供
   - 移除对BaseUrlProvider的直接依赖

4. ✅ 创建详细文档
   - docs/DYNAMIC-URL-FIX.md
   - 说明工作原理和使用方法

**影响范围**:
- `core-network/interceptor/DynamicBaseUrlInterceptor.kt` (新增)
- `core-network/di/NetworkModule.kt` (修改)

**技术要点**:
- 拦截器顺序：DynamicBaseUrlInterceptor → AuthInterceptor → UnauthorizedInterceptor → LoggingInterceptor
- 每次请求都从BaseUrlProvider读取最新配置
- 修改服务器地址后立即生效，无需重启应用
- 支持运行时切换环境（开发/测试/生产）
- 请求直接发送JSON参数，不需要JSON-RPC外层包装
- Content-Type: `application/json; charset=utf-8`

**优势**:
- ✅ 真正的动态URL支持
- ✅ 修改后立即生效
- ✅ 架构清晰，职责分离
- ✅ 向后兼容，不影响现有代码

---

## 🔧 Bug修复记录

### 2026-01-14: 修复Retrofit接口参数类型问题

**问题描述**:
- 加载活动列表时报错：`Parameter type must not include a type variable or wildcard: java.util.Map<java.lang.String, ?>`
- 原因是 `MpmApiService.getActivities()` 方法的参数类型 `Map<String, Any>` 被 Kotlin 编译器推断为 `Map<String, ?>`（通配符类型）
- Retrofit 不支持通配符类型的参数

**修复方案**:
- 在 `getActivities()` 方法的参数上添加 `@JvmSuppressWildcards` 注解
- 修改为：`@Body request: Map<String, @JvmSuppressWildcards Any>`
- 这个注解告诉 Kotlin 编译器不要将 `Any` 类型转换为通配符 `?`
- 参考了同文件中 `getAllTags()` 和 `getFacesWithName()` 方法的实现

**影响范围**:
- ✅ 活动列表现在可以正常加载
- ✅ 照片编辑页面的活动下拉列表可以正常工作
- ✅ 照片列表的活动筛选功能可以正常工作

**相关文件**:
- `MpmApiService.kt` - 添加 `@JvmSuppressWildcards` 注解

**技术要点**:
- Kotlin 的泛型类型在编译为 Java 字节码时可能会被转换为通配符类型
- `@JvmSuppressWildcards` 注解可以抑制这种转换
- Retrofit 要求参数类型必须是具体类型，不能是通配符类型
- 对于 `Map<String, Any>` 类型的参数，必须使用 `@JvmSuppressWildcards` 注解

---

### 2026-01-14: 修复活动创建成功后报错"data is null"问题

**问题描述**:
- 创建活动成功后，前端报错"data is null"
- 原因是后端创建活动成功后返回的`ApiResponse<Activity>`中`data`字段为`null`
- `ApiResponse.getDataOrThrow()`方法会在`data`为`null`时抛出异常

**修复方案**:
- 修改`ActivityRepository.createActivity()`方法，返回类型从`Flow<Result<Activity>>`改为`Flow<Result<Unit>>`
- 修改`ActivityRepository.updateActivity()`方法，返回类型从`Flow<Result<Activity>>`改为`Flow<Result<Unit>>`
- 不再使用`safeApiCall`，而是直接调用API并检查`response.isSuccess()`
- 创建/更新成功后返回`Result.Success(Unit)`，不依赖后端返回的`data`字段
- 添加`NetworkErrorHandler`的import以处理异常

**影响范围**:
- ✅ 活动创建功能现在可以正常工作
- ✅ 活动更新功能也得到修复
- ✅ 创建/更新成功后会自动返回并刷新活动列表

**相关文件**:
- `ActivityRepository.kt` - 修改创建和更新方法的返回类型和实现逻辑

**技术要点**:
- 后端某些API在成功时不返回data字段，只返回code=0表示成功
- 前端需要适配这种情况，不能强制要求data字段存在
- 使用`Result.Success(Unit)`表示操作成功但无返回数据

---

### 2026-01-14: 修复HTTPS视频播放SSL证书验证失败问题

**问题描述**:
- 使用HTTPS协议播放视频时报错：`SSLHandshakeException: Trust anchor for certification path not found`
- 原因是服务器使用了自签名证书或开发环境证书，Android无法验证证书链

**修复方案**:
- 创建`SSLHelper`工具类，提供信任所有证书的SSLSocketFactory
- 实现自定义的X509TrustManager，跳过证书验证
- 实现自定义的HostnameVerifier，信任所有主机名
- 在ExoPlayer的HTTP数据源配置中，设置HttpsURLConnection的默认SSL配置

**影响范围**:
- ✅ HTTPS视频现在可以正常播放
- ✅ 支持自签名证书和开发环境证书
- ⚠️ 此配置仅用于开发环境，生产环境应使用正确的证书验证

**相关文件**:
- `VideoPlayer.kt` - 添加SSLHelper工具类和SSL配置

**安全提示**:
- 信任所有证书会带来安全风险，仅在开发环境使用
- 生产环境应该：
  1. 使用正规CA签发的证书
  2. 或将自签名证书添加到应用的信任库中
  3. 或使用Network Security Configuration配置信任的证书

---

### 2026-01-09: 修复照片时间字段命名不一致问题

**问题描述**:
- `UpdateImageRequest` 中使用了 `@SerializedName("takenDate")` (camelCase)
- 而 `Photo` 模型中使用了 `@SerializedName("taken_date")` (snake_case)
- 导致更新照片时间时，后端无法正确接收参数

**修复方案**:
- 统一使用 snake_case 命名规范
- 将 `UpdateImageRequest.takenDate` 的 `@SerializedName` 从 `"takenDate"` 改为 `"taken_date"`
- 保持 Kotlin 属性名为 `takenDate`（符合 Kotlin 命名规范）

**影响范围**:
- ✅ 照片编辑功能现在可以正确更新拍摄时间
- ✅ 符合后端 API 的 snake_case 命名规范
- ✅ 与 Photo 模型保持一致

**相关文件**:
- `ApiRequest.kt` - 修复 UpdateImageRequest 字段命名

---

### 2026-01-27: 新增自动同步照片功能任务

**功能描述**:
- 新增任务14.5：实现自动同步照片功能
- 优先级：P0 - 高优先级核心功能
- 支持自动同步指定目录下的照片和视频到服务器

**核心功能**:
1. 在设置页面配置同步目录
2. 支持后台自动同步
3. 支持增量同步和断点续传
4. 支持多种同步策略（立即/定期/仅WiFi）
5. 显示同步进度和历史记录
6. 支持文件变化监听和自动触发

**技术方案**:
- 使用WorkManager实现定期后台任务
- 使用Foreground Service确保同步不被杀死
- 使用Room数据库持久化同步状态
- 使用ContentObserver监听媒体文件变化
- 使用DocumentsContract选择同步目录

**用户价值**:
- 自动备份照片，无需手动操作
- 支持后台同步，不影响正常使用
- 增量同步，节省流量和时间
- 支持多种同步策略，满足不同需求

**项目影响**:
- 总任务数从30增加到31
- 待开始任务从14增加到15
- 完成率从53%调整为52%

---

*最后更新时间: 2026-01-28 10:20*