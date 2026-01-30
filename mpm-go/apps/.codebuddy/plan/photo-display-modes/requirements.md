# 需求文档：照片多模式展示功能

## 引言

本文档定义了Android客户端照片多模式展示功能的需求。该功能旨在为用户提供多种灵活的照片浏览方式，包括列表视图、相册目录树、时间轴、地理位置聚簇和人脸识别分组。用户可以根据不同的使用场景和需求，在这些展示模式之间自由切换，以获得最佳的照片浏览和管理体验。

该功能将充分利用服务端已有的5种展示模式API，在客户端提供统一且流畅的用户体验。

**重要说明**：本需求将基于现有代码架构进行增强，复用已有的 `PhotoRepository`、`HomeScreen` 导航结构和 `AlbumsScreen` 等组件，避免重复开发。

## 需求

### 需求 1：主导航与模式切换

**用户故事：** 作为一名用户，我希望能够在不同的照片展示模式之间快速切换，以便根据不同场景选择最合适的浏览方式。

#### 验收标准

1. WHEN 用户打开应用 THEN 系统 SHALL 显示底部导航栏，包含6个Tab：「全部」、「相册」、「时间」、「位置」、「人脸」、「设置」
2. WHEN 用户点击前5个Tab（全部、相册、时间、位置、人脸）THEN 系统 SHALL 在300ms内切换到对应的照片展示模式
3. WHEN 用户点击「设置」Tab THEN 系统 SHALL 显示设置页面，包含：活动、照片上传、同步设置、存储管理等功能入口
4. WHEN 用户切换模式 THEN 系统 SHALL 使用 `SavedStateHandle` 保存当前模式的浏览状态（滚动位置、筛选条件等）
5. WHEN 用户返回之前访问过的模式 THEN 系统 SHALL 恢复该模式的上次浏览状态
6. IF 网络不可用 THEN 系统 SHALL 显示缓存的数据，并在顶部显示离线标识

**技术实现说明**：
- 复用现有的 `HomeScreen` 底部导航架构
- 将底部导航从5个Tab改为6个（全部、相册、时间、位置、人脸、设置）
- 将现有的 `ActivityScreen` 和 `UploadScreen` 整合到设置页中
- 使用 `Navigation Compose` 管理6个Tab的路由

**UI布局示意**：
```
┌─────────────────────────────────────┐
│                                     │
│        照片网格内容区域              │
│                                     │
│                                     │
├─────────────────────────────────────┤
│ 全部 相册 时间 位置 人脸 设置        │ ← 底部导航（6个Tab）
└─────────────────────────────────────┘
```

### 需求 2：全部照片列表模式

**用户故事：** 作为一名用户，我希望能够查看所有照片的列表，并进行过滤和排序，以便快速找到特定的照片。

#### 验收标准

1. WHEN 用户进入「全部」Tab THEN 系统 SHALL 以网格布局（3列或4列可配置）展示所有照片的缩略图
2. WHEN 用户下拉刷新 THEN 系统 SHALL 调用 `PhotoRepository.getPhotos()` 获取最新的照片列表
3. WHEN 用户滚动到列表底部 THEN 系统 SHALL 自动加载下一页照片（使用 `start` 和 `size` 参数分页）
4. WHEN 用户点击筛选按钮 THEN 系统 SHALL 显示筛选面板，包含：文件类型（照片/视频）、收藏状态等选项
5. WHEN 用户点击排序按钮 THEN 系统 SHALL 显示排序选项：按时间升序/降序、按文件名、按文件大小
6. WHEN 用户应用筛选或排序条件 THEN 系统 SHALL 使用 `PhotoRepository.getPhotos()` 的 `star`、`video`、`order` 参数重新请求数据
7. WHEN 用户长按照片 THEN 系统 SHALL 进入多选模式，支持批量操作（下载、删除、移动等）
8. WHEN 用户点击单张照片 THEN 系统 SHALL 调用现有的 `onNavigateToPhotoDetail(photoId)` 进入照片详情页面

**技术实现说明**：
- 复用现有的 `PhotoRepository.getPhotos()` API
- 参考现有的 `PhotoListScreen` 实现模式
- 使用现有的 `Photo` 数据模型

### 需求 3：相册目录树模式

**用户故事：** 作为一名用户，我希望能够按照上传时的原始相册目录结构浏览照片，以便保持与本地文件系统一致的组织方式。

#### 验收标准

1. WHEN 用户进入「相册」Tab THEN 系统 SHALL 显示根目录下的所有相册文件夹列表
2. WHEN 系统显示相册列表 THEN 每个相册项 SHALL 显示：相册名称、封面图（第一张照片）、照片数量、最后更新时间
3. WHEN 用户点击某个相册 THEN 系统 SHALL 调用 `PhotoRepository.getPhotos(path = "相册路径")` 获取该相册的照片
4. WHEN 用户在子目录中 THEN 系统 SHALL 在顶部显示面包屑导航，支持快速返回上级目录
5. WHEN 用户点击面包屑中的任一层级 THEN 系统 SHALL 直接跳转到该层级目录
6. WHEN 相册为空 THEN 系统 SHALL 显示空状态提示
7. IF 目录层级超过3层 THEN 系统 SHALL 支持目录树的展开/折叠操作
8. WHEN 用户长按相册 THEN 系统 SHALL 支持相册级别的操作（重命名、删除、下载整个相册等）

**技术实现说明**：
- **复用现有的 `AlbumsScreen`**，增强其功能
- 使用 `PhotoRepository.getPhotos(path = ...)` API
- 如果服务端有相册树API，优先使用；否则通过 `path` 参数逐级查询

### 需求 4：时间轴树形展示模式

**用户故事：** 作为一名用户，我希望能够按照时间维度（年-月）浏览照片，以便回顾特定时期的照片记忆。

#### 验收标准

1. WHEN 用户进入「时间」Tab THEN 系统 SHALL 调用 `PhotoRepository.getPhotosDateTree()` 获取时间树数据
2. WHEN 系统显示年份列表 THEN 每个年份项 SHALL 显示：年份、该年的照片总数、代表性封面图
3. WHEN 用户点击某个年份 THEN 系统 SHALL 展开该年份，显示其下的月份列表（1月-12月）
4. WHEN 系统显示月份列表 THEN 每个月份项 SHALL 显示：月份、该月的照片数量、月份封面图
5. WHEN 用户点击某个月份 THEN 系统 SHALL 调用 `PhotoRepository.getPhotos(dateKey = "YYYY-MM")` 获取该月的照片
6. WHEN 用户再次点击已展开的年份或月份 THEN 系统 SHALL 折叠该层级
7. WHEN 用户滚动时间轴 THEN 系统 SHALL 在右侧显示快速滚动条，标注年份刻度
8. WHEN 用户拖动快速滚动条 THEN 系统 SHALL 快速定位到对应的年份位置
9. IF 某个月份没有照片 THEN 系统 SHALL 不显示该月份项
10. WHEN 用户首次进入时间轴 THEN 系统 SHALL 默认展开最近的年份和月份

**技术实现说明**：
- 使用现有的 `PhotoRepository.getPhotosDateTree()` API
- 使用 `PhotoRepository.getPhotos(dateKey = ...)` API 获取特定月份的照片
- 参考Web端的 `TreeNode` 数据结构

### 需求 5：地理位置聚簇展示模式

**用户故事：** 作为一名用户，我希望能够在地图上查看照片的拍摄地点，并按地理位置聚簇浏览，以便回顾旅行足迹和特定地点的照片。

#### 验收标准

1. WHEN 用户进入「位置」Tab THEN 系统 SHALL 优先显示按地点名称分组的列表展示（降级方案）
2. WHEN 系统显示地点列表 THEN 每个地点项 SHALL 显示：地点名称、照片数量、代表性封面图
3. WHEN 用户点击某个地点 THEN 系统 SHALL 进入该地点的照片列表页面
4. IF 服务端支持地理位置聚簇API THEN 系统 SHALL 提供「地图视图」切换按钮
5. WHEN 用户切换到地图视图 THEN 系统 SHALL 显示地图，并在地图上标注照片聚簇点
6. WHEN 系统显示聚簇点 THEN 每个聚簇 SHALL 显示：位置标记、该位置的照片数量
7. WHEN 多个照片位置接近 THEN 系统 SHALL 自动聚合为一个聚簇点，显示总数量
8. WHEN 用户放大地图 THEN 系统 SHALL 自动拆分聚簇，显示更细粒度的位置分布
9. WHEN 用户点击某个聚簇点 THEN 系统 SHALL 显示该位置的照片网格预览（底部抽屉或弹窗）
10. IF 照片没有GPS信息 THEN 系统 SHALL 不在地图上显示该照片
11. WHEN 地图加载失败 THEN 系统 SHALL 自动降级为列表视图，并显示提示信息

**技术实现说明**：
- **第一阶段**：实现列表视图（使用 `Photo.address` 字段分组）
- **第二阶段**：如果服务端提供地理位置聚簇API，再实现地图视图
- 地图SDK选择：国内项目使用高德地图，国际项目使用Google Maps

### 需求 6：人脸识别分组展示模式

**用户故事：** 作为一名用户，我希望能够按照识别出的人物查看照片，以便快速找到包含特定人物的所有照片。

#### 验收标准

1. WHEN 用户进入「人脸」Tab THEN 系统 SHALL 调用服务端人脸识别API获取人物列表
2. WHEN 系统显示人物列表 THEN 每个人物项 SHALL 显示：人物头像（代表性照片）、人物名称（如已标注）或「未命名」、包含该人物的照片数量
3. WHEN 人物未命名 THEN 系统 SHALL 显示默认占位符和「点击命名」提示
4. WHEN 用户点击某个人物 THEN 系统 SHALL 调用 `PhotoRepository.getPhotos(faceId = ...)` 获取该人物的照片列表
5. WHEN 用户在人物照片列表中 THEN 系统 SHALL 以网格形式显示包含该人物的所有照片
6. WHEN 用户长按人物项 THEN 系统 SHALL 显示操作菜单：重命名、合并人物、隐藏人物
7. WHEN 用户点击「重命名」 THEN 系统 SHALL 显示输入框，允许用户为人物命名
8. WHEN 用户保存人物名称 THEN 系统 SHALL 将名称同步到服务端，并更新本地显示
9. WHEN 用户点击「合并人物」 THEN 系统 SHALL 进入多选模式，允许选择要合并的其他人物
10. IF 服务端未识别出任何人脸 THEN 系统 SHALL 显示空状态：「暂无识别到的人物」
11. WHEN 人物列表较多 THEN 系统 SHALL 支持按名称搜索人物

**技术实现说明**：
- 使用现有的 `PhotoRepository.getPhotos(faceId = ...)` API
- 需要确认服务端是否已实现人脸识别相关API（获取人物列表、重命名、合并等）
- 如果服务端未实现，此功能作为v2.0规划

### 需求 7：设置页整合

**用户故事：** 作为一名用户，我希望能够在设置页中统一管理活动、照片上传和系统配置，以便集中处理不常用的功能。

#### 验收标准

1. WHEN 用户进入「设置」Tab THEN 系统 SHALL 显示设置页面，包含多个功能入口
2. WHEN 系统显示设置页 THEN 应包含以下功能入口：
   - 活动（复用现有的 `ActivityScreen`）
   - 照片上传（复用现有的 `UploadScreen`）
   - 同步设置
   - 存储管理
   - 关于应用
   - 其他系统设置
3. WHEN 用户点击「活动」入口 THEN 系统 SHALL 导航到现有的 `ActivityScreen`
4. WHEN 用户点击「照片上传」入口 THEN 系统 SHALL 导航到现有的 `UploadScreen`
5. WHEN 用户点击其他设置项 THEN 系统 SHALL 导航到对应的设置页面

**技术实现说明**：
- 创建新的 `SettingsScreen`，作为设置页的主入口
- 复用现有的 `ActivityScreen` 和 `UploadScreen`
- 使用 `Navigation Compose` 管理设置页的子页面导航

### 需求 8：通用交互与性能要求

**用户故事：** 作为一名用户，我希望应用响应迅速、操作流畅，并且在各种网络条件下都能正常使用，以便获得良好的使用体验。

#### 验收标准

1. WHEN 用户执行任何操作 THEN 系统 SHALL 在100ms内给出视觉反馈（加载动画、按钮状态变化等）
2. WHEN 系统加载照片列表 THEN 缩略图 SHALL 在500ms内显示（使用Coil的渐进式加载）
3. WHEN 用户快速滚动 THEN 系统 SHALL 使用 `LazyVerticalGrid` 的懒加载机制，避免内存溢出
4. WHEN 网络请求失败 THEN 系统 SHALL 使用现有的 `Result.Error` 状态显示友好提示，并提供重试按钮
5. WHEN 用户在弱网环境 THEN 系统 SHALL 优先加载缩略图（使用 `Photo.thumb` 字段）
6. WHEN 用户已浏览过的内容 THEN 系统 SHALL 使用Coil的内存缓存和磁盘缓存，支持离线浏览
7. IF 缓存超过限制（如500MB） THEN 系统 SHALL 依赖Coil的LRU策略自动清理最旧的缓存
8. WHEN 用户在任何模式下 THEN 系统 SHALL 支持下拉刷新获取最新数据
9. WHEN 系统后台同步完成 THEN 系统 SHALL 自动刷新当前视图（如果用户仍在该页面）
10. WHEN 用户旋转屏幕 THEN 系统 SHALL 使用 `SavedStateHandle` 保持当前状态，调整布局以适应横屏/竖屏

**技术实现说明**：
- 复用现有的 `Result<T>` 状态管理模式
- 使用Coil的缓存配置（已在项目中使用）
- 参考现有的 `PhotoListScreen` 的性能优化实践

### 需求 9：照片详情与操作

**用户故事：** 作为一名用户，我希望能够查看照片的详细信息并执行各种操作，以便更好地管理我的照片。

#### 验收标准

1. WHEN 用户在任何模式下点击照片 THEN 系统 SHALL 调用现有的 `onNavigateToPhotoDetail(photoId)` 进入全屏照片查看器
2. WHEN 系统显示照片详情 THEN 应支持：双指缩放、拖动平移、左右滑动切换照片（复用现有的 `PhotoDetailScreen`）
3. WHEN 用户在照片查看器中 THEN 系统 SHALL 在底部显示操作栏：分享、下载、删除、更多
4. WHEN 用户点击「更多」 THEN 系统 SHALL 显示详细信息面板：拍摄时间、地点、设备信息、文件大小、分辨率等
5. WHEN 用户点击「下载」 THEN 系统 SHALL 将原图下载到本地相册，并显示进度
6. WHEN 用户点击「分享」 THEN 系统 SHALL 调用系统分享面板，支持分享到其他应用
7. WHEN 用户点击「删除」 THEN 系统 SHALL 调用 `PhotoRepository.trashPhotos()` 移动到回收站
8. WHEN 照片包含GPS信息 THEN 详情面板 SHALL 显示地图缩略图，点击可跳转到位置模式
9. WHEN 照片包含人脸信息 THEN 详情面板 SHALL 显示识别出的人物标签，点击可跳转到人脸模式

**技术实现说明**：
- **完全复用现有的 `PhotoDetailScreen`**
- 使用现有的 `PhotoRepository.trashPhotos()`、`PhotoRepository.updatePhoto()` 等API
- 参考现有的照片详情页实现

## 技术约束

### 核心技术栈（延续现有架构）
1. **UI框架**：Kotlin + Jetpack Compose（必须）
2. **依赖注入**：Hilt（必须）
3. **异步处理**：Coroutines + Flow（必须）
4. **网络请求**：Retrofit + OkHttp（必须）
5. **本地存储**：Room（元数据缓存，已有 `SyncFile`、`SyncDirectory` 等实体）
6. **图片加载**：Coil（项目已使用）

### 架构设计（轻量级原则）
7. **架构模式**：单模块 + MVVM（ViewModel → Repository → API/Database）
8. **导航方案**：Navigation Compose + SavedStateHandle（状态保持）
9. **状态管理**：StateFlow + sealed class（UI状态）
10. **不需要**：Clean Architecture的Domain层、复杂的多模块划分

### 复用现有代码
11. **Repository层**：复用 `PhotoRepository`，所有API调用通过它进行
12. **导航架构**：复用 `HomeScreen` + `BottomNavigationBar` + `MpmNavGraph`
13. **数据模型**：复用 `Photo`、`TreeNode`、`Activity` 等现有模型
14. **UI组件**：复用现有的 `PhotoDetailScreen`、`AlbumsScreen`、`ActivityScreen`、`UploadScreen` 等

### 可选功能（分阶段实现）
15. **地图功能**：
    - 国内项目：高德地图SDK（免费额度充足）
    - 国际项目：Google Maps SDK
    - **降级方案**：优先实现列表视图，地图作为v2.0功能
16. **人脸识别**：完全依赖服务端，客户端仅负责展示和标注

### 设计规范
17. 必须遵循 Material Design 3 设计规范
18. 必须支持深色模式（DynamicColor）
19. 必须适配 Android 8.0（API 26）及以上版本

## 非功能性需求

### 性能指标
1. **UI流畅度**：照片列表滚动帧率保持在 55fps 以上
2. **响应速度**：用户操作反馈延迟 < 100ms
3. **图片加载**：缩略图首屏加载 < 500ms
4. **内存控制**：应用内存占用 < 200MB（不含图片缓存）

### 网络优化
5. **弱网支持**：优先加载缩略图，延迟加载高清图
6. **请求管理**：支持请求取消和重试机制
7. **离线缓存**：依赖Coil的缓存机制，支持离线查看（限制500MB）

### 用户体验
8. **错误处理**：网络/加载失败时显示友好提示和重试按钮
9. **状态保持**：模式切换时保存滚动位置和筛选条件
10. **可访问性**：支持 TalkBack 屏幕阅读器

### 代码质量
11. **可维护性**：遵循DRY原则，避免代码重复
12. **错误处理**：文件/网络操作必须有完善的Try-Catch
13. **测试覆盖**：核心业务逻辑单元测试覆盖率 > 60%

### 安全性
14. **数据加密**：敏感数据（如人脸信息）必须加密存储
15. **权限管理**：动态申请存储和网络权限

## 实施优先级建议

### P0（核心功能，必须实现）
- 需求1：主导航与模式切换（6个底部Tab）
- 需求2：全部照片列表模式（复用现有实现）
- 需求3：相册目录树模式（增强现有 `AlbumsScreen`）
- 需求4：时间轴树形展示模式（使用现有API）
- 需求7：设置页整合（整合活动和上传功能）
- 需求9：照片详情与操作（复用现有 `PhotoDetailScreen`）

### P1（重要功能，优先实现）
- 需求8：通用交互与性能要求
- 需求5：地理位置展示（列表视图）
- 需求6：人脸识别分组展示（需确认服务端API）

### P2（增强功能，可后续迭代）
- 需求5：地理位置展示（地图视图 + 聚簇算法）
- 高级筛选和搜索功能
- 批量操作优化

## 与现有代码的兼容性说明

### 导航架构调整（最终方案）

**现状**：
- `HomeScreen` 包含5个底部Tab（照片、活动、相册、上传、设置）

**调整方案**：
- **底部导航**：从5个Tab改为6个（全部、相册、时间、位置、人脸、设置）
- **设置页整合**：将现有的 `ActivityScreen` 和 `UploadScreen` 整合到设置页中
- **优势**：
  - ✅ 常用功能优先：5个照片浏览模式直接在底部导航
  - ✅ 不常用功能收纳：活动、上传统一放在设置页
  - ✅ 无需顶部Tab：直接在底部导航切换，更加直观
  - ✅ 符合用户习惯：类似微信、QQ的底部导航设计

**UI布局示意**：
```
┌─────────────────────────────────────┐
│                                     │
│        照片网格内容区域              │
│                                     │
│                                     │
├─────────────────────────────────────┤
│ 全部 相册 时间 位置 人脸 设置        │ ← 底部导航（6个Tab）
└─────────────────────────────────────┘
```

**底部导航Tab详细说明**：
| Tab | 功能 | 图标 | 路由 |
|-----|------|------|------|
| 全部 | 所有照片列表（支持筛选排序） | `Icons.Default.Photo` | `Routes.ALL_PHOTOS` |
| 相册 | 相册目录树 | `Icons.Default.Folder` | `Routes.ALBUMS` |
| 时间 | 时间轴树形展示 | `Icons.Default.CalendarMonth` | `Routes.TIMELINE` |
| 位置 | 地理位置聚簇 | `Icons.Default.LocationOn` | `Routes.LOCATIONS` |
| 人脸 | 人脸识别分组 | `Icons.Default.Face` | `Routes.PEOPLE` |
| 设置 | 活动、上传、系统设置 | `Icons.Default.Settings` | `Routes.SETTINGS` |

**设置页内容结构**：
```
设置页（SettingsScreen）
├── 活动（ActivityScreen）
├── 照片上传（UploadScreen）
├── 同步设置
├── 存储管理
├── 关于应用
└── 其他系统设置
```

### API接口映射
| 功能模式 | 使用的API | 参数 |
|---------|----------|------|
| 全部照片 | `PhotoRepository.getPhotos()` | `star`, `video`, `order` |
| 相册目录树 | `PhotoRepository.getPhotos()` | `path` |
| 时间轴 | `PhotoRepository.getPhotosDateTree()` + `getPhotos()` | `dateKey` |
| 地理位置 | `PhotoRepository.getPhotos()` | 按 `address` 分组 |
| 人脸识别 | `PhotoRepository.getPhotos()` | `faceId` |

### 数据模型复用
- `Photo`：照片基础模型 ✅
- `TreeNode`：时间树节点 ✅
- `Activity`：活动模型 ✅
- `PicsResponse`：照片列表响应 ✅