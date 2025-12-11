<template>
  <div class="trash-container">
    <t-card title="回收站" :bordered="false">
      <template #actions>
        <t-popconfirm
          content="确定清空回收站吗？此操作不可恢复！"
          @confirm="emptyTrash"
        >
          <t-button theme="danger" :loading="emptying">
            <template #icon><t-icon name="delete" /></template>
            清空回收站
          </t-button>
        </t-popconfirm>
      </template>
      
      <div class="trash-stats">
        <t-alert theme="warning" message="回收站中的照片可以恢复或永久删除" />
        <div class="stats-row">
          <span v-if="selectedPhotos.size > 0" class="selection-info">
            已选择 {{ selectedPhotos.size }} 张
            <t-button size="small" variant="text" @click="clearSelection">
              清除选择
            </t-button>
          </span>
          <span v-else class="count-text">共 {{ totalCount }} 张照片</span>
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
                    <t-dropdown-item @click="restorePhoto(photo)">
                      <template #prefix-icon>
                        <t-icon name="rollback" />
                      </template>
                      恢复
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
        <t-empty description="回收站为空" />
      </div>
    </div>
    </t-card>
    
    <!-- 清空进度对话框 -->
    <t-dialog
      v-model:visible="progressVisible"
      header="清空回收站"
      :footer="false"
      :close-btn="false"
      :close-on-overlay-click="false"
    >
      <div class="progress-content">
        <t-progress
          :percentage="progress"
          :label="`${progressData.count} / ${progressData.total}`"
        />
        <div class="progress-text">正在删除照片...</div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import {
  getPicsApi,
  getCountApi,
  trashPhotosApi,
  emptyTrashApi,
  getProgressApi,
  Photo
} from '@/api'
import dayjs from 'dayjs'

const loading = ref(false)
const emptying = ref(false)
const scrollContainer = ref<HTMLElement | null>(null)
const totalCount = ref(0)

// 虚拟滚动配置
const itemHeight = 280
const itemsPerRow = ref(5)
const bufferSize = 3

// 照片缓存
const photoCache = ref<Map<number, Photo[]>>(new Map())
const pageSize = 100

// 滚动状态
const scrollTop = ref(0)
const containerHeight = ref(0)

// 计算总高度
const totalHeight = computed(() => {
  const rowCount = Math.ceil(totalCount.value / itemsPerRow.value)
  return rowCount * itemHeight
})

// 计算可见范围
const visibleRange = computed(() => {
  const startRow = Math.floor(scrollTop.value / itemHeight)
  const endRow = Math.ceil((scrollTop.value + containerHeight.value) / itemHeight)
  
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

const progressVisible = ref(false)
const progressData = reactive({
  total: 0,
  count: 0,
  progress: 0
})

const progress = ref(0)
let progressTimer: number | null = null

// 多选功能
const selectedPhotos = ref<Set<number>>(new Set())
const lastSelectedIndex = ref<number>(-1)

// 加载指定范围的照片
const loadPhotos = async (start: number, size: number) => {
  const cacheKey = start
  
  if (photoCache.value.has(cacheKey)) {
    return
  }
  
  try {
    const res = await getPicsApi({
      trashed: true,
      start,
      size,
      order: '-taken_date'
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
  
  const startPage = Math.floor(startIndex / pageSize)
  const endPage = Math.floor((endIndex - 1) / pageSize)
  
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
  
  await loadVisiblePhotos()
}

// 计算每行显示数量
const updateItemsPerRow = () => {
  if (!scrollContainer.value) return
  
  const containerWidth = scrollContainer.value.clientWidth
  const itemWidth = 216
  itemsPerRow.value = Math.max(1, Math.floor(containerWidth / itemWidth))
}

// 获取所有照片（按顺序）
const getAllPhotosInOrder = (): Photo[] => {
  const allPhotos: Photo[] = []
  
  const cacheKeys = Array.from(photoCache.value.keys()).sort((a, b) => a - b)
  for (const key of cacheKeys) {
    const photos = photoCache.value.get(key)
    if (photos) {
      allPhotos.push(...photos)
    }
  }
  
  return allPhotos
}

// 清除选择
const clearSelection = () => {
  selectedPhotos.value.clear()
  lastSelectedIndex.value = -1
}

// 处理照片点击
const handlePhotoClick = (photo: Photo, event: MouseEvent) => {
  // Ctrl/Cmd + 点击：单独选择/取消
  if (event.ctrlKey || event.metaKey) {
    toggleSelection(photo)
  }
  // Shift + 点击：连续选择
  else if (event.shiftKey && lastSelectedIndex.value >= 0) {
    selectRange(photo)
  }
  // 普通点击：切换选择
  else {
    toggleSelection(photo)
  }
}

// 切换单个照片的选中状态
const toggleSelection = (photo: Photo) => {
  if (selectedPhotos.value.has(photo.id)) {
    selectedPhotos.value.delete(photo.id)
  } else {
    selectedPhotos.value.add(photo.id)
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
  
  for (let i = start; i <= end; i++) {
    selectedPhotos.value.add(allPhotos[i].id)
  }
}

const loadCount = async () => {
  try {
    const res = await getCountApi({ trashed: true })
    if (res.code === 0) {
      totalCount.value = res.data
    }
  } catch (error) {
    console.error('Load count error:', error)
  }
}

const restorePhoto = async (photo: Photo) => {
  try {
    await trashPhotosApi([photo.name])
    MessagePlugin.success('已恢复')
    
    // 清除缓存，重新加载
    photoCache.value.clear()
    totalCount.value--
    await loadVisiblePhotos()
  } catch (error) {
    console.error('Restore photo error:', error)
  }
}

const emptyTrash = async () => {
  emptying.value = true
  try {
    const res = await emptyTrashApi()
    if (res.code === 0 && res.data.taskId) {
      progressVisible.value = true
      startProgressPolling(res.data.taskId)
    }
  } catch (error) {
    console.error('Empty trash error:', error)
    emptying.value = false
  }
}

const resetAndLoadAfterEmpty = async () => {
  photoCache.value.clear()
  scrollTop.value = 0
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = 0
  }
  clearSelection()
  await loadCount()
  await loadVisiblePhotos()
}

const startProgressPolling = (taskId: string) => {
  progressTimer = window.setInterval(async () => {
    try {
      const res = await getProgressApi(taskId)
      if (res.code === 0) {
        Object.assign(progressData, res.data)
        progress.value = res.data.progress
        
        if (res.data.progress >= 100) {
          stopProgressPolling()
          progressVisible.value = false
          emptying.value = false
          MessagePlugin.success('清空回收站完成')
          resetAndLoadAfterEmpty()
        }
      }
    } catch (error) {
      console.error('Get progress error:', error)
      stopProgressPolling()
      emptying.value = false
    }
  }, 1000)
}

const stopProgressPolling = () => {
  if (progressTimer) {
    clearInterval(progressTimer)
    progressTimer = null
  }
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
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
  stopProgressPolling()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.trash-container {
  height: 100%;
}

.trash-stats {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stats-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.count-text {
  font-size: 14px;
  color: var(--td-text-color-secondary);
}

.selection-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--td-brand-color);
  font-weight: 500;
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

.progress-content {
  padding: 24px;
  text-align: center;
}

.progress-text {
  margin-top: 16px;
  font-size: 14px;
  color: var(--td-text-color-secondary);
}
</style>
