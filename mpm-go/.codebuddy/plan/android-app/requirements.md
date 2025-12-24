# MPM Android 应用需求文档

## 引言

MPM（My Photo Manager）是一个功能完善的照片管理系统，目前已有Web端应用和完整的后台API支持。本需求文档旨在规划开发对应的Android原生应用，为用户提供移动端的照片管理体验。

Android应用将基于现有的后台API（详见API文档.md）进行开发，实现与Web端功能对等的移动端体验，包括照片浏览、上传、人脸识别、地理位置、活动管理等核心功能。

### 技术栈
- **开发语言**: Kotlin
- **最低支持版本**: Android 8.0 (API 26)
- **目标版本**: Android 14 (API 34)
- **架构模式**: MVVM + Repository
- **网络框架**: Retrofit + OkHttp
- **图片加载**: Coil
- **依赖注入**: Hilt
- **异步处理**: Kotlin Coroutines + Flow
- **本地存储**: Room + DataStore
- **UI框架**: Jetpack Compose + Material Design 3

---

## 需求

### 需求 1：用户认证与安全

**用户故事：** 作为一名用户，我希望能够安全地登录应用并保持登录状态，以便访问我的私人照片库

#### 验收标准

1. WHEN 用户首次打开应用 THEN 系统 SHALL 显示登录界面，包含账号和密码输入框
2. WHEN 用户输入正确的账号和密码并点击登录 THEN 系统 SHALL 调用 `/api/checkPassword` 接口验证身份
3. IF 登录成功 THEN 系统 SHALL 将签名（signature）和用户信息安全存储在本地，并跳转到主界面
4. IF 登录失败 THEN 系统 SHALL 显示错误提示信息
5. WHEN 用户已登录 THEN 系统 SHALL 在所有API请求的Header中自动添加 `Signature` 和 `Account` 字段
6. WHEN 用户点击退出登录 THEN 系统 SHALL 清除本地存储的认证信息并返回登录界面
7. WHEN 应用重新启动且本地存在有效的登录凭证 THEN 系统 SHALL 自动登录并进入主界面
8. IF 任何API请求返回认证失败 THEN 系统 SHALL 自动跳转到登录界面

---

### 需求 2：照片浏览与展示

**用户故事：** 作为一名用户，我希望能够流畅地浏览我的照片库，以便快速找到和查看我的照片

#### 验收标准

1. WHEN 用户进入照片页面 THEN 系统 SHALL 调用 `/api/getPics` 接口加载照片列表，以瀑布流或网格形式展示
2. WHEN 照片列表加载时 THEN 系统 SHALL 显示加载指示器
3. WHEN 用户滚动到列表底部 THEN 系统 SHALL 自动加载下一页照片（分页加载，每页75张）
4. WHEN 显示照片缩略图 THEN 系统 SHALL 通过 `/cos/` 接口加载缩略图，并使用图片缓存机制
5. WHEN 用户点击某张照片 THEN 系统 SHALL 打开照片详情页面，显示大图和详细信息
6. WHEN 用户在详情页左右滑动 THEN 系统 SHALL 切换到上一张或下一张照片
7. WHEN 用户双击或捏合手势 THEN 系统 SHALL 支持照片缩放查看
8. IF 照片包含旋转信息 THEN 系统 SHALL 自动按照正确角度显示照片
9. WHEN 用户下拉刷新 THEN 系统 SHALL 重新加载照片列表

---

### 需求 3：照片筛选与搜索

**用户故事：** 作为一名用户，我希望能够通过多种方式筛选照片，以便快速定位特定的照片

#### 验收标准

1. WHEN 用户点击筛选按钮 THEN 系统 SHALL 显示筛选选项面板
2. WHEN 用户选择"收藏"筛选 THEN 系统 SHALL 调用 `/api/getPics` 接口，参数 `star=true`，只显示收藏的照片
3. WHEN 用户选择"视频"筛选 THEN 系统 SHALL 调用 `/api/getPics` 接口，参数 `video=true`，只显示视频文件
4. WHEN 用户选择日期筛选 THEN 系统 SHALL 显示日期树（基于 `/api/getPicsDate` 接口），支持按年、月、活动筛选
5. WHEN 用户选择文件夹筛选 THEN 系统 SHALL 显示文件夹树（基于 `/api/getFoldersData` 接口），支持按路径筛选
6. WHEN 用户选择标签筛选 THEN 系统 SHALL 显示标签列表（基于 `/api/getAllTags` 接口），支持按标签筛选
7. WHEN 用户选择人脸筛选 THEN 系统 SHALL 显示已命名的人脸列表（基于 `/api/getFacesWithName` 接口），支持按人脸筛选
8. WHEN 用户应用筛选条件 THEN 系统 SHALL 使用相应参数调用 `/api/getPics` 接口并刷新照片列表
9. WHEN 用户清除筛选条件 THEN 系统 SHALL 恢复显示所有照片

---

### 需求 4：照片详情与编辑

**用户故事：** 作为一名用户，我希望能够查看和编辑照片的详细信息，以便更好地管理我的照片

#### 验收标准

1. WHEN 用户打开照片详情页 THEN 系统 SHALL 显示照片的拍摄日期、地理位置、地址、尺寸、关联活动、标签等信息
2. WHEN 用户点击收藏按钮 THEN 系统 SHALL 调用 `/api/updateImage` 接口切换收藏状态，并更新UI
3. WHEN 用户点击旋转按钮 THEN 系统 SHALL 调用 `/api/updateImage` 接口更新旋转角度（90度递增），并刷新显示
4. WHEN 用户点击编辑地理位置 THEN 系统 SHALL 显示地图选择器，允许用户修改经纬度
5. WHEN 用户修改地理位置并保存 THEN 系统 SHALL 调用 `/api/updateImage` 接口更新位置信息，并自动获取地址
6. WHEN 用户点击编辑拍摄日期 THEN 系统 SHALL 显示日期时间选择器
7. WHEN 用户修改拍摄日期并保存 THEN 系统 SHALL 调用 `/api/updateImage` 接口更新日期信息
8. WHEN 用户点击编辑标签 THEN 系统 SHALL 显示标签编辑器，支持添加、删除标签（逗号分隔）
9. WHEN 用户修改标签并保存 THEN 系统 SHALL 调用 `/api/updateImage` 接口更新标签信息
10. WHEN 用户点击关联活动 THEN 系统 SHALL 显示活动列表，允许用户选择或创建活动
11. WHEN 用户选择活动并保存 THEN 系统 SHALL 调用 `/api/updateImage` 接口更新活动关联
12. IF 照片包含人脸信息 THEN 系统 SHALL 在照片上标注人脸框和姓名（基于 `/api/getFacesForPhoto` 接口）

---

### 需求 5：照片上传

**用户故事：** 作为一名用户，我希望能够从手机上传照片到系统，以便集中管理我的照片

#### 验收标准

1. WHEN 用户点击上传按钮 THEN 系统 SHALL 显示照片选择器，支持从相册选择多张照片
2. WHEN 用户选择照片后 THEN 系统 SHALL 显示待上传照片列表，包含缩略图和文件名
3. WHEN 用户点击开始上传 THEN 系统 SHALL 为本次上传生成唯一的 `batchId`
4. WHEN 上传照片时 THEN 系统 SHALL 调用 `/api/uploadPhoto` 接口，使用 `multipart/form-data` 格式
5. WHEN 上传进行中 THEN 系统 SHALL 显示每张照片的上传进度条和整体进度
6. IF 某张照片上传失败 THEN 系统 SHALL 标记该照片为失败状态，并允许重试
7. WHEN 所有照片上传完成 THEN 系统 SHALL 显示上传结果摘要（成功/失败数量）
8. WHEN 上传过程中用户切换到后台 THEN 系统 SHALL 继续在后台上传，并显示通知栏进度
9. WHEN 用户点击取消上传 THEN 系统 SHALL 停止未完成的上传任务
10. IF 网络断开 THEN 系统 SHALL 暂停上传，并在网络恢复后提示用户继续

---

### 需求 6：回收站管理

**用户故事：** 作为一名用户，我希望能够将照片移入回收站并在需要时恢复或永久删除，以便安全地管理不需要的照片

#### 验收标准

1. WHEN 用户在照片列表长按照片 THEN 系统 SHALL 进入多选模式，允许选择多张照片
2. WHEN 用户选择照片后点击删除按钮 THEN 系统 SHALL 显示确认对话框
3. WHEN 用户确认删除 THEN 系统 SHALL 调用 `/api/trashPhotos` 接口将照片移入回收站
4. WHEN 用户进入回收站页面 THEN 系统 SHALL 调用 `/api/getPics` 接口，参数 `trashed=true`，显示回收站中的照片
5. WHEN 用户在回收站选择照片并点击恢复 THEN 系统 SHALL 调用 `/api/trashPhotos` 接口恢复照片
6. WHEN 用户点击清空回收站 THEN 系统 SHALL 显示警告对话框，说明此操作不可恢复
7. WHEN 用户确认清空回收站 THEN 系统 SHALL 调用 `/api/emptyTrash` 接口，返回 `taskId`
8. WHEN 清空回收站任务执行中 THEN 系统 SHALL 定期调用 `/api/getProgress/:taskId` 接口查询进度，并显示进度条
9. WHEN 清空回收站完成 THEN 系统 SHALL 显示完成提示，并刷新回收站列表

---

### 需求 7：活动管理

**用户故事：** 作为一名用户，我希望能够创建和管理活动，以便将相关照片组织在一起

#### 验收标准

1. WHEN 用户进入活动页面 THEN 系统 SHALL 调用 `/api/getActivities` 接口加载活动列表
2. WHEN 显示活动列表 THEN 系统 SHALL 展示活动名称、描述、日期范围、照片数量
3. WHEN 用户点击某个活动 THEN 系统 SHALL 跳转到照片页面，筛选该活动的照片
4. WHEN 用户点击创建活动按钮 THEN 系统 SHALL 显示活动编辑表单
5. WHEN 用户填写活动信息（名称、描述、开始日期、结束日期、位置）并保存 THEN 系统 SHALL 调用 `/api/createOrUpdateActivity` 接口创建活动
6. WHEN 用户长按活动项 THEN 系统 SHALL 显示操作菜单（编辑、删除）
7. WHEN 用户选择编辑活动 THEN 系统 SHALL 显示活动编辑表单，预填充现有信息
8. WHEN 用户修改活动信息并保存 THEN 系统 SHALL 调用 `/api/createOrUpdateActivity` 接口更新活动
9. WHEN 用户选择删除活动 THEN 系统 SHALL 显示确认对话框
10. WHEN 用户确认删除活动 THEN 系统 SHALL 调用 `/api/deleteActivity` 接口删除活动
11. IF 活动包含地理位置 THEN 系统 SHALL 在活动详情中显示地图标记

---

### 需求 8：人脸识别与管理

**用户故事：** 作为一名用户，我希望能够查看和管理照片中识别出的人脸，以便快速找到特定人物的照片

#### 验收标准

1. WHEN 用户进入人脸页面 THEN 系统 SHALL 调用 `/api/getFaces` 接口加载人脸列表，支持分页
2. WHEN 显示人脸列表 THEN 系统 SHALL 展示人脸缩略图（通过 `/get_face_img/:faceId/0` 获取）、姓名、照片数量
3. WHEN 用户点击某个人脸 THEN 系统 SHALL 跳转到照片页面，筛选包含该人脸的照片
4. WHEN 用户长按人脸项 THEN 系统 SHALL 显示操作菜单（命名、隐藏、收藏、合并）
5. WHEN 用户选择命名 THEN 系统 SHALL 显示输入对话框
6. WHEN 用户输入姓名并保存 THEN 系统 SHALL 调用 `/api/updateFace` 接口更新人脸姓名
7. WHEN 用户选择隐藏/显示 THEN 系统 SHALL 调用 `/api/updateFace` 接口切换隐藏状态
8. WHEN 用户选择收藏/取消收藏 THEN 系统 SHALL 调用 `/api/updateFace` 接口切换收藏状态
9. WHEN 用户选择合并人脸 THEN 系统 SHALL 显示人脸选择器，允许选择目标人脸
10. WHEN 用户确认合并 THEN 系统 SHALL 调用 `/api/mergeFace` 接口合并人脸
11. WHEN 用户在照片详情页点击人脸框 THEN 系统 SHALL 显示操作菜单（命名、删除人脸信息）
12. WHEN 用户选择删除人脸信息 THEN 系统 SHALL 调用 `/api/removePhotoFaceInfo` 接口删除该人脸标注
13. WHEN 用户选择重新扫描人脸 THEN 系统 SHALL 调用 `/api/rescanFace` 接口重新识别照片中的人脸
14. WHEN 人脸列表支持筛选 THEN 系统 SHALL 提供"显示隐藏人脸"开关和姓名搜索功能

---

### 需求 9：地图视图

**用户故事：** 作为一名用户，我希望能够在地图上查看照片的拍摄位置，以便了解照片的地理分布

#### 验收标准

1. WHEN 用户进入地图页面 THEN 系统 SHALL 调用 `/geo_json_api/loadMarkersGeoJson` 接口加载所有照片的地理位置数据
2. WHEN 地图加载完成 THEN 系统 SHALL 在地图上显示照片位置标记（使用聚合显示优化性能）
3. WHEN 用户点击地图标记 THEN 系统 SHALL 显示该位置的照片缩略图列表
4. WHEN 用户点击缩略图 THEN 系统 SHALL 打开照片详情页
5. WHEN 用户缩放地图 THEN 系统 SHALL 动态调整标记聚合程度
6. WHEN 用户点击定位按钮 THEN 系统 SHALL 将地图中心移动到用户当前位置
7. IF 照片没有地理位置信息 THEN 系统 SHALL 不在地图上显示该照片
8. WHEN 地图视图支持切换地图类型 THEN 系统 SHALL 提供标准地图、卫星地图等选项

---

### 需求 10：文件夹管理

**用户故事：** 作为一名用户，我希望能够按文件夹组织和管理照片，以便保持照片库的结构化

#### 验收标准

1. WHEN 用户进入文件夹页面 THEN 系统 SHALL 调用 `/api/getFoldersData` 接口加载文件夹树结构
2. WHEN 显示文件夹树 THEN 系统 SHALL 展示文件夹名称、照片数量，支持展开/折叠
3. WHEN 用户点击某个文件夹 THEN 系统 SHALL 跳转到照片页面，筛选该文件夹路径下的照片
4. WHEN 用户长按文件夹 THEN 系统 SHALL 显示操作菜单（移动、删除、批量修改日期、批量修改位置）
5. WHEN 用户选择移动文件夹 THEN 系统 SHALL 显示目标文件夹选择器
6. WHEN 用户选择目标文件夹并确认 THEN 系统 SHALL 调用 `/api/moveFolder` 接口移动文件夹
7. WHEN 用户选择删除文件夹 THEN 系统 SHALL 显示确认对话框
8. WHEN 用户确认删除 THEN 系统 SHALL 调用 `/api/switchTrashFolder` 接口将文件夹移入回收站
9. WHEN 用户选择批量修改日期 THEN 系统 SHALL 显示日期选择器
10. WHEN 用户选择日期并确认 THEN 系统 SHALL 调用 `/api/updateFolderDate` 接口批量更新该文件夹下所有照片的日期
11. WHEN 用户选择批量修改位置 THEN 系统 SHALL 显示地图选择器
12. WHEN 用户选择位置并确认 THEN 系统 SHALL 调用 `/api/updateFolderGis` 接口批量更新该文件夹下所有照片的位置

---

### 需求 11：相册视图（按日期）

**用户故事：** 作为一名用户，我希望能够按时间线浏览照片，以便回顾不同时期的照片

#### 验收标准

1. WHEN 用户进入相册页面 THEN 系统 SHALL 调用 `/api/getPicsDate` 接口加载日期树结构
2. WHEN 显示日期树 THEN 系统 SHALL 按年、月、活动分组展示，显示每个分组的照片数量
3. WHEN 用户点击年份 THEN 系统 SHALL 展开显示该年的所有月份
4. WHEN 用户点击月份 THEN 系统 SHALL 展开显示该月的活动（如有）
5. WHEN 用户点击某个时间节点 THEN 系统 SHALL 跳转到照片页面，筛选该时间范围的照片
6. WHEN 用户在相册页面下拉刷新 THEN 系统 SHALL 重新加载日期树结构
7. WHEN 相册页面支持筛选 THEN 系统 SHALL 提供"仅收藏"和"回收站"筛选选项

---

### 需求 12：应用设置与配置

**用户故事：** 作为一名用户，我希望能够配置应用的行为和外观，以便获得个性化的使用体验

#### 验收标准

1. WHEN 用户进入设置页面 THEN 系统 SHALL 显示各项配置选项
2. WHEN 用户配置服务器地址 THEN 系统 SHALL 允许输入自定义的后端API地址，并保存到本地
3. WHEN 用户配置图片质量 THEN 系统 SHALL 提供"原图"、"高质量"、"标准"选项，影响图片加载策略
4. WHEN 用户配置缓存策略 THEN 系统 SHALL 提供缓存大小限制和清除缓存功能
5. WHEN 用户点击清除缓存 THEN 系统 SHALL 清除所有图片缓存，并显示释放的空间大小
6. WHEN 用户配置主题模式 THEN 系统 SHALL 提供"跟随系统"、"浅色"、"深色"选项
7. WHEN 用户切换主题 THEN 系统 SHALL 立即应用新主题，无需重启应用
8. WHEN 用户查看应用信息 THEN 系统 SHALL 显示应用版本号、开源许可等信息
9. WHEN 用户配置上传设置 THEN 系统 SHALL 提供"仅WiFi上传"、"移动网络上传"选项
10. WHEN 用户配置通知设置 THEN 系统 SHALL 提供上传完成通知、任务进度通知等开关

---

### 需求 13：离线与缓存

**用户故事：** 作为一名用户，我希望应用能够智能缓存数据，以便在网络不佳时也能浏览已加载的内容

#### 验收标准

1. WHEN 用户浏览照片 THEN 系统 SHALL 自动缓存已加载的缩略图和大图
2. WHEN 用户离线时浏览照片列表 THEN 系统 SHALL 显示已缓存的照片，并标注离线状态
3. WHEN 用户离线时尝试加载未缓存的内容 THEN 系统 SHALL 显示"网络不可用"提示
4. WHEN 网络恢复 THEN 系统 SHALL 自动重试失败的请求
5. WHEN 缓存空间达到限制 THEN 系统 SHALL 按照LRU策略自动清理旧缓存
6. WHEN 用户手动清除缓存 THEN 系统 SHALL 清除所有图片缓存，但保留用户数据和配置

---

### 需求 14：性能与用户体验

**用户故事：** 作为一名用户，我希望应用运行流畅且响应迅速，以便获得良好的使用体验

#### 验收标准

1. WHEN 应用启动 THEN 系统 SHALL 在2秒内显示主界面（已登录状态）
2. WHEN 加载照片列表 THEN 系统 SHALL 使用虚拟滚动和图片懒加载技术，确保流畅滚动
3. WHEN 加载大图 THEN 系统 SHALL 先显示模糊缩略图，再逐步加载高清图（渐进式加载）
4. WHEN 用户执行操作（收藏、删除等）THEN 系统 SHALL 立即更新UI（乐观更新），后台同步到服务器
5. IF 后台同步失败 THEN 系统 SHALL 回滚UI状态，并显示错误提示
6. WHEN 应用在后台运行 THEN 系统 SHALL 释放不必要的内存资源
7. WHEN 应用内存占用过高 THEN 系统 SHALL 自动清理图片缓存
8. WHEN 用户进行长时间操作（上传、清空回收站）THEN 系统 SHALL 防止屏幕休眠
9. WHEN 应用发生错误 THEN 系统 SHALL 记录错误日志，并显示友好的错误提示
10. WHEN 用户在弱网环境 THEN 系统 SHALL 显示加载状态，并提供重试选项

---

### 需求 15：权限管理

**用户故事：** 作为一名用户，我希望应用只在必要时请求权限，以便保护我的隐私

#### 验收标准

1. WHEN 用户首次使用上传功能 THEN 系统 SHALL 请求存储/照片访问权限，并说明用途
2. IF 用户拒绝权限 THEN 系统 SHALL 显示权限说明对话框，引导用户到设置页面授权
3. WHEN 用户使用地图定位功能 THEN 系统 SHALL 请求位置权限
4. WHEN 用户使用相机拍照上传功能 THEN 系统 SHALL 请求相机权限
5. WHEN 应用需要后台上传 THEN 系统 SHALL 请求后台运行权限（Android 12+）
6. WHEN 应用需要发送通知 THEN 系统 SHALL 请求通知权限（Android 13+）
7. IF 用户撤销权限 THEN 系统 SHALL 禁用相关功能，并在用户尝试使用时提示重新授权

---

## 非功能性需求

### 性能要求
- 应用启动时间 < 2秒（冷启动）
- 照片列表滚动帧率 ≥ 60 FPS
- 图片加载时间 < 1秒（WiFi环境，标准质量）
- 内存占用 < 200MB（正常使用）

### 兼容性要求
- 支持 Android 8.0 (API 26) 及以上版本
- 支持不同屏幕尺寸（手机、平板）
- 支持横屏和竖屏模式
- 支持深色模式

### 安全性要求
- 登录凭证使用加密存储（EncryptedSharedPreferences）
- 网络通信使用 HTTPS
- 敏感信息不记录到日志
- 支持应用锁（生物识别/PIN码）

### 可维护性要求
- 代码遵循 Kotlin 编码规范
- 使用 MVVM 架构，分层清晰
- 关键功能编写单元测试
- 使用 Git 进行版本控制

---

## 技术架构说明

### 模块划分
- **app**: 应用主模块，包含 Application、MainActivity
- **feature-auth**: 认证模块（登录、登出）
- **feature-photos**: 照片浏览模块
- **feature-upload**: 照片上传模块
- **feature-faces**: 人脸管理模块
- **feature-activities**: 活动管理模块
- **feature-map**: 地图视图模块
- **feature-folders**: 文件夹管理模块
- **feature-settings**: 设置模块
- **core-network**: 网络层（Retrofit、API定义）
- **core-data**: 数据层（Repository、本地数据库）
- **core-ui**: UI组件库（通用Composable、主题）
- **core-common**: 通用工具类

### 数据流
1. UI层（Compose）→ ViewModel → Repository → Network/Database
2. 使用 StateFlow/SharedFlow 进行状态管理
3. 使用 Kotlin Coroutines 处理异步操作

### 依赖注入
- 使用 Hilt 进行依赖注入
- 每个模块定义自己的 Module
- 使用 @Singleton、@ViewModelScoped 等作用域

---

## 开发优先级

### P0（第一阶段 - MVP）
- 需求 1：用户认证与安全
- 需求 2：照片浏览与展示
- 需求 4：照片详情与编辑（基础功能）
- 需求 6：回收站管理

### P1（第二阶段 - 核心功能）
- 需求 3：照片筛选与搜索
- 需求 5：照片上传
- 需求 7：活动管理
- 需求 11：相册视图

### P2（第三阶段 - 高级功能）
- 需求 8：人脸识别与管理
- 需求 9：地图视图
- 需求 10：文件夹管理

### P3（第四阶段 - 优化完善）
- 需求 12：应用设置与配置
- 需求 13：离线与缓存
- 需求 14：性能与用户体验优化
- 需求 15：权限管理完善

---

## 设计参考

### UI/UX设计原则
- 遵循 Material Design 3 设计规范
- 使用 Material You 动态配色
- 保持与Web端功能一致，但针对移动端优化交互
- 优先考虑单手操作便利性
- 使用底部导航栏进行主要功能切换

### 关键页面
1. **登录页**: 简洁的账号密码输入，支持记住密码
2. **照片列表页**: 瀑布流/网格布局，底部导航栏
3. **照片详情页**: 全屏显示，底部操作栏（收藏、编辑、删除等）
4. **上传页**: 照片选择器 + 上传进度列表
5. **人脸列表页**: 网格布局，显示人脸缩略图和姓名
6. **活动列表页**: 卡片式布局，显示活动信息和代表照片
7. **地图页**: 全屏地图 + 浮动操作按钮
8. **设置页**: 分组列表布局

---

## 后续扩展方向

1. **智能相册**: 基于AI的照片自动分类和推荐
2. **照片编辑**: 内置简单的图片编辑功能（裁剪、滤镜等）
3. **分享功能**: 生成分享链接，支持照片分享给其他用户
4. **备份同步**: 自动备份手机照片到系统
5. **Widget支持**: 桌面小部件显示最近照片或回忆
6. **Wear OS支持**: 手表端快速浏览照片
7. **多用户支持**: 家庭共享相册功能

---

*需求文档版本: v1.0*  
*创建日期: 2025-12-19*