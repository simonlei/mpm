<template>
  <div class="photos-container">
    <!-- 左侧时间线树 -->
    <div class="sidebar">
      <DateTree
        ref="dateTreeRef"
        :star="filters.star"
        :trashed="false"
        @select="handleDateSelect"
      />
    </div>
    
    <!-- 右侧主内容区 -->
    <div class="main-content">
      <!-- 过滤器 -->
      <div class="filter-bar">
      <t-space>
        <!-- 时间筛选提示 -->
        <t-tag
          v-if="filters.dateKey !== null"
          theme="primary"
          variant="light"
          closable
          @close="handleDateSelect(null)"
        >
          <template #icon><t-icon name="time" /></template>
          {{ getDateKeyLabel(filters.dateKey) }}
        </t-tag>
        
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
        
        <t-select
          v-model="searchTag"
          placeholder="选择标签"
          clearable
          filterable
          style="width: 200px"
          @change="resetAndLoad"
        >
          <template #prefix-icon>
            <t-icon name="discount" />
          </template>
          <t-option
            v-for="tag in allTags"
            :key="tag"
            :value="tag"
            :label="tag"
          />
        </t-select>
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
          :theme="selectionMode ? 'primary' : 'default'"
          :variant="selectionMode ? 'base' : 'outline'"
          size="small"
          @click="toggleSelectionMode"
        >
          <template #icon><t-icon :name="selectionMode ? 'check-circle-filled' : 'check-circle'" /></template>
          {{ selectionMode ? '退出选择' : '选择' }}
        </t-button>
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
              <!-- 选中标记（选择模式下才显示勾选图标） -->
              <div v-if="selectionMode && selectedPhotos.has(photo.id)" class="selection-badge">
                <t-icon name="check-circle-filled" size="24px" />
              </div>
              
              <!-- 选择框（选择模式下始终显示） -->
              <div v-if="selectionMode" class="selection-checkbox" :class="{ 'is-checked': selectedPhotos.has(photo.id) }">
                <t-icon :name="selectedPhotos.has(photo.id) ? 'check-circle-filled' : 'circle'" size="20px" />
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
      :header="currentPhotoHeader"
      width="90%"
      :footer="false"
      placement="center"
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
          <!-- 左侧图片 -->
          <div class="viewer-image">
            <!-- 加载中遮罩 -->
            <div v-if="photoSwitching" class="viewer-loading">
              <t-loading size="large" text="加载中..." />
            </div>
            
            <img
              v-show="!photoSwitching"
              :key="currentPhoto.id"
              :src="`/cos/small/${currentPhoto.name}`"
              :alt="currentPhoto.name"
              @load="handleImageLoaded"
              @error="handleImageError"
            />
          </div>
          
          <!-- 右侧信息 -->
          <div class="viewer-info">
            <t-descriptions :column="1" bordered>
              <t-descriptions-item label="大小">
                {{ formatFileSize(currentPhoto.size) }}
              </t-descriptions-item>
              <t-descriptions-item label="宽度">
                {{ currentPhoto.width }} px
              </t-descriptions-item>
              <t-descriptions-item label="高度">
                {{ currentPhoto.height }} px
              </t-descriptions-item>
              <t-descriptions-item v-if="currentPhoto.description" label="描述">
                {{ currentPhoto.description }}
              </t-descriptions-item>
              <t-descriptions-item label="时间">
                {{ formatDateTime(currentPhoto.takenDate) }}
              </t-descriptions-item>
              <t-descriptions-item label="标签">
                <t-select
                  v-model="currentPhotoTags"
                  placeholder="选择或输入标签"
                  multiple
                  filterable
                  creatable
                  clearable
                  @change="handleTagChange"
                >
                  <t-option
                    v-for="tag in allTags"
                    :key="tag"
                    :value="tag"
                    :label="tag"
                  />
                </t-select>
              </t-descriptions-item>
              <t-descriptions-item v-if="currentPhoto.address" label="地址">
                {{ currentPhoto.address }}
              </t-descriptions-item>
              <t-descriptions-item v-if="currentPhoto.activityDesc" label="所属活动">
                {{ currentPhoto.activityDesc }}
              </t-descriptions-item>
            </t-descriptions>
            
            <!-- 操作按钮 -->
            <div class="viewer-actions">
              <t-space direction="vertical" style="width: 100%">
                <t-button theme="primary" block @click="toggleStarInViewer">
                  <template #icon>
                    <t-icon :name="currentPhoto.star ? 'star-filled' : 'star'" />
                  </template>
                  {{ currentPhoto.star ? '取消收藏' : '收藏' }}
                </t-button>
                
                <t-button theme="danger" block @click="deleteCurrentPhoto">
                  <template #icon><t-icon name="delete" /></template>
                  删除
                </t-button>
              </t-space>
            </div>
          </div>
        </div>
      </div>
    </t-dialog>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { getPicsApi, updateImageApi, trashPhotosApi, getCountApi, getAllTagsApi, Photo } from '@/api'
import DateTree from '@/components/DateTree.vue'
import dayjs from 'dayjs'

const loading = ref(false)
const scrollContainer = ref<HTMLElement | null>(null)
const totalCount = ref(0)
const searchTag = ref('')
const allTags = ref<string[]>([])
const dateTreeRef = ref<InstanceType<typeof DateTree> | null>(null)

const filters = reactive({
  star: false,
  video: false,
  order: '-taken_date' as string,
  dateKey: null as string | null
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
const currentPhotoIndex = ref<number>(-1)
const photoSwitching = ref(false) // 照片切换中状态
const currentPhotoTags = ref<string[]>([]) // 当前照片的tags数组

// 多选功能
const selectedPhotos = ref<Set<number>>(new Set())
const lastSelectedIndex = ref<number>(-1)
const selectionMode = ref(false) // 选择模式开关

// 计算当前照片的标题（显示序号）
const currentPhotoHeader = computed(() => {
  if (!currentPhoto.value) return ''
  const index = currentPhotoIndex.value
  if (index >= 0 && totalCount.value > 0) {
    return `${currentPhoto.value.name} (${index + 1} / ${totalCount.value})`
  }
  return currentPhoto.value.name
})

// 是否有上一张
const hasPrevPhoto = computed(() => {
  return currentPhotoIndex.value > 0
})

// 是否有下一张
const hasNextPhoto = computed(() => {
  return currentPhotoIndex.value < totalCount.value - 1
})

// 清除选择
const clearSelection = () => {
  selectedPhotos.value.clear()
  lastSelectedIndex.value = -1
}

// 切换选择模式
const toggleSelectionMode = () => {
  selectionMode.value = !selectionMode.value
  
  // 退出选择模式时清除选择
  if (!selectionMode.value) {
    clearSelection()
  }
}

// 切换过滤器，重置列表
const toggleFilter = (key: 'star' | 'video') => {
  filters[key] = !filters[key]
  clearSelection()
  // 清除时间线选择
  dateTreeRef.value?.clearSelection()
  filters.dateKey = null
  resetAndLoad()
}

// 处理时间线选择
const handleDateSelect = (nodeId: number | null) => {
  if (nodeId === null) {
    filters.dateKey = null
  } else if (nodeId >= 1000000) {
    // 活动节点: ID = 1000000 + activityId
    filters.dateKey = String(nodeId)
  } else if (nodeId >= 100) {
    // 月份节点: ID = year * 100 + month
    const year = Math.floor(nodeId / 100)
    const month = nodeId % 100
    filters.dateKey = `${year}${String(month).padStart(2, '0')}`
  } else {
    // 年份节点: ID = year
    filters.dateKey = String(nodeId)
  }
  
  clearSelection()
  resetAndLoad()
}

// 获取 dateKey 的显示标签
const getDateKeyLabel = (dateKey: string) => {
  const nodeId = parseInt(dateKey)
  
  if (nodeId >= 1000000) {
    return '活动筛选'
  } else if (nodeId >= 100) {
    const year = Math.floor(nodeId / 100)
    const month = nodeId % 100
    return `${year} 年 ${month} 月`
  } else {
    return `${nodeId} 年`
  }
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
    // 构建 dateKey 参数
    const dateKey = filters.dateKey || undefined
    
    const res = await getPicsApi({
      star: filters.star || undefined,
      video: filters.video || undefined,
      trashed: false,
      start,
      size,
      order: filters.order,
      tag: searchTag.value || undefined,
      dateKey: dateKey
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

// 滚动到指定照片位置
const scrollToPhoto = async (photoIndex: number) => {
  if (!scrollContainer.value) return
  
  // 确保该照片的数据已加载
  await ensurePhotoLoaded(photoIndex)
  
  // 计算照片所在的行
  const row = Math.floor(photoIndex / itemsPerRow.value)
  
  // 计算滚动位置（居中显示）
  const targetScrollTop = row * itemHeight - containerHeight.value / 2 + itemHeight / 2
  
  // 滚动到目标位置
  scrollContainer.value.scrollTo({
    top: Math.max(0, targetScrollTop),
    behavior: 'smooth'
  })
  
  // 等待滚动和渲染完成
  await nextTick()
  await new Promise(resolve => setTimeout(resolve, 300))
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

const loadAllTags = async () => {
  try {
    const res = await getAllTagsApi()
    if (res.code === 0) {
      allTags.value = res.data
    }
  } catch (error) {
    console.error('Load tags error:', error)
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

// 处理tag变化
const handleTagChange = async (value: string[]) => {
  if (!currentPhoto.value) return
  
  try {
    // 将数组转换为逗号分隔的字符串
    const tagString = value.filter(t => t.trim()).join(',')
    
    await updateImageApi({
      id: currentPhoto.value.id,
      tags: tagString
    })
    
    // 更新当前照片对象的tags
    currentPhoto.value.tags = tagString
    
    // 同步更新缓存中的照片数据
    const cacheKey = Math.floor(currentPhotoIndex.value / pageSize) * pageSize
    const cachePhotos = photoCache.value.get(cacheKey)
    if (cachePhotos) {
      const photoIndex = currentPhotoIndex.value - cacheKey
      if (cachePhotos[photoIndex]) {
        cachePhotos[photoIndex].tags = tagString
      }
    }
    
    // 如果有新tag不在列表中，添加到标签列表
    value.forEach(tag => {
      if (tag && !allTags.value.includes(tag)) {
        allTags.value.push(tag)
      }
    })
    
    MessagePlugin.success('标签已更新')
  } catch (error) {
    console.error('Update tag error:', error)
    MessagePlugin.error('标签更新失败')
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

// 在查看器中切换收藏状态
const toggleStarInViewer = async () => {
  if (!currentPhoto.value) return
  
  try {
    await updateImageApi({
      id: currentPhoto.value.id,
      star: !currentPhoto.value.star
    })
    
    currentPhoto.value.star = !currentPhoto.value.star
    
    // 同步更新缓存中的照片数据
    const cacheKey = Math.floor(currentPhotoIndex.value / pageSize) * pageSize
    const cachePhotos = photoCache.value.get(cacheKey)
    if (cachePhotos) {
      const photoIndex = currentPhotoIndex.value - cacheKey
      if (cachePhotos[photoIndex]) {
        cachePhotos[photoIndex].star = currentPhoto.value.star
      }
    }
    
    MessagePlugin.success(currentPhoto.value.star ? '已收藏' : '已取消收藏')
  } catch (error) {
    console.error('Toggle star error:', error)
    MessagePlugin.error('操作失败')
  }
}

// 删除当前查看的照片
const deleteCurrentPhoto = async () => {
  if (!currentPhoto.value) return
  
  try {
    const photoName = currentPhoto.value.name
    const currentIndex = currentPhotoIndex.value
    
    // 删除照片
    await trashPhotosApi([photoName])
    MessagePlugin.success('已移至回收站')
    
    // 减少总数
    totalCount.value--
    
    // 清除缓存
    photoCache.value.clear()
    
    // 判断是否还有照片
    if (totalCount.value === 0) {
      // 没有照片了，关闭查看器
      viewerVisible.value = false
      await loadVisiblePhotos()
      return
    }
    
    // 重新加载照片数据
    await loadVisiblePhotos()
    
    // 等待数据加载完成
    await nextTick()
    
    // 获取更新后的照片列表
    const allPhotos = getAllPhotosInOrder()
    
    if (allPhotos.length === 0) {
      // 没有照片了，关闭查看器
      viewerVisible.value = false
      return
    }
    
    // 切换到下一张照片
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
      // 如果找不到照片，关闭查看器
      viewerVisible.value = false
    }
  } catch (error) {
    console.error('Delete photo error:', error)
    MessagePlugin.error('删除失败')
  }
}

// 处理照片点击
const handlePhotoClick = (photo: Photo, event: MouseEvent) => {
  // 选择模式下
  if (selectionMode.value) {
    // 如果按住 Ctrl/Cmd 键，单独选择/取消选择
    if (event.ctrlKey || event.metaKey) {
      toggleSelection(photo)
    }
    // 如果按住 Shift 键，连续选择
    else if (event.shiftKey && lastSelectedIndex.value >= 0) {
      selectRange(photo)
    }
    // 普通点击，切换选中状态
    else {
      toggleSelection(photo)
    }
  }
  // 非选择模式下
  else {
    // 普通点击查看照片
    viewPhoto(photo)
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
  // 将逗号分隔的tag字符串转换为数组
  currentPhotoTags.value = photo.tags ? photo.tags.split(',').filter(t => t.trim()) : []
  
  // 计算当前照片在所有照片中的索引
  const allPhotos = getAllPhotosInOrder()
  currentPhotoIndex.value = allPhotos.findIndex(p => p.id === photo.id)
  
  viewerVisible.value = true
}

// 查看上一张照片
const viewPrevPhoto = async () => {
  if (!hasPrevPhoto.value || photoSwitching.value) return
  
  photoSwitching.value = true
  
  try {
    const newIndex = currentPhotoIndex.value - 1
    await ensurePhotoLoaded(newIndex)
    
    const allPhotos = getAllPhotosInOrder()
    const photo = allPhotos[newIndex]
    if (photo) {
      currentPhoto.value = photo
      currentPhotoIndex.value = newIndex
      // 将逗号分隔的tag字符串转换为数组
      currentPhotoTags.value = photo.tags ? photo.tags.split(',').filter(t => t.trim()) : []
    }
  } catch (error) {
    console.error('View prev photo error:', error)
    photoSwitching.value = false
  }
}

// 查看下一张照片
const viewNextPhoto = async () => {
  if (!hasNextPhoto.value || photoSwitching.value) return
  
  photoSwitching.value = true
  
  try {
    const newIndex = currentPhotoIndex.value + 1
    await ensurePhotoLoaded(newIndex)
    
    const allPhotos = getAllPhotosInOrder()
    const photo = allPhotos[newIndex]
    if (photo) {
      currentPhoto.value = photo
      currentPhotoIndex.value = newIndex
      // 将逗号分隔的tag字符串转换为数组
      currentPhotoTags.value = photo.tags ? photo.tags.split(',').filter(t => t.trim()) : []
    }
  } catch (error) {
    console.error('View next photo error:', error)
    photoSwitching.value = false
  }
}

// 图片加载完成
const handleImageLoaded = () => {
  photoSwitching.value = false
}

// 图片加载失败
const handleImageError = () => {
  photoSwitching.value = false
  MessagePlugin.error('照片加载失败')
}

// 确保指定索引的照片已加载
const ensurePhotoLoaded = async (index: number) => {
  const cacheKey = Math.floor(index / pageSize) * pageSize
  
  // 如果该分页未加载，先加载
  if (!photoCache.value.has(cacheKey)) {
    await loadPhotos(cacheKey, pageSize)
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

// 查看器打开时添加键盘监听
const handleViewerOpened = () => {
  window.addEventListener('keydown', handleKeyDown)
  photoSwitching.value = false // 重置加载状态
}

// 查看器关闭时移除键盘监听
const handleViewerClosed = async () => {
  window.removeEventListener('keydown', handleKeyDown)
  photoSwitching.value = false
  
  // 如果有当前照片，滚动到该位置并选中
  if (currentPhoto.value && currentPhotoIndex.value >= 0) {
    // 清空之前的选择
    selectedPhotos.value.clear()
    
    // 选中当前照片（只显示边框高亮）
    selectedPhotos.value.add(currentPhoto.value.id)
    lastSelectedIndex.value = currentPhotoIndex.value
    
    // 滚动到照片位置
    await scrollToPhoto(currentPhotoIndex.value)
  }
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const formatDateTime = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const formatFileSize = (bytes: number) => {
  if (!bytes) return '0 B'
  
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const k = 1024
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return (bytes / Math.pow(k, i)).toFixed(2) + ' ' + units[i]
}

// 窗口大小改变时重新计算
const handleResize = () => {
  updateItemsPerRow()
  if (scrollContainer.value) {
    containerHeight.value = scrollContainer.value.clientHeight
  }
}

onMounted(async () => {
  await Promise.all([loadCount(), loadAllTags()])
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
  gap: 16px;
}

.sidebar {
  width: 260px;
  flex-shrink: 0;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
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

.action-menu {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 9;
  opacity: 0;
  transition: opacity 0.2s;
}

/* 选择模式下，将操作菜单向左移动以避免与选择框重叠 */
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

.viewer-image img {
  max-width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 4px;
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
}
</style>
