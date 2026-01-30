# 实施计划：照片多模式展示功能

## 任务清单

- [x] 1. 调整底部导航架构（从5个Tab改为6个）
   - 修改 `HomeScreen` 的底部导航栏，将原有的5个Tab（照片、活动、相册、上传、设置）改为6个Tab（全部、相册、时间、位置、人脸、设置）
   - 更新 `MpmNavGraph` 路由配置，添加新的路由常量：`Routes.ALL_PHOTOS`、`Routes.TIMELINE`、`Routes.LOCATIONS`、`Routes.PEOPLE`
   - 更新底部导航的图标和文本标签，使用Material Icons：`Photo`、`Folder`、`CalendarMonth`、`LocationOn`、`Face`、`Settings`
   - _需求：1.1, 1.2, 1.3_

- [x] 2. 创建设置页主入口（整合活动和上传功能）
   - 创建 `SettingsScreen.kt`，作为设置页的主入口界面
   - 使用 `LazyColumn` 实现设置项列表，包含：活动、照片上传、同步设置、存储管理、关于应用等入口
   - 配置导航路由，点击「活动」和「照片上传」时导航到现有的 `ActivityScreen` 和 `UploadScreen`
   - 使用Material Design 3的ListItem组件，确保符合设计规范
   - _需求：7.1, 7.2, 7.3, 7.4_

- [x] 3. 实现全部照片列表模式（AllPhotosScreen）
   - 创建 `AllPhotosScreen.kt` 和 `AllPhotosViewModel.kt`
   - 使用 `LazyVerticalGrid` 实现照片网格布局（3列或4列可配置）
   - 调用 `PhotoRepository.getPhotos()` 获取照片列表，支持分页加载（使用 `start` 和 `size` 参数）
   - 实现下拉刷新功能（使用 `PullRefreshIndicator`）
   - 添加筛选和排序功能：筛选面板支持文件类型（照片/视频）、收藏状态；排序支持按时间、文件名、文件大小
   - 实现长按进入多选模式，支持批量操作
   - 复用现有的 `Photo` 数据模型和 `onNavigateToPhotoDetail` 导航逻辑
   - _需求：2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8_

- [x] 4. 增强相册目录树模式（AlbumsScreen）
- [x] 4.1 实现横竖屏自适应布局
   - 修改现有的 `AlbumsScreen.kt`，使用 `LocalConfiguration` 判断横竖屏
   - 横屏模式：实现左右分栏布局（`Row` + `weight(0.3f)` 和 `weight(0.7f)`），左侧显示目录树，右侧显示照片网格
   - 竖屏模式：实现面包屑导航 + 底部抽屉布局（`ModalBottomSheet`）
   - _需求：3.9, 3.10_

- [x] 4.2 实现目录树组件（FolderTreePanel）
   - 创建 `FolderTreePanel` 可组合函数，支持目录树的展开/折叠操作
   - 使用 `LazyColumn` 实现目录树列表，每个目录项显示：文件夹图标、相册名称、照片数量
   - 实现目录点击事件，调用 `PhotoRepository.getPhotos(path = ...)` 获取该目录的照片
   - 高亮显示当前选中的目录（使用Primary颜色背景）
   - _需求：3.1, 3.2, 3.3, 3.7_

- [x] 4.3 实现面包屑导航和底部抽屉
   - 创建 `BreadcrumbNavigation` 可组合函数，显示当前路径（如：首页 > 2024 > 春游）
   - 在面包屑右侧添加目录图标按钮，点击展开 `ModalBottomSheet`
   - 实现底部抽屉（占屏幕60-70%高度），内部嵌入 `FolderTreePanel`
   - 点击目录后自动收起抽屉，更新面包屑和照片网格
   - _需求：3.4, 3.5, 3.11, 3.12_

- [x] 5. 实现时间轴树形展示模式（TimelineScreen）
   - 创建 `TimelineScreen.kt` 和 `TimelineViewModel.kt`
   - 调用 `PhotoRepository.getPhotosDateTree()` 获取时间树数据（年-月结构）
   - 使用 `LazyColumn` 实现可展开/折叠的时间树列表
   - 年份项显示：年份、照片总数、代表性封面图；月份项显示：月份、照片数量、月份封面图
   - 点击月份时调用 `PhotoRepository.getPhotos(dateKey = "YYYY-MM")` 获取该月照片
   - 实现快速滚动条（右侧显示年份刻度）
   - 默认展开最近的年份和月份
   - _需求：4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 4.10_

- [x] 6. 实现地理位置列表展示模式（LocationsScreen）
   - 创建 `LocationsScreen.kt` 和 `LocationsViewModel.kt`
   - 调用 `PhotoRepository.getPhotos()` 获取所有照片，按 `Photo.address` 字段分组
   - 使用 `LazyColumn` 实现地点列表，每个地点项显示：地点名称、照片数量、代表性封面图
   - 点击地点项时导航到该地点的照片列表页面
   - 处理空状态：如果照片没有GPS信息，显示友好提示
   - _需求：5.1, 5.2, 5.3, 5.10_

- [x] 7. 实现人脸识别分组展示模式（PeopleScreen）
   - 创建 `PeopleScreen.kt` 和 `PeopleViewModel.kt`
   - 调用服务端人脸识别API获取人物列表（需确认API是否已实现）
   - 使用 `LazyVerticalGrid` 实现人物网格，每个人物项显示：头像、名称（或「未命名」）、照片数量
   - 点击人物项时调用 `PhotoRepository.getPhotos(faceId = ...)` 获取该人物的照片列表
   - 实现长按操作菜单：重命名、合并人物、隐藏人物
   - 处理空状态：如果服务端未识别出任何人脸，显示「暂无识别到的人物」
   - _需求：6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9, 6.10_

- [x] 8. 实现状态保持和性能优化
   - 在所有ViewModel中使用 `SavedStateHandle` 保存浏览状态（滚动位置、筛选条件、展开状态等）
   - 确保所有照片列表使用 `LazyVerticalGrid` 的懒加载机制，避免内存溢出
   - 配置Coil图片加载：优先加载缩略图（`Photo.thumb`），支持渐进式加载
   - 实现网络请求失败的错误处理：使用 `Result.Error` 状态显示友好提示和重试按钮
   - 确保所有模式支持下拉刷新功能
   - _需求：1.4, 1.5, 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.8, 8.10_

- [x] 9. 确保照片详情页兼容性
   - 验证所有模式的照片点击事件都正确调用 `onNavigateToPhotoDetail(photoId)`
   - 确保现有的 `PhotoDetailScreen` 支持从不同模式进入后的左右滑动切换
   - 验证照片详情页的操作功能（分享、下载、删除）在所有模式下都能正常工作
   - _需求：9.1, 9.2, 9.3, 9.7_

- [ ] 10. 编写单元测试和集成测试
   - 为 `AllPhotosViewModel`、`TimelineViewModel`、`LocationsViewModel`、`PeopleViewModel` 编写单元测试
   - 测试状态管理：加载状态、成功状态、错误状态、空状态
   - 测试分页加载逻辑、筛选排序逻辑、展开折叠逻辑
   - 测试 `SavedStateHandle` 的状态保存和恢复
   - 确保核心业务逻辑单元测试覆盖率 > 60%
   - _需求：非功能性需求 - 代码质量_

## 实施说明

### 开发顺序建议
1. **第一阶段（核心架构）**：任务1、任务2 - 建立导航架构基础
2. **第二阶段（核心功能）**：任务3、任务4、任务5 - 实现三个主要照片浏览模式
3. **第三阶段（扩展功能）**：任务6、任务7 - 实现地理位置和人脸识别模式
4. **第四阶段（优化和测试）**：任务8、任务9、任务10 - 性能优化和测试

### 技术要点
- **复用现有代码**：充分利用 `PhotoRepository`、`Photo` 模型、`PhotoDetailScreen` 等现有组件
- **状态管理**：统一使用 `StateFlow` + `sealed class` 管理UI状态
- **导航管理**：使用 `Navigation Compose` + `SavedStateHandle` 实现状态保持
- **图片加载**：使用Coil的缓存机制，优先加载缩略图
- **Material Design 3**：所有UI组件遵循MD3设计规范，支持深色模式

### 注意事项
- 任务7（人脸识别）需要先确认服务端API是否已实现，如未实现可暂时跳过
- 任务6（地理位置）优先实现列表视图，地图视图作为v2.0功能
- 所有任务都应该在前一个任务的基础上逐步递进，确保每个步骤都是可独立测试的

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