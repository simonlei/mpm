# 实施计划：照片多模式展示功能

## 任务清单

### 阶段一：导航架构调整（基于现有HomeScreen）

- [ ] 1. 调整底部导航配置
   - 修改 `Routes.kt`，将现有的5个路由改为6个：`ALL_PHOTOS`、`ALBUMS`、`TIMELINE`、`LOCATIONS`、`PEOPLE`、`SETTINGS`
   - 修改 `BottomNavItem.kt`，更新底部导航项配置（移除 `PHOTOS` 和 `ACTIVITIES`，新增 `ALL_PHOTOS`、`TIMELINE`、`LOCATIONS`、`PEOPLE`）
   - 更新 `HomeNavGraph`，调整导航路由映射关系
   - _需求：1.1, 1.2, 1.3_
   - _复用：现有的 `HomeScreen`、`BottomNavigationBar`、`MpmNavGraph`_

### 阶段二：改造和增强现有照片功能

- [ ] 2. 改造现有PhotoListScreen为AllPhotosScreen
   - 将现有的 `PhotoListScreen` 重命名或复制为 `AllPhotosScreen`
   - 复用现有的照片网格展示逻辑（`LazyVerticalGrid` + Coil）
   - 复用现有的分页加载逻辑
   - 增强筛选功能：添加收藏状态筛选（使用 `PhotoRepository.getPhotos(star = ...)` 参数）
   - 增强排序功能：添加排序选项UI（使用 `order` 参数）
   - 复用现有的长按多选和批量操作逻辑
   - _需求：2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8_
   - _复用：现有的 `PhotoListScreen`、`PhotoListViewModel`、`PhotoRepository.getPhotos()`_

- [ ] 3. 增强现有AlbumsScreen功能
   - 保持现有的 `AlbumsScreen` 相册列表展示逻辑
   - 增加面包屑导航组件（在TopAppBar中显示当前路径）
   - 增加目录展开/折叠功能（如果目录层级超过3层）
   - 增加长按相册操作菜单（重命名、删除、下载）
   - 复用现有的 `PhotoRepository.getPhotos(path = ...)` API
   - _需求：3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_
   - _复用：现有的 `AlbumsScreen`、`AlbumsViewModel`_

### 阶段三：新增时间轴和位置功能

- [ ] 4. 创建TimelineScreen（基于现有模式）
   - 参考 `AlbumsScreen` 的实现模式，创建 `TimelineScreen.kt` 和 `TimelineViewModel.kt`
   - 使用现有的 `PhotoRepository.getPhotosDateTree()` API
   - 复用现有的 `TreeNode` 数据模型
   - 使用 `LazyColumn` 实现年份和月份的可展开列表（参考相册的展开逻辑）
   - 点击月份时调用 `PhotoRepository.getPhotos(dateKey = "YYYY-MM")`
   - 复用现有的照片网格组件展示月份照片
   - _需求：4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.10_
   - _复用：`PhotoRepository.getPhotosDateTree()`、`TreeNode`、照片网格组件_

- [ ] 5. 创建LocationsScreen（列表视图）
   - 参考 `AllPhotosScreen` 的实现模式，创建 `LocationsScreen.kt` 和 `LocationsViewModel.kt`
   - 调用 `PhotoRepository.getPhotos()` 获取所有照片
   - 在ViewModel中按 `Photo.address` 字段分组（客户端分组逻辑）
   - 以列表形式展示地点分组（复用现有的列表Item组件）
   - 点击地点进入照片列表（复用 `AllPhotosScreen` 的网格展示）
   - _需求：5.1, 5.2, 5.3, 5.11_
   - _复用：`PhotoRepository.getPhotos()`、`Photo` 模型、照片网格组件_

### 阶段四：人脸识别和设置页整合

- [ ] 6. 创建PeopleScreen（需确认服务端API）
   - 确认服务端是否已实现人脸识别相关API
   - 如果API已实现，参考 `AllPhotosScreen` 创建 `PeopleScreen.kt` 和 `PeopleViewModel.kt`
   - 调用服务端API获取人物列表
   - 以网格形式展示人物（复用照片网格的布局逻辑）
   - 点击人物调用 `PhotoRepository.getPhotos(faceId = ...)` 获取照片
   - 如果服务端未实现，创建占位页面显示"功能开发中"
   - _需求：6.1, 6.2, 6.3, 6.4, 6.5_
   - _复用：`PhotoRepository.getPhotos()`、照片网格组件_

- [ ] 7. 创建SettingsScreen并整合现有功能
   - 创建 `SettingsScreen.kt`，使用列表形式展示功能入口
   - 在 `HomeNavGraph` 中配置导航到现有的 `ActivityScreen` 和 `UploadScreen`
   - 添加功能入口：活动、照片上传、同步设置、存储管理、关于应用
   - 实现点击功能入口的导航逻辑
   - _需求：7.1, 7.2, 7.3, 7.4, 7.5_
   - _复用：现有的 `ActivityScreen`、`UploadScreen`_

### 阶段五：通用组件抽取和性能优化

- [ ] 8. 抽取通用照片网格组件
   - 从现有的 `PhotoListScreen` 中抽取照片网格逻辑为 `PhotoGrid.kt` 可复用组件
   - 支持3/4列布局配置参数
   - 保持现有的Coil图片加载配置
   - 保持现有的懒加载和内存回收机制
   - 在 `AllPhotosScreen`、`TimelineScreen`、`LocationsScreen` 中复用该组件
   - _需求：8.1, 8.2, 8.3_
   - _复用：现有的照片网格实现、Coil配置_

- [ ] 9. 统一状态管理和错误处理
   - 检查现有的 `Result<T>` 或 `UiState` 状态管理模式
   - 如果不存在，创建 `UiState` sealed class（Loading, Success, Error, Empty）
   - 在各ViewModel中统一使用该状态管理模式
   - 复用现有的错误提示和重试逻辑
   - 实现下拉刷新和上拉加载更多的通用逻辑
   - _需求：8.4, 8.8_
   - _复用：现有的错误处理逻辑_

- [ ] 10. 实现状态保持和性能优化
   - 在各ViewModel中使用 `SavedStateHandle` 保存浏览状态（滚动位置、筛选条件）
   - 检查现有的Coil缓存配置，确保已启用内存缓存和磁盘缓存
   - 复用现有的弱网环境优化策略（优先加载缩略图）
   - 复用现有的网络请求失败重试机制
   - 确保屏幕旋转时状态保持
   - _需求：1.4, 1.5, 8.5, 8.6, 8.7, 8.10_
   - _复用：现有的Coil配置、网络请求逻辑_

## 技术实现要点

### 复用现有代码（重要）

#### Repository层（完全复用）
```kotlin
// 复用现有的PhotoRepository，所有API调用通过它进行
@Inject lateinit var photoRepository: PhotoRepository

// 全部照片模式
photoRepository.getPhotos(star = false, video = false, order = "id")

// 相册模式
photoRepository.getPhotos(path = "相册路径")

// 时间轴模式
photoRepository.getPhotosDateTree()  // 获取时间树
photoRepository.getPhotos(dateKey = "2024-01")  // 获取特定月份照片

// 人脸识别模式
photoRepository.getPhotos(faceId = 123)
```

#### 导航架构（基于现有HomeScreen调整）
```kotlin
// 修改现有的Routes.kt
object Routes {
    // 底部导航（6个Tab）
    const val ALL_PHOTOS = "all_photos"      // 新增
    const val ALBUMS = "albums"              // 保留
    const val TIMELINE = "timeline"          // 新增
    const val LOCATIONS = "locations"        // 新增
    const val PEOPLE = "people"              // 新增
    const val SETTINGS = "settings"          // 保留
    
    // 移除
    // const val PHOTOS = "photos"
    // const val ACTIVITIES = "activities"
}
```

#### UI组件（最大化复用）
```kotlin
// 复用现有的照片网格展示逻辑
// 从PhotoListScreen中抽取为可复用组件
@Composable
fun PhotoGrid(
    photos: List<Photo>,
    columns: Int = 3,
    onPhotoClick: (Photo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier
    ) {
        items(photos) { photo ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photo.thumb)  // 使用现有的Photo.thumb字段
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = null
            )
        }
    }
}
```

#### 状态管理（复用现有模式）
```kotlin
// 检查并复用现有的状态管理模式
// 如果项目中已有Result<T>或UiState，直接使用
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    object Empty : UiState<Nothing>()
}
```

## 复用现有代码清单

### 完全复用（无需修改）
- `PhotoRepository` 及其所有API方法
- `Photo`、`TreeNode`、`Activity`、`PicsResponse` 等数据模型
- `PhotoDetailScreen`（照片详情页）
- `ActivityScreen`（活动页面）
- `UploadScreen`（上传页面）
- Coil图片加载配置
- Hilt依赖注入配置

### 需要增强（在现有基础上修改）
- `HomeScreen` + `BottomNavigationBar`（调整Tab数量和配置）
- `PhotoListScreen`（改造为AllPhotosScreen，增强筛选排序）
- `AlbumsScreen`（增加面包屑导航和操作菜单）
- `Routes.kt` 和 `BottomNavItem.kt`（调整路由配置）

### 需要新建（参考现有模式）
- `TimelineScreen` + `TimelineViewModel`（参考AlbumsScreen的树形结构）
- `LocationsScreen` + `LocationsViewModel`（参考AllPhotosScreen的网格展示）
- `PeopleScreen` + `PeopleViewModel`（参考AllPhotosScreen的网格展示）
- `SettingsScreen`（简单的列表页面）
- `PhotoGrid.kt`（从现有代码中抽取）

## 注意事项

1. **优先复用现有代码**：
   - 所有API调用必须通过现有的 `PhotoRepository` 进行
   - 照片网格展示逻辑从现有的 `PhotoListScreen` 中抽取
   - 树形展开逻辑参考现有的 `AlbumsScreen` 实现
   - 状态管理、错误处理、图片加载等逻辑完全复用

2. **最小化改动**：
   - 导航架构只调整Tab配置，不改变整体结构
   - `AlbumsScreen` 只增加功能，不重构现有逻辑
   - `PhotoListScreen` 改造为 `AllPhotosScreen` 时保持核心逻辑不变

3. **渐进式开发**：
   - 先完成导航架构调整（任务1）
   - 再改造现有功能（任务2-3）
   - 最后新增功能（任务4-7）
   - 最后优化和抽取通用组件（任务8-10）

4. **服务端API确认**：
   - 人脸识别API需要先确认是否已实现
   - 如果未实现，创建占位页面，作为v2.0功能

## 实施优先级

### P0（核心功能，必须实现）
- 任务1：调整底部导航配置
- 任务2：改造现有PhotoListScreen为AllPhotosScreen
- 任务3：增强现有AlbumsScreen功能
- 任务4：创建TimelineScreen
- 任务7：创建SettingsScreen并整合现有功能

### P1（重要功能，优先实现）
- 任务5：创建LocationsScreen（列表视图）
- 任务6：创建PeopleScreen（需确认服务端API）
- 任务8：抽取通用照片网格组件
- 任务9：统一状态管理和错误处理
- 任务10：实现状态保持和性能优化

### P2（增强功能，可后续迭代）
- 地理位置展示（地图视图 + 聚簇算法）
- 时间轴快速滚动条（年份刻度标注）
- 高级筛选和搜索功能
- 批量操作优化