<template>
  <div class="photos-container">
    <!-- 过滤器 -->
    <div class="filter-bar">
      <t-space>
        <t-button
          :theme="filters.star ? 'primary' : 'default'"
          :variant="filters.star ? 'base' : 'outline'"
          @click="toggleFilter('star')"
        >
          <template #icon><t-icon name="star" /></template>
          收藏
        </t-button>
        
        <t-button
          :theme="filters.video ? 'primary' : 'default'"
          :variant="filters.video ? 'base' : 'outline'"
          @click="toggleFilter('video')"
        >
          <template #icon><t-icon name="video" /></template>
          视频
        </t-button>
        
        <t-select
          v-model="filters.order"
          placeholder="排序"
          style="width: 150px"
          @change="resetAndLoad"
        >
          <t-option value="id" label="ID 升序" />
          <t-option value="-id" label="ID 降序" />
          <t-option value="taken_date" label="日期 升序" />
          <t-option value="-taken_date" label="日期 降序" />
        </t-select>
        
        <t-input
          v-model="searchTag"
          placeholder="搜索标签"
          clearable
          style="width: 200px"
          @change="resetAndLoad"
        >
          <template #prefix-icon>
            <t-icon name="search" />
          </template>
        </t-input>
      </t-space>
      
      <div class="right-actions">
        <span v-if="selectedPhotos.size > 0" class="selection-info">
          已选择 {{ selectedPhotos.size }} 张
          <t-button size="small" variant="text" @click="clearSelection">
            清除选择
          </t-button>
        </span>
        <span v-else class="count-text">
          共 {{ totalCount }} 张照片
        </span>
        <t-button
          v-if="totalCount > 0"
          size="small"
          variant="text"
          @click="scrollToTop"
        >
          <template #icon><t-icon name="arrow-up" /></template>
          回到顶部
        </t-button>
      </div>
    </div>
    
    <!-- 照片网格 - 虚拟滚动 -->
    <div
      ref="scrollContainer"
      class="photos-scroll-container"
      @scroll="handleScroll"
    >
      <!-- 虚拟占位空间 -->
      <div :style="{ height: totalHeight + 'px', position: 'relative' }">
        <!-- 可见区域的照片 -->
        <div
          :style="{ transform: `translateY(${offsetY}px)` }"
          class="photos-grid"
        >
          <div
            v-for="photo in visiblePhotos"
            :key="photo.id"
            class="photo-item"
            :class="{ 'photo-selected': selectedPhotos.has(photo.id) }"
            @click="handlePhotoClick(photo, $event)"
          >
            <div class="photo-wrapper">
              <!-- 选中标记 -->
              <div v-if="selectedPhotos.has(photo.id)" class="selection-badge">
                <t-icon name="check-circle-filled" size="24px" />
              </div>
              
              <!-- 收藏标记（小星星） -->
              <div v-if="photo.star" class="star-badge">
                <t-icon name="star-filled" size="16px" />
              </div>
              
              <!-- 更多操作按钮 -->
              <div class="action-menu" @click.stop>
                <t-dropdown
                  :min-column-width="120"
                  trigger="click"
                  placement="bottom-right"
                >
                  <t-button
                    theme="default"
                    variant="text"
                    shape="circle"
                    size="small"
                    class="menu-trigger"
                  >
                    <t-icon name="ellipsis" />
                  </t-button>
                  
                  <t-dropdown-menu>
                    <t-dropdown-item @click="toggleStar(photo)">
                      <template #prefix-icon>
                        <t-icon :name="photo.star ? 'star-filled' : 'star'" />
                      </template>
                      {{ photo.star ? '取消收藏' : '收藏' }}
                    </t-dropdown-item>
                    
                    <t-dropdown-item theme="error" @click="trashPhoto(photo)">
                      <template #prefix-icon>
                        <t-icon name="delete" />
                      </template>
                      删除
                    </t-dropdown-item>
                  </t-dropdown-menu>
                </t-dropdown>
              </div>
              
              <img
                :src="`/cos/${photo.thumb}`"
                :alt="photo.name"
                class="photo-img"
                loading="lazy"
              />
            </div>
            
            <div class="photo-info">
              <div class="photo-date">{{ formatDate(photo.takenDate) }}</div>
              <div v-if="photo.address" class="photo-address">
                <t-icon name="location" size="12px" />
                {{ photo.address }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- 加载中指示器 -->
        <div
          v-if="loading"
          class="loading-overlay"
          :style="{ transform: `translateY(${offsetY + visiblePhotos.length * itemHeight}px)` }"
        >
          <t-loading size="small" />
          <span class="loading-text">加载中...</span>
        </div>
      </div>
      
      <div v-if="totalCount === 0 && !loading" class="empty-state">
        <t-empty description="暂无照片" />
      </div>
    </div>
    
    <!-- 照片查看器 -->
    <t-dialog
      v-model:visible="viewerVisible"
      :header="currentPhoto?.name"
      width="90%"
      :footer="false"
      destroy-on-close
    >
      <div v-if="currentPhoto" class="photo-viewer">
        <div class="viewer-image">
          <img
            :src="`/cos/small/${currentPhoto.name}`"
            :alt="currentPhoto.name"
            style="max-width: 100%; max-height: 70vh; object-fit: contain;"
          />
        </div>
        
        <div class="viewer-info">
          <t-descriptions :column="2" bordered>
            <t-descriptions-item label="拍摄日期">
              {{ formatDateTime(currentPhoto.takenDate) }}
            </t-descriptions-item>
            <t-descriptions-item label="尺寸">
              {{ currentPhoto.width }} × {{ currentPhoto.height }}
            </t-descriptions-item>
            <t-descriptions-item v-if="currentPhoto.address" label="位置">
              {{ currentPhoto.address }}
            </t-descriptions-item>
            <t-descriptions-item v-if="currentPhoto.tag" label="标签">
              {{ currentPhoto.tag }}
            </t-descriptions-item>
            <t-descriptions-item v-if="currentPhoto.activityDesc" label="活动" :span="2">
              {{ currentPhoto.activityDesc }}
            </t-descriptions-item>
          </t-descriptions>
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getPicsApi, updateImageApi, trashPhotosApi, getCountApi, Photo } from '@/api'
import dayjs from 'dayjs'

const loading = ref(false)
const scrollContainer = ref<HTMLElement | null>(null)
const totalCount = ref(0)
const searchTag = ref('')

const filters = reactive({
  star: false,
  video: false,
  order: '-taken_date' as string
})

// 虚拟滚动配置
const itemHeight = 280 // 每个照片项的高度（包括图片和信息）
const itemsPerRow = ref(5) // 每行显示数量
const bufferSize = 3 // 缓冲行数

// 照片缓存: key = start, value = photos
const photoCache = ref<Map<number, Photo[]>>(new Map())
const pageSize = 100 // 每次请求的数量

// 滚动状态
const scrollTop = ref(0)
const containerHeight = ref(0)

// 计算总高度（基于总数和每行数量）
const totalHeight = computed(() => {
  const rowCount = Math.ceil(totalCount.value / itemsPerRow.value)
  return rowCount * itemHeight
})

// 计算可见范围
const visibleRange = computed(() => {
  const startRow = Math.floor(scrollTop.value / itemHeight)
  const endRow = Math.ceil((scrollTop.value + containerHeight.value) / itemHeight)
  
  // 添加缓冲区
  const bufferedStartRow = Math.max(0, startRow - bufferSize)
  const bufferedEndRow = endRow + bufferSize
  
  const startIndex = bufferedStartRow * itemsPerRow.value
  const endIndex = Math.min(bufferedEndRow * itemsPerRow.value, totalCount.value)
  
  return {
    startIndex,
    endIndex,
    startRow: bufferedStartRow
  }
})

// 计算偏移量
const offsetY = computed(() => {
  return visibleRange.value.startRow * itemHeight
})

// 获取可见照片
const visiblePhotos = computed(() => {
  const { startIndex, endIndex } = visibleRange.value
  const photos: Photo[] = []
  
  // 从缓存中获取照片
  for (let i = startIndex; i < endIndex; i++) {
    const cacheKey = Math.floor(i / pageSize) * pageSize
    const cachePhotos = photoCache.value.get(cacheKey)
    if (cachePhotos) {
      const photoIndex = i - cacheKey
      if (cachePhotos[photoIndex]) {
        photos.push(cachePhotos[photoIndex])
      }
    }
  }
  
  return photos
})

const viewerVisible = ref(false)
const currentPhoto = ref<Photo | null>(null)

// 多选功能
const selectedPhotos = ref<Set<number>>(new Set())
const lastSelectedIndex = ref<number>(-1)

// 清除选择
const clearSelection = () => {
  selectedPhotos.value.clear()
  lastSelectedIndex.value = -1
}

// 切换过滤器，重置列表
const toggleFilter = (key: 'star' | 'video') => {
  filters[key] = !filters[key]
  clearSelection()
  resetAndLoad()
}

// 重置并加载
const resetAndLoad = async () => {
  photoCache.value.clear()
  scrollTop.value = 0
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = 0
  }
  clearSelection()
  await loadCount()
  loadVisiblePhotos()
}

// 加载指定范围的照片
const loadPhotos = async (start: number, size: number) => {
  const cacheKey = start
  
  // 如果已在缓存中，直接返回
  if (photoCache.value.has(cacheKey)) {
    return
  }
  
  try {
    const res = await getPicsApi({
      star: filters.star || undefined,
      video: filters.video || undefined,
      trashed: false,
      start,
      size,
      order: filters.order,
      tag: searchTag.value || undefined
    })
    
    if (res.code === 0) {
      photoCache.value.set(cacheKey, res.data.data)
    }
  } catch (error) {
    console.error('Load photos error:', error)
  }
}

// 加载可见区域的照片
const loadVisiblePhotos = async () => {
  if (loading.value || totalCount.value === 0) return
  
  loading.value = true
  
  const { startIndex, endIndex } = visibleRange.value
  
  // 计算需要加载的分页
  const startPage = Math.floor(startIndex / pageSize)
  const endPage = Math.floor((endIndex - 1) / pageSize)
  
  // 并行加载所有需要的分页
  const promises: Promise<void>[] = []
  for (let page = startPage; page <= endPage; page++) {
    const start = page * pageSize
    if (!photoCache.value.has(start)) {
      promises.push(loadPhotos(start, pageSize))
    }
  }
  
  await Promise.all(promises)
  loading.value = false
}

// 滚动事件处理
const handleScroll = async () => {
  if (!scrollContainer.value) return
  
  scrollTop.value = scrollContainer.value.scrollTop
  containerHeight.value = scrollContainer.value.clientHeight
  
  // 加载可见区域的照片
  await loadVisiblePhotos()
}

// 回到顶部
const scrollToTop = () => {
  if (scrollContainer.value) {
    scrollContainer.value.scrollTo({
      top: 0,
      behavior: 'smooth'
    })
  }
}

// 计算每行显示数量
const updateItemsPerRow = () => {
  if (!scrollContainer.value) return
  
  const containerWidth = scrollContainer.value.clientWidth
  const itemWidth = 216 // 200px + 16px gap
  itemsPerRow.value = Math.max(1, Math.floor(containerWidth / itemWidth))
}

const loadCount = async () => {
  try {
    const res = await getCountApi({ trashed: false })
    if (res.code === 0) {
      totalCount.value = res.data
    }
  } catch (error) {
    console.error('Load count error:', error)
  }
}

const toggleStar = async (photo: Photo) => {
  try {
    await updateImageApi({
      id: photo.id,
      star: !photo.star
    })
    photo.star = !photo.star
    MessagePlugin.success(photo.star ? '已收藏' : '已取消收藏')
  } catch (error) {
    console.error('Toggle star error:', error)
  }
}

const trashPhoto = async (photo: Photo) => {
  try {
    await trashPhotosApi([photo.name])
    MessagePlugin.success('已移至回收站')
    
    // 清除缓存，重新加载
    photoCache.value.clear()
    totalCount.value--
    await loadVisiblePhotos()
  } catch (error) {
    console.error('Trash photo error:', error)
  }
}

// 处理照片点击
const handlePhotoClick = (photo: Photo, event: MouseEvent) => {
  // 如果按住 Ctrl/Cmd 键，单独选择/取消选择
  if (event.ctrlKey || event.metaKey) {
    toggleSelection(photo)
  }
  // 如果按住 Shift 键，连续选择
  else if (event.shiftKey && lastSelectedIndex.value >= 0) {
    selectRange(photo)
  }
  // 普通点击
  else {
    // 如果已经有选中的照片，则切换当前照片的选中状态
    if (selectedPhotos.value.size > 0) {
      toggleSelection(photo)
    } else {
      // 否则查看照片
      viewPhoto(photo)
    }
  }
}

// 切换单个照片的选中状态
const toggleSelection = (photo: Photo) => {
  if (selectedPhotos.value.has(photo.id)) {
    selectedPhotos.value.delete(photo.id)
  } else {
    selectedPhotos.value.add(photo.id)
    // 记录最后选中的索引
    const allPhotos = getAllPhotosInOrder()
    lastSelectedIndex.value = allPhotos.findIndex(p => p.id === photo.id)
  }
}

// 连续选择（Shift）
const selectRange = (photo: Photo) => {
  const allPhotos = getAllPhotosInOrder()
  const currentIndex = allPhotos.findIndex(p => p.id === photo.id)
  
  if (currentIndex === -1 || lastSelectedIndex.value === -1) {
    toggleSelection(photo)
    return
  }
  
  const start = Math.min(lastSelectedIndex.value, currentIndex)
  const end = Math.max(lastSelectedIndex.value, currentIndex)
  
  // 选中范围内的所有照片
  for (let i = start; i <= end; i++) {
    selectedPhotos.value.add(allPhotos[i].id)
  }
}

// 获取所有照片（按顺序）
const getAllPhotosInOrder = (): Photo[] => {
  const allPhotos: Photo[] = []
  
  // 从缓存中提取所有照片并排序
  const cacheKeys = Array.from(photoCache.value.keys()).sort((a, b) => a - b)
  for (const key of cacheKeys) {
    const photos = photoCache.value.get(key)
    if (photos) {
      allPhotos.push(...photos)
    }
  }
  
  return allPhotos
}

const viewPhoto = (photo: Photo) => {
  currentPhoto.value = photo
  viewerVisible.value = true
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 窗口大小改变时重新计算
const handleResize = () => {
  updateItemsPerRow()
  if (scrollContainer.value) {
    containerHeight.value = scrollContainer.value.clientHeight
  }
}

onMounted(async () => {
  await loadCount()
  updateItemsPerRow()
  
  if (scrollContainer.value) {
    containerHeight.value = scrollContainer.value.clientHeight
  }
  
  await nextTick()
  await loadVisiblePhotos()
  
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.photos-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
}

.right-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.count-text {
  font-size: 14px;
  color: var(--td-text-color-secondary);
}

.photos-scroll-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  padding-bottom: 24px;
}

.photo-item {
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.photo-item:hover {
  transform: translateY(-4px);
}

.photo-item.photo-selected .photo-wrapper {
  box-shadow: 0 0 0 3px var(--td-brand-color);
  transform: scale(0.95);
}

.photo-wrapper {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.photo-wrapper:hover .action-menu {
  opacity: 1;
}

.selection-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
  color: var(--td-brand-color);
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.star-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 10;
  color: #ffc107;
  background: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.action-menu {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 9;
  opacity: 0;
  transition: opacity 0.2s;
}

/* 如果有选中标记，将操作菜单向左移动 */
.photo-selected .action-menu {
  right: 40px;
}

.menu-trigger {
  background: rgba(255, 255, 255, 0.95) !important;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(4px);
}

.menu-trigger:hover {
  background: white !important;
}

.selection-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--td-brand-color);
  font-weight: 500;
}

.photo-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  pointer-events: none;
}

.photo-wrapper:hover .photo-overlay {
  opacity: 1;
}

.photo-info {
  padding: 8px 4px;
}

.photo-date {
  font-size: 12px;
  color: var(--td-text-color-primary);
  margin-bottom: 4px;
}

.photo-address {
  font-size: 11px;
  color: var(--td-text-color-placeholder);
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.loading-overlay {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  gap: 12px;
  position: absolute;
  left: 0;
  right: 0;
}

.loading-text {
  font-size: 14px;
  color: var(--td-text-color-secondary);
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.photo-viewer {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.viewer-image {
  text-align: center;
}

.viewer-info {
  padding: 16px;
  background: var(--td-bg-color-container);
  border-radius: 8px;
}
</style>
