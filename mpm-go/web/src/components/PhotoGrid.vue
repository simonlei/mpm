<template>
  <div class="photo-grid-container">
    <!-- 过滤器和操作栏 -->
    <div class="filter-bar">
      <t-space>
        <slot name="filters"></slot>
      </t-space>
      
      <div class="right-actions">
        <span v-if="selectedPhotos.size > 0" class="selection-info">
          已选择 {{ selectedPhotos.size }} 张
          <t-button size="small" variant="text" @click="clearSelection">
            清除选择
          </t-button>
          <slot name="batch-actions" :selectedPhotos="selectedPhotos" :selectedPhotosList="getSelectedPhotosList()"></slot>
        </span>
        <span v-else class="count-text">
          共 {{ totalCount }} 张照片
        </span>
        <t-button
          :theme="selectionMode ? 'primary' : 'default'"
          :variant="selectionMode ? 'base' : 'outline'"
          size="small"
          @click="toggleSelectionMode"
        >
          <template #icon><t-icon :name="selectionMode ? 'check-circle-filled' : 'check-circle'" /></template>
          {{ selectionMode ? '退出选择' : '选择' }}
        </t-button>
        <slot name="extra-actions"></slot>
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
            :class="{ 
              'photo-selected': selectedPhotos.has(photo.id),
              'photo-last-viewed': !selectionMode && lastViewedPhotoId === photo.id
            }"
            @click="handlePhotoClick(photo, $event)"
          >
            <div class="photo-wrapper">
              <!-- 选中标记（选择模式下才显示勾选图标） -->
              <div v-if="selectionMode && selectedPhotos.has(photo.id)" class="selection-badge">
                <t-icon name="check-circle-filled" size="24px" />
              </div>
              
              <!-- 选择框（选择模式下始终显示） -->
              <div v-if="selectionMode" class="selection-checkbox" :class="{ 'is-checked': selectedPhotos.has(photo.id) }">
                <t-icon :name="selectedPhotos.has(photo.id) ? 'check-circle-filled' : 'circle'" size="20px" />
              </div>
              
              <!-- 收藏标记（小星星） -->
              <div v-if="photo.star && !trashed" class="star-badge">
                <t-icon name="star-filled" size="16px" />
              </div>
              
              <!-- 视频时长标记 -->
              <div v-if="photo.media_type === 'video' && photo.duration" class="duration-badge">
                <t-icon name="play-circle-filled" size="12px" />
                <span>{{ formatDuration(photo.duration) }}</span>
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
                    <slot name="photo-actions" :photo="photo"></slot>
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
              <div class="photo-date">{{ formatDate(photo.taken_date) }}</div>
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
        <t-empty :description="emptyText" />
      </div>
    </div>
    
    <!-- 照片查看器 -->
    <t-dialog
      v-model:visible="viewerVisible"
      :header="currentPhotoHeader"
      width="90%"
      placement="center"
      :footer="false"
      destroy-on-close
      @opened="handleViewerOpened"
      @closed="handleViewerClosed"
    >
      <div v-if="currentPhoto" class="photo-viewer">
        <!-- 导航按钮 -->
        <div class="viewer-nav">
          <t-button
            v-if="hasPrevPhoto"
            class="nav-button nav-prev"
            theme="default"
            variant="outline"
            shape="circle"
            size="large"
            :disabled="photoSwitching"
            :loading="photoSwitching"
            @click="viewPrevPhoto"
          >
            <t-icon name="chevron-left" size="24px" />
          </t-button>
          
          <t-button
            v-if="hasNextPhoto"
            class="nav-button nav-next"
            theme="default"
            variant="outline"
            shape="circle"
            size="large"
            :disabled="photoSwitching"
            :loading="photoSwitching"
            @click="viewNextPhoto"
          >
            <t-icon name="chevron-right" size="24px" />
          </t-button>
        </div>
        
        <div class="viewer-content">
          <!-- 左侧媒体展示 -->
          <div class="viewer-image">
            <!-- 加载中遮罩 -->
            <div v-if="photoSwitching" class="viewer-loading">
              <t-loading size="large" text="加载中..." />
            </div>
            
            <!-- 视频播放器 -->
            <video
              v-if="currentPhoto.media_type === 'video'"
              v-show="!photoSwitching"
              :key="currentPhoto.id"
              :src="`/cos/video_t/${currentPhoto.name}.mp4`"
              controls
              class="video-player"
              @loadeddata="handleImageLoaded"
              @error="handleImageError"
            >
              您的浏览器不支持视频播放
            </video>
            
            <!-- 图片展示（带人脸框） -->
            <div
              v-else
              v-show="!photoSwitching"
              class="image-container"
            >
              <div class="image-wrapper" :style="{ transform: `rotate(${currentPhoto.rotate || 0}deg)` }">
                <img
                  ref="viewerImageRef"
                  :key="currentPhoto.id"
                  :src="`/cos/small/${currentPhoto.name}`"
                  :alt="currentPhoto.name"
                  @load="handleImageLoaded"
                  @error="handleImageError"
                />
                
                <!-- 人脸框 -->
                <div
                  v-if="showFaceBoxes && facesForPhoto.length > 0"
                  class="face-boxes-overlay"
                >
                  <div
                    v-for="face in facesForPhoto"
                    :key="face.id"
                    class="face-box"
                    :style="getFaceBoxStyle(face)"
                  >
                    <div class="face-name">{{ face.name || '未命名' }}</div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 旋转按钮 -->
            <div v-if="currentPhoto && currentPhoto.media_type !== 'video'" class="rotate-buttons">
              <t-button
                theme="default"
                variant="outline"
                shape="circle"
                size="medium"
                :loading="rotating"
                @click="rotatePhoto(-90)"
              >
                <template #icon>
                  <t-icon name="rollback" size="20px" />
                </template>
              </t-button>
              <t-button
                theme="default"
                variant="outline"
                shape="circle"
                size="medium"
                :loading="rotating"
                @click="rotatePhoto(90)"
              >
                <template #icon>
                  <t-icon name="rollfront" size="20px" />
                </template>
              </t-button>
            </div>
          </div>
          
          <!-- 右侧信息 -->
          <div class="viewer-info">
            <t-descriptions :column="1" bordered>
              <t-descriptions-item v-if="currentPhoto.media_type === 'video' && currentPhoto.duration" label="时长">
                {{ formatDuration(currentPhoto.duration) }}
              </t-descriptions-item>
              <t-descriptions-item label="大小">
                {{ formatFileSize(currentPhoto.size) }}
              </t-descriptions-item>
              <t-descriptions-item label="宽度">
                {{ currentPhoto.width }} px
              </t-descriptions-item>
              <t-descriptions-item label="高度">
                {{ currentPhoto.height }} px
              </t-descriptions-item>
              <t-descriptions-item label="描述">
                <div v-if="!editingDescription" class="description-display">
                  <span v-if="currentPhoto.description" class="description-text">{{ currentPhoto.description }}</span>
                  <span v-else class="empty-description">无描述</span>
                  <t-button
                    theme="default"
                    variant="text"
                    size="small"
                    @click="startEditDescription"
                  >
                    <template #icon><t-icon name="edit" /></template>
                  </t-button>
                </div>
                <div v-else class="description-editor">
                  <t-textarea
                    v-model="editDescriptionValue"
                    placeholder="请输入描述信息"
                    :autosize="{ minRows: 2, maxRows: 6 }"
                    :maxlength="500"
                    show-limit-number
                  />
                  <div class="editor-actions">
                    <t-button
                      size="small"
                      variant="outline"
                      @click="cancelEditDescription"
                    >
                      取消
                    </t-button>
                    <t-button
                      size="small"
                      theme="primary"
                      :loading="savingDescription"
                      @click="saveDescription"
                    >
                      保存
                    </t-button>
                  </div>
                </div>
              </t-descriptions-item>
              <t-descriptions-item label="时间">
                <div v-if="!editingDateTime" class="description-display">
                  <span class="description-text">{{ formatDateTime(currentPhoto.taken_date) }}</span>
                  <t-button
                    theme="default"
                    variant="text"
                    size="small"
                    @click="startEditDateTime"
                  >
                    <template #icon><t-icon name="edit" /></template>
                  </t-button>
                </div>
                <div v-else class="description-editor">
                  <t-date-picker
                    v-model="editDateTimeValue"
                    enable-time-picker
                    format="YYYY-MM-DD HH:mm:ss"
                    value-type="YYYY-MM-DD HH:mm:ss"
                    placeholder="请选择日期时间"
                    clearable
                  />
                  <div class="editor-actions">
                    <t-button
                      size="small"
                      variant="outline"
                      @click="cancelEditDateTime"
                    >
                      取消
                    </t-button>
                    <t-button
                      size="small"
                      theme="primary"
                      :loading="savingDateTime"
                      @click="saveDateTime"
                    >
                      保存
                    </t-button>
                  </div>
                </div>
              </t-descriptions-item>
              <t-descriptions-item label="标签">
                <slot name="tag-editor" :photo="currentPhoto" :tags="currentPhotoTags" :onTagChange="handleTagChange">
                  <span v-if="currentPhoto.tags">{{ currentPhoto.tags }}</span>
                  <span v-else class="empty-tag">无标签</span>
                </slot>
              </t-descriptions-item>
              <t-descriptions-item v-if="currentPhoto.address" label="地址">
                {{ currentPhoto.address }}
              </t-descriptions-item>
              <t-descriptions-item v-if="currentPhoto.activity_desc" label="所属活动">
                {{ currentPhoto.activity_desc }}
              </t-descriptions-item>
            </t-descriptions>
            
            <div class="viewer-actions">
              <t-button
                v-if="currentPhoto.media_type !== 'video'"
                :theme="showFaceBoxes ? 'primary' : 'default'"
                variant="outline"
                block
                :loading="loadingFaces"
                @click="toggleFaceBoxes"
              >
                <template #icon><t-icon name="usergroup" /></template>
                {{ showFaceBoxes ? '隐藏圈人' : '圈人' }}
              </t-button>
              <t-button
                theme="primary"
                variant="outline"
                block
                @click="downloadOriginal"
              >
                <template #icon><t-icon name="download" /></template>
                下载原图
              </t-button>
              <slot name="viewer-actions" :photo="currentPhoto"></slot>
            </div>
          </div>
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { Photo, FaceForPhoto, getFacesForPhotoApi } from '@/api'
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

// 配置 dayjs 时区插件
dayjs.extend(utc)
dayjs.extend(timezone)

interface Props {
  loadPhotosApi: (start: number, size: number) => Promise<Photo[]>
  totalCount: number
  trashed?: boolean
  emptyText?: string
}

const props = withDefaults(defineProps<Props>(), {
  trashed: false,
  emptyText: '暂无照片'
})

const emit = defineEmits<{
  'update:totalCount': [count: number]
  'refresh': []
}>()

const loading = ref(false)
const scrollContainer = ref<HTMLElement | null>(null)
const viewerImageRef = ref<HTMLImageElement | null>(null)

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
  const rowCount = Math.ceil(props.totalCount / itemsPerRow.value)
  return rowCount * itemHeight
})

// 计算可见范围
const visibleRange = computed(() => {
  const startRow = Math.floor(scrollTop.value / itemHeight)
  const endRow = Math.ceil((scrollTop.value + containerHeight.value) / itemHeight)
  
  const bufferedStartRow = Math.max(0, startRow - bufferSize)
  const bufferedEndRow = endRow + bufferSize
  
  const startIndex = bufferedStartRow * itemsPerRow.value
  const endIndex = Math.min(bufferedEndRow * itemsPerRow.value, props.totalCount)
  
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

// 查看器相关
const viewerVisible = ref(false)
const currentPhoto = ref<Photo | null>(null)
const currentPhotoIndex = ref<number>(-1)
const photoSwitching = ref(false)
const currentPhotoTags = ref<string[]>([])

// 多选功能
const selectedPhotos = ref<Set<number>>(new Set())
const lastSelectedIndex = ref<number>(-1)
const selectionMode = ref(false)

// 当前浏览的照片（用于在网格中高亮显示）
const lastViewedPhotoId = ref<number | null>(null)

// 人脸圈选功能
const showFaceBoxes = ref(false)
const loadingFaces = ref(false)
const facesForPhoto = ref<FaceForPhoto[]>([])

// 计算当前照片的标题
const currentPhotoHeader = computed(() => {
  if (!currentPhoto.value) return ''
  const index = currentPhotoIndex.value
  if (index >= 0 && props.totalCount > 0) {
    return `${currentPhoto.value.name} (${index + 1} / ${props.totalCount})`
  }
  return currentPhoto.value.name
})

// 是否有上一张/下一张
const hasPrevPhoto = computed(() => currentPhotoIndex.value > 0)
const hasNextPhoto = computed(() => currentPhotoIndex.value < props.totalCount - 1)

// 加载指定范围的照片
const loadPhotos = async (start: number, size: number) => {
  const cacheKey = start
  
  if (photoCache.value.has(cacheKey)) {
    return
  }
  
  try {
    const photos = await props.loadPhotosApi(start, size)
    photoCache.value.set(cacheKey, photos)
  } catch (error) {
    console.error('Load photos error:', error)
  }
}

// 加载可见区域的照片
const loadVisiblePhotos = async () => {
  if (loading.value || props.totalCount === 0) return
  
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

// 获取选中的照片列表
const getSelectedPhotosList = (): Photo[] => {
  const allPhotos = getAllPhotosInOrder()
  return allPhotos.filter(p => selectedPhotos.value.has(p.id))
}

// 清除选择
const clearSelection = () => {
  selectedPhotos.value.clear()
  lastSelectedIndex.value = -1
}

// 切换选择模式
const toggleSelectionMode = () => {
  selectionMode.value = !selectionMode.value
  
  if (!selectionMode.value) {
    clearSelection()
  } else {
    // 进入选择模式时清除高亮
    lastViewedPhotoId.value = null
  }
}

// 处理照片点击
const handlePhotoClick = (photo: Photo, event: MouseEvent) => {
  if (selectionMode.value) {
    if (event.ctrlKey || event.metaKey) {
      toggleSelection(photo)
    } else if (event.shiftKey && lastSelectedIndex.value >= 0) {
      selectRange(photo)
    } else {
      toggleSelection(photo)
    }
  } else {
    // 清除上次查看的高亮
    lastViewedPhotoId.value = null
    viewPhoto(photo)
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

const viewPhoto = (photo: Photo) => {
  currentPhoto.value = photo
  currentPhotoTags.value = photo.tags ? photo.tags.split(',').filter(t => t.trim()) : []
  
  const allPhotos = getAllPhotosInOrder()
  currentPhotoIndex.value = allPhotos.findIndex(p => p.id === photo.id)
  
  // 重置编辑状态
  editingDescription.value = false
  editDescriptionValue.value = ''
  editingDateTime.value = false
  editDateTimeValue.value = ''
  
  viewerVisible.value = true
}

// 查看上一张/下一张照片
const viewPrevPhoto = async () => {
  if (!hasPrevPhoto.value || photoSwitching.value) return
  
  photoSwitching.value = true
  
  // 重置编辑状态
  editingDescription.value = false
  editDescriptionValue.value = ''
  editingDateTime.value = false
  editDateTimeValue.value = ''
  
  // 重置人脸框状态
  showFaceBoxes.value = false
  facesForPhoto.value = []
  
  try {
    const newIndex = currentPhotoIndex.value - 1
    await ensurePhotoLoaded(newIndex)
    
    const allPhotos = getAllPhotosInOrder()
    const photo = allPhotos[newIndex]
    if (photo) {
      currentPhoto.value = photo
      currentPhotoIndex.value = newIndex
      currentPhotoTags.value = photo.tags ? photo.tags.split(',').filter(t => t.trim()) : []
    }
  } catch (error) {
    console.error('View prev photo error:', error)
    photoSwitching.value = false
  }
}

const viewNextPhoto = async () => {
  if (!hasNextPhoto.value || photoSwitching.value) return
  
  photoSwitching.value = true
  
  // 重置编辑状态
  editingDescription.value = false
  editDescriptionValue.value = ''
  editingDateTime.value = false
  editDateTimeValue.value = ''
  
  // 重置人脸框状态
  showFaceBoxes.value = false
  facesForPhoto.value = []
  
  try {
    const newIndex = currentPhotoIndex.value + 1
    await ensurePhotoLoaded(newIndex)
    
    const allPhotos = getAllPhotosInOrder()
    const photo = allPhotos[newIndex]
    if (photo) {
      currentPhoto.value = photo
      currentPhotoIndex.value = newIndex
      currentPhotoTags.value = photo.tags ? photo.tags.split(',').filter(t => t.trim()) : []
    }
  } catch (error) {
    console.error('View next photo error:', error)
    photoSwitching.value = false
  }
}

// 图片加载完成/失败
const handleImageLoaded = () => {
  photoSwitching.value = false
}

const handleImageError = () => {
  photoSwitching.value = false
  MessagePlugin.error('照片加载失败')
}

// 下载原图
const downloadOriginal = () => {
  if (!currentPhoto.value) return
  
  const downloadUrl = `/cos/origin/${currentPhoto.value.name}`
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = currentPhoto.value.name
  link.click()
}

// 旋转功能
const rotating = ref(false)
const rotatePhoto = async (degrees: number) => {
  if (!currentPhoto.value || rotating.value) return
  
  rotating.value = true
  try {
    const currentRotate = currentPhoto.value.rotate || 0
    const newRotate = (currentRotate + degrees) % 360
    
    // 调用 API 更新旋转角度
    const { updateImageApi } = await import('@/api')
    const response = await updateImageApi({
      id: currentPhoto.value.id,
      rotate: newRotate
    })
    
    // 从后端响应中获取更新后的照片数据（包含新的 thumb）
    if (response.code === 0 && response.data) {
      const updatedPhoto = response.data
      
      // 更新当前照片的旋转角度和缩略图
      currentPhoto.value.rotate = updatedPhoto.rotate
      currentPhoto.value.thumb = updatedPhoto.thumb
      
      // 同步更新缓存中的照片数据
      const currentIndex = currentPhotoIndex.value
      const cacheKey = Math.floor(currentIndex / pageSize) * pageSize
      const cachePhotos = photoCache.value.get(cacheKey)
      if (cachePhotos) {
        const photoIndex = currentIndex - cacheKey
        if (cachePhotos[photoIndex]) {
          cachePhotos[photoIndex].rotate = updatedPhoto.rotate
          cachePhotos[photoIndex].thumb = updatedPhoto.thumb
        }
      }
    }
    
    MessagePlugin.success('旋转成功')
  } catch (error) {
    console.error('Rotate photo error:', error)
    MessagePlugin.error('旋转失败')
  } finally {
    rotating.value = false
  }
}

// 描述编辑功能
const editingDescription = ref(false)
const editDescriptionValue = ref('')
const savingDescription = ref(false)

const startEditDescription = () => {
  if (!currentPhoto.value) return
  editDescriptionValue.value = currentPhoto.value.description || ''
  editingDescription.value = true
}

const cancelEditDescription = () => {
  editingDescription.value = false
  editDescriptionValue.value = ''
}

const saveDescription = async () => {
  if (!currentPhoto.value || savingDescription.value) return
  
  savingDescription.value = true
  try {
    const { updateImageApi } = await import('@/api')
    const response = await updateImageApi({
      id: currentPhoto.value.id,
      description: editDescriptionValue.value
    })
    
    if (response.code === 0 && response.data) {
      const updatedPhoto = response.data
      
      // 更新当前照片的描述
      currentPhoto.value.description = updatedPhoto.description
      
      // 同步更新缓存中的照片数据
      const currentIndex = currentPhotoIndex.value
      const cacheKey = Math.floor(currentIndex / pageSize) * pageSize
      const cachePhotos = photoCache.value.get(cacheKey)
      if (cachePhotos) {
        const photoIndex = currentIndex - cacheKey
        if (cachePhotos[photoIndex]) {
          cachePhotos[photoIndex].description = updatedPhoto.description
        }
      }
      
      MessagePlugin.success('保存成功')
      editingDescription.value = false
    }
  } catch (error) {
    console.error('Save description error:', error)
    MessagePlugin.error('保存失败')
  } finally {
    savingDescription.value = false
  }
}

// 日期时间编辑功能
const editingDateTime = ref(false)
const editDateTimeValue = ref('')
const savingDateTime = ref(false)

const startEditDateTime = () => {
  if (!currentPhoto.value) return
  // 格式化为 YYYY-MM-DD HH:mm:ss 格式
  editDateTimeValue.value = formatDateTime(currentPhoto.value.taken_date)
  editingDateTime.value = true
}

const cancelEditDateTime = () => {
  editingDateTime.value = false
  editDateTimeValue.value = ''
}

const saveDateTime = async () => {
  if (!currentPhoto.value || savingDateTime.value || !editDateTimeValue.value) return
  
  savingDateTime.value = true
  try {
    const { updateImageApi } = await import('@/api')
    
    // 将本地时间转换为 ISO 8601 格式，但保持为本地时间（使用 +08:00 时区）
    const localDateString = dayjs(editDateTimeValue.value).format('YYYY-MM-DDTHH:mm:ss+08:00')
    
    const response = await updateImageApi({
      id: currentPhoto.value.id,
      taken_date: localDateString
    })
    
    if (response.code === 0 && response.data) {
      const updatedPhoto = response.data
      
      // 更新当前照片的时间
      currentPhoto.value.taken_date = updatedPhoto.taken_date
      
      // 同步更新缓存中的照片数据
      const currentIndex = currentPhotoIndex.value
      const cacheKey = Math.floor(currentIndex / pageSize) * pageSize
      const cachePhotos = photoCache.value.get(cacheKey)
      if (cachePhotos) {
        const photoIndex = currentIndex - cacheKey
        if (cachePhotos[photoIndex]) {
          cachePhotos[photoIndex].taken_date = updatedPhoto.taken_date
        }
      }
      
      MessagePlugin.success('保存成功')
      editingDateTime.value = false
    }
  } catch (error) {
    console.error('Save datetime error:', error)
    MessagePlugin.error('保存失败')
  } finally {
    savingDateTime.value = false
  }
}

// 确保指定索引的照片已加载
const ensurePhotoLoaded = async (index: number) => {
  const cacheKey = Math.floor(index / pageSize) * pageSize
  
  if (!photoCache.value.has(cacheKey)) {
    await loadPhotos(cacheKey, pageSize)
  }
}

// 处理tag变化（由父组件提供具体实现）
const handleTagChange = (value: string[]) => {
  // 这个函数会通过 slot 传递给父组件
}

// 切换人脸框显示
const toggleFaceBoxes = async () => {
  if (showFaceBoxes.value) {
    // 隐藏人脸框
    showFaceBoxes.value = false
    facesForPhoto.value = []
  } else {
    // 加载并显示人脸框
    await loadFacesForPhoto()
  }
}

// 加载照片的人脸信息
const loadFacesForPhoto = async () => {
  if (!currentPhoto.value || loadingFaces.value) return
  
  loadingFaces.value = true
  try {
    const res = await getFacesForPhotoApi({ id: currentPhoto.value.id })
    if (res.code === 0 && res.data) {
      facesForPhoto.value = res.data
      showFaceBoxes.value = true
      
      if (res.data.length === 0) {
        MessagePlugin.info('该照片未检测到人脸')
      }
    }
  } catch (error) {
    console.error('Load faces for photo error:', error)
    MessagePlugin.error('加载人脸信息失败')
  } finally {
    loadingFaces.value = false
  }
}

// 计算人脸框的样式（基于图片实际显示大小）
const getFaceBoxStyle = (face: FaceForPhoto) => {
  if (!viewerImageRef.value || !currentPhoto.value) return {}
  
  const img = viewerImageRef.value
  
  // 图片在浏览器中的实际显示尺寸（受 CSS max-width/max-height 影响）
  const imgDisplayWidth = img.offsetWidth
  const imgDisplayHeight = img.offsetHeight
  
  // /small/ 图片的实际像素尺寸（这才是后端检测人脸时使用的图片尺寸）
  const imgNaturalWidth = img.naturalWidth
  const imgNaturalHeight = img.naturalHeight
  
  // 计算缩放比例：/small/ 图片尺寸 -> 浏览器显示尺寸
  const scaleX = imgDisplayWidth / imgNaturalWidth
  const scaleY = imgDisplayHeight / imgNaturalHeight
  
  // 转换坐标和尺寸（从 /small/ 图片坐标转换为显示坐标）
  const left = face.x * scaleX
  const top = face.y * scaleY
  const width = face.width * scaleX
  const height = face.height * scaleY
  
  return {
    left: `${left}px`,
    top: `${top}px`,
    width: `${width}px`,
    height: `${height}px`
  }
}

// 键盘事件处理
const handleKeyDown = (event: KeyboardEvent) => {
  if (!viewerVisible.value || photoSwitching.value) return
  
  if (event.key === 'ArrowLeft') {
    event.preventDefault()
    viewPrevPhoto()
  } else if (event.key === 'ArrowRight') {
    event.preventDefault()
    viewNextPhoto()
  } else if (event.key === 'Escape') {
    viewerVisible.value = false
  }
}

// 查看器打开/关闭时的处理
const handleViewerOpened = () => {
  window.addEventListener('keydown', handleKeyDown)
  photoSwitching.value = false
}

const handleViewerClosed = () => {
  window.removeEventListener('keydown', handleKeyDown)
  photoSwitching.value = false
  // 重置编辑状态
  editingDescription.value = false
  editDescriptionValue.value = ''
  editingDateTime.value = false
  editDateTimeValue.value = ''
  // 重置人脸框状态
  showFaceBoxes.value = false
  facesForPhoto.value = []
  // 记录最后查看的照片ID并滚动到该位置
  if (currentPhoto.value) {
    lastViewedPhotoId.value = currentPhoto.value.id
    // 延迟一下确保DOM更新完成后再滚动
    nextTick(() => {
      scrollToPhoto(currentPhotoIndex.value)
    })
  }
}

// 滚动到指定照片位置
const scrollToPhoto = (index: number) => {
  if (!scrollContainer.value || index < 0) return
  
  const row = Math.floor(index / itemsPerRow.value)
  const targetScrollTop = row * itemHeight
  
  // 计算是否需要滚动（照片不在可视区域）
  const currentScrollTop = scrollContainer.value.scrollTop
  const visibleHeight = scrollContainer.value.clientHeight
  
  // 如果照片在可视区域外，则滚动到该位置（居中显示）
  if (targetScrollTop < currentScrollTop || targetScrollTop > currentScrollTop + visibleHeight - itemHeight) {
    scrollContainer.value.scrollTo({
      top: Math.max(0, targetScrollTop - visibleHeight / 2 + itemHeight / 2),
      behavior: 'smooth'
    })
  }
}

const formatDate = (date: string) => {
  if (!date) return ''
  // 直接格式化，不进行时区转换（假设后端返回的已经是正确的时间）
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date: string) => {
  if (!date) return ''
  // 直接格式化，不进行时区转换（假设后端返回的已经是正确的时间）
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const formatFileSize = (bytes: number) => {
  if (!bytes) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + units[i]
}

const formatDuration = (seconds: number) => {
  if (!seconds) return '0:00'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = Math.floor(seconds % 60)
  
  if (hours > 0) {
    // 大于等于1小时：1:03:04
    return `${hours}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
  } else {
    // 小于1小时：3:04 或 0:04
    return `${minutes}:${String(secs).padStart(2, '0')}`
  }
}

// 窗口大小改变时重新计算
const handleResize = () => {
  updateItemsPerRow()
  if (scrollContainer.value) {
    containerHeight.value = scrollContainer.value.clientHeight
  }
}

// 暴露方法给父组件
const refresh = () => {
  photoCache.value.clear()
  scrollTop.value = 0
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = 0
  }
  clearSelection()
  loadVisiblePhotos()
}

// 关闭查看器
const closeViewer = () => {
  viewerVisible.value = false
}

// 在查看器中切换到下一张或上一张照片（用于恢复/删除后的智能切换）
const switchPhotoInViewer = async (currentIndex: number) => {
  // 判断是否还有照片
  if (props.totalCount === 0) {
    closeViewer()
    return
  }
  
  // 重新加载照片数据
  await loadVisiblePhotos()
  
  // 获取更新后的照片列表
  const allPhotos = getAllPhotosInOrder()
  
  if (allPhotos.length === 0) {
    closeViewer()
    return
  }
  
  // 计算新的索引
  let newIndex = currentIndex
  
  // 如果当前索引超出范围，切换到上一张
  if (newIndex >= allPhotos.length) {
    newIndex = allPhotos.length - 1
  }
  
  // 确保新索引的照片已加载
  await ensurePhotoLoaded(newIndex)
  
  // 更新当前照片
  const newPhoto = allPhotos[newIndex]
  if (newPhoto) {
    currentPhoto.value = newPhoto
    currentPhotoIndex.value = newIndex
    currentPhotoTags.value = newPhoto.tags ? newPhoto.tags.split(',').filter(t => t.trim()) : []
  } else {
    closeViewer()
  }
}

defineExpose({
  refresh,
  clearSelection,
  selectedPhotos,
  photoCache,
  viewerVisible,
  currentPhoto,
  currentPhotoIndex,
  switchPhotoInViewer,
  closeViewer
})

// 监听 totalCount 变化，当从 0 变为有值时加载照片
watch(() => props.totalCount, (newCount, oldCount) => {
  if (newCount > 0 && oldCount === 0) {
    loadVisiblePhotos()
  }
})

onMounted(async () => {
  updateItemsPerRow()
  
  if (scrollContainer.value) {
    containerHeight.value = scrollContainer.value.clientHeight
  }
  
  await nextTick()
  
  // 只有在 totalCount 已经有值的情况下才加载
  if (props.totalCount > 0) {
    await loadVisiblePhotos()
  }
  
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.photo-grid-container {
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

.photo-item.photo-last-viewed .photo-wrapper {
  box-shadow: 0 0 0 3px var(--td-warning-color);
  animation: pulse-border 2s ease-in-out;
}

@keyframes pulse-border {
  0%, 100% {
    box-shadow: 0 0 0 3px var(--td-warning-color);
  }
  50% {
    box-shadow: 0 0 0 3px var(--td-warning-color), 0 0 16px var(--td-warning-color);
  }
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

.selection-checkbox {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 10;
  color: var(--td-text-color-placeholder);
  background: rgba(255, 255, 255, 0.95);
  border-radius: 50%;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
  transition: all 0.2s;
  cursor: pointer;
}

.selection-checkbox.is-checked {
  color: var(--td-brand-color);
  background: white;
}

.selection-checkbox:hover {
  transform: scale(1.1);
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

.duration-badge {
  position: absolute;
  bottom: 8px;
  right: 8px;
  z-index: 10;
  color: white;
  background: rgba(0, 0, 0, 0.75);
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 11px;
  font-weight: 500;
  line-height: 1.4;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  gap: 3px;
}

.action-menu {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 9;
  opacity: 0;
  transition: opacity 0.2s;
}

.photo-item:has(.selection-checkbox) .action-menu {
  right: 44px;
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
  position: relative;
  overflow: hidden;
  max-width: 100%;
}

.viewer-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  overflow: hidden;
  max-width: 100%;
}

.viewer-nav {
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  transform: translateY(-50%);
  pointer-events: none;
  z-index: 10;
}

.nav-button {
  pointer-events: auto;
  position: absolute;
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.nav-button:hover {
  background: white !important;
  transform: scale(1.1);
}

.nav-prev {
  left: -60px;
}

.nav-next {
  right: -60px;
}

.viewer-image {
  flex: 1;
  text-align: center;
  min-height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  min-width: 0;
  overflow: hidden;
}

.image-container {
  position: relative;
  display: inline-block;
  line-height: 0;
}

.image-wrapper {
  position: relative;
  display: inline-block;
  line-height: 0;
  transform-origin: center;
  transition: transform 0.3s ease;
}

.image-wrapper > img {
  display: block;
}

.viewer-image img {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 4px;
}

.face-boxes-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.face-box {
  position: absolute;
  border: 2px solid #0052d9;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.8), 0 0 10px rgba(0, 82, 217, 0.5);
  border-radius: 4px;
  transition: all 0.2s;
}

.face-box:hover {
  border-color: #0034b5;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.9), 0 0 15px rgba(0, 82, 217, 0.8);
}

.face-name {
  position: absolute;
  bottom: -24px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 82, 217, 0.9);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  pointer-events: auto;
  backdrop-filter: blur(4px);
}

.rotate-buttons {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
  background: rgba(255, 255, 255, 0.95);
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 5;
}

.rotate-buttons .t-button {
  width: 40px;
  height: 40px;
}

.rotate-buttons .t-button:hover {
  background-color: var(--td-brand-color-light);
  border-color: var(--td-brand-color);
}

.video-player {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 4px;
  outline: none;
}

.viewer-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.viewer-info {
  width: 300px;
  max-width: 25vw;
  min-width: 200px;
  flex-shrink: 0;
  max-height: 70vh;
  overflow-y: auto;
  overflow-x: hidden;
  word-wrap: break-word;
  word-break: break-all;
}

.viewer-info :deep(.t-descriptions__label) {
  width: 60px;
  min-width: 60px;
}

.viewer-actions {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--td-component-border);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-tag {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
}

.description-display {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.description-text {
  flex: 1;
  white-space: pre-wrap;
  word-break: break-word;
}

.empty-description {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
}

.description-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
