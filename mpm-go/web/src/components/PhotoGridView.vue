<template>
  <PhotoGrid
    ref="photoGridRef"
    :load-photos-api="loadPhotosApi"
    :total-count="totalCount"
    :trashed="trashed"
    :empty-text="emptyText"
  >
    <!-- 过滤器插槽 -->
    <template #filters>
      <slot name="filters"></slot>
      
      <t-button
        v-if="showStarFilter"
        :theme="filters.star ? 'primary' : 'default'"
        :variant="filters.star ? 'base' : 'outline'"
        @click="toggleFilter('star')"
      >
        <template #icon><t-icon name="star" /></template>
        收藏
      </t-button>
      
      <t-button
        v-if="showVideoFilter"
        :theme="filters.video ? 'primary' : 'default'"
        :variant="filters.video ? 'base' : 'outline'"
        @click="toggleFilter('video')"
      >
        <template #icon><t-icon name="video" /></template>
        视频
      </t-button>
      
      <t-select
        v-if="showOrderFilter"
        v-model="filters.order"
        placeholder="排序"
        style="width: 150px"
        :options="orderOptions"
        @change="refresh"
      />
      
      <t-select
        v-if="showTagFilter"
        v-model="filters.tag"
        placeholder="选择标签"
        clearable
        filterable
        style="width: 200px"
        :options="tagOptions"
        @change="refresh"
      >
        <template #prefixIcon>
          <t-icon name="discount" />
        </template>
      </t-select>
    </template>
    
    <!-- 照片操作插槽 -->
    <template #photo-actions="{ photo }">
      <t-dropdown-item v-if="!trashed" @click="toggleStar(photo)">
        <template #prefix-icon>
          <t-icon :name="photo.star ? 'star-filled' : 'star'" />
        </template>
        {{ photo.star ? '取消收藏' : '收藏' }}
      </t-dropdown-item>
      
      <t-dropdown-item v-if="showActivityAction && !trashed" @click="showActivityDialog(photo)">
        <template #prefix-icon>
          <t-icon name="calendar" />
        </template>
        {{ photo.activity ? '修改活动' : '添加到活动' }}
      </t-dropdown-item>
      
      <slot name="extra-photo-actions" :photo="photo"></slot>
      
      <t-dropdown-item v-if="trashed" @click="restorePhoto(photo)">
        <template #prefix-icon>
          <t-icon name="rollback" />
        </template>
        恢复
      </t-dropdown-item>
      
      <t-dropdown-item v-if="!trashed" theme="error" @click="trashPhoto(photo)">
        <template #prefix-icon>
          <t-icon name="delete" />
        </template>
        删除
      </t-dropdown-item>
    </template>
    
    <!-- 标签编辑器插槽 -->
    <template #tag-editor="{ photo }">
      <t-select
        v-if="!trashed"
        v-model="currentPhotoTags"
        placeholder="选择或输入标签"
        multiple
        filterable
        creatable
        clearable
        :options="tagOptions"
        @change="handleTagChange"
      />
    </template>
    
    <!-- 批量操作插槽 -->
    <template #batch-actions="{ selectedPhotos, selectedPhotosList }">
      <t-dropdown v-if="!trashed" trigger="click">
        <t-button size="small" variant="outline">
          批量操作
          <t-icon name="chevron-down" />
        </t-button>
        <t-dropdown-menu>
          <t-dropdown-item @click="batchToggleStar(selectedPhotosList)">
            <template #prefix-icon>
              <t-icon name="star" />
            </template>
            批量收藏/取消
          </t-dropdown-item>
          <t-dropdown-item v-if="showActivityAction" @click="batchAddToActivity(selectedPhotosList)">
            <template #prefix-icon>
              <t-icon name="calendar" />
            </template>
            批量添加到活动
          </t-dropdown-item>
          <t-dropdown-item theme="error" @click="batchTrash(selectedPhotosList)">
            <template #prefix-icon>
              <t-icon name="delete" />
            </template>
            批量删除
          </t-dropdown-item>
        </t-dropdown-menu>
      </t-dropdown>
      
      <t-button v-if="trashed" size="small" theme="primary" @click="batchRestore(selectedPhotosList)">
        <template #icon><t-icon name="rollback" /></template>
        批量恢复
      </t-button>
    </template>
    
    <!-- 额外操作按钮插槽 -->
    <template #extra-actions>
      <slot name="extra-actions"></slot>
    </template>
    
    <!-- 查看器操作插槽 -->
    <template #viewer-actions="{ photo }">
      <t-space direction="vertical" style="width: 100%">
        <t-button v-if="!trashed" theme="primary" block @click="toggleStarInViewer(photo)">
          <template #icon>
            <t-icon :name="photo.star ? 'star-filled' : 'star'" />
          </template>
          {{ photo.star ? '取消收藏' : '收藏' }}
        </t-button>
        
        <t-button v-if="showActivityAction && !trashed" block @click="showActivityDialogInViewer(photo)">
          <template #icon><t-icon name="calendar" /></template>
          {{ photo.activity ? '修改活动' : '添加到活动' }}
        </t-button>
        
        <slot name="extra-viewer-actions" :photo="photo"></slot>
        
        <t-button v-if="trashed" theme="primary" block @click="restorePhoto(photo)">
          <template #icon><t-icon name="rollback" /></template>
          恢复照片
        </t-button>
        
        <t-button v-if="!trashed" theme="danger" block @click="deletePhotoInViewer(photo)">
          <template #icon><t-icon name="delete" /></template>
          删除
        </t-button>
      </t-space>
    </template>
  </PhotoGrid>
  
  <!-- 活动选择对话框 -->
  <t-dialog
    v-if="showActivityAction"
    v-model:visible="activityDialogVisible"
    header="选择活动"
    width="500px"
    :on-confirm="confirmAddToActivity"
  >
    <t-select
      v-model="selectedActivityId"
      placeholder="选择活动"
      clearable
      filterable
      style="width: 100%"
    >
      <t-option
        v-for="activity in activities"
        :key="activity.id"
        :value="activity.id"
        :label="activity.name"
      />
    </t-select>
  </t-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import {
  getPicsApi,
  getAllTagsApi,
  getActivitiesApi,
  updateImageApi,
  trashPhotosApi,
  Photo,
  Activity,
  GetPicsParams
} from '@/api'
import PhotoGrid from './PhotoGrid.vue'

interface Props {
  // 数据源相关
  loadPhotosParams?: Partial<GetPicsParams>  // 额外的查询参数（如 dateKey, path, faceId）
  totalCount: number
  trashed?: boolean
  emptyText?: string
  
  // 功能开关
  showStarFilter?: boolean
  showVideoFilter?: boolean
  showOrderFilter?: boolean
  showTagFilter?: boolean
  showActivityAction?: boolean
  
  // 默认排序
  defaultOrder?: string
}

const props = withDefaults(defineProps<Props>(), {
  trashed: false,
  emptyText: '暂无照片',
  showStarFilter: true,
  showVideoFilter: true,
  showOrderFilter: true,
  showTagFilter: true,
  showActivityAction: true,
  defaultOrder: '-taken_date'
})

const emit = defineEmits<{
  'star-filter-change': [value: boolean]
  'refresh-needed': []
  'total-count-change': [count: number]
}>()

const photoGridRef = ref<InstanceType<typeof PhotoGrid> | null>(null)
const allTags = ref<string[]>([])
const currentPhotoTags = ref<string[]>([])
const activities = ref<Activity[]>([])
const activityDialogVisible = ref(false)
const selectedActivityId = ref<number | null>(null)
const pendingPhotos = ref<Photo[]>([])

const filters = reactive({
  star: false,
  video: false,
  order: props.defaultOrder,
  tag: ''
})

// 排序选项
const orderOptions = [
  { label: 'ID 升序', value: 'id' },
  { label: 'ID 降序', value: '-id' },
  { label: '日期 升序', value: 'taken_date' },
  { label: '日期 降序', value: '-taken_date' },
  { label: '大小 升序', value: 'size' },
  { label: '大小 降序', value: '-size' },
  { label: '宽度 升序', value: 'width' },
  { label: '宽度 降序', value: '-width' },
  { label: '高度 升序', value: 'height' },
  { label: '高度 降序', value: '-height' }
]

// 标签选项
const tagOptions = computed(() => {
  return allTags.value.map(tag => ({
    label: tag,
    value: tag
  }))
})

// 切换过滤器
const toggleFilter = (key: 'star' | 'video') => {
  filters[key] = !filters[key]
  
  // 通知父组件星标过滤变化
  if (key === 'star') {
    emit('star-filter-change', filters.star)
  }
  
  refresh()
}

// 加载照片的 API 函数
const loadPhotosApi = async (start: number, size: number): Promise<Photo[]> => {
  try {
    const params: GetPicsParams = {
      trashed: props.trashed,
      start,
      size,
      order: filters.order,
      star: filters.star || undefined,
      video: filters.video || undefined,
      tag: filters.tag || undefined,
      ...props.loadPhotosParams
    }
    
    const res = await getPicsApi(params)
    
    if (res.code === 0) {
      return res.data.data
    }
    return []
  } catch (error) {
    console.error('Load photos error:', error)
    return []
  }
}

// 加载所有标签
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

// 加载所有活动
const loadActivities = async () => {
  if (!props.showActivityAction) return
  
  try {
    const res = await getActivitiesApi()
    if (res.code === 0) {
      activities.value = res.data
    }
  } catch (error) {
    console.error('Load activities error:', error)
  }
}

// 刷新
const refresh = () => {
  photoGridRef.value?.refresh()
}

// 切换收藏状态
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
    MessagePlugin.error('操作失败')
  }
}

// 在查看器中切换收藏
const toggleStarInViewer = async (photo: Photo) => {
  try {
    await updateImageApi({
      id: photo.id,
      star: !photo.star
    })
    
    photo.star = !photo.star
    
    // 更新缓存中的照片数据
    if (photoGridRef.value) {
      const currentIndex = photoGridRef.value.currentPhotoIndex
      const cacheKey = Math.floor(currentIndex / 100) * 100
      const cachePhotos = photoGridRef.value.photoCache.get(cacheKey)
      if (cachePhotos) {
        const photoIndex = currentIndex - cacheKey
        if (cachePhotos[photoIndex]) {
          cachePhotos[photoIndex].star = photo.star
        }
      }
    }
    
    MessagePlugin.success(photo.star ? '已收藏' : '已取消收藏')
  } catch (error) {
    console.error('Toggle star error:', error)
    MessagePlugin.error('操作失败')
  }
}

// 删除/恢复照片
const trashPhoto = async (photo: Photo) => {
  try {
    await trashPhotosApi([photo.id])
    MessagePlugin.success(props.trashed ? '已恢复' : '已移至回收站')
    
    emit('total-count-change', -1)
    refresh()
  } catch (error) {
    console.error('Trash photo error:', error)
    MessagePlugin.error(props.trashed ? '恢复失败' : '删除失败')
  }
}

// 恢复照片（回收站专用）
const restorePhoto = async (photo: Photo) => {
  if (!photoGridRef.value) return
  
  try {
    const isViewerOpen = photoGridRef.value.viewerVisible
    const currentIndex = photoGridRef.value.currentPhotoIndex
    
    await trashPhotosApi([photo.id])
    MessagePlugin.success('已恢复')
    
    emit('total-count-change', -1)
    
    if (isViewerOpen) {
      photoGridRef.value.photoCache.clear()
      await new Promise(resolve => setTimeout(resolve, 100))
      await photoGridRef.value.switchPhotoInViewer(currentIndex)
    } else {
      refresh()
    }
  } catch (error) {
    console.error('Restore photo error:', error)
    MessagePlugin.error('恢复失败')
  }
}

// 在查看器中删除照片
const deletePhotoInViewer = async (photo: Photo) => {
  try {
    const currentIndex = photoGridRef.value?.currentPhotoIndex || 0
    
    await trashPhotosApi([photo.id])
    MessagePlugin.success(props.trashed ? '已恢复' : '已移至回收站')
    
    emit('total-count-change', -1)
    
    if (photoGridRef.value) {
      photoGridRef.value.photoCache.clear()
    }
    
    if (photoGridRef.value) {
      await photoGridRef.value.switchPhotoInViewer(currentIndex)
    }
  } catch (error) {
    console.error('Delete photo error:', error)
    MessagePlugin.error(props.trashed ? '恢复失败' : '删除失败')
  }
}

// 显示活动选择对话框
const showActivityDialog = (photo: Photo) => {
  pendingPhotos.value = [photo]
  selectedActivityId.value = photo.activity || null
  activityDialogVisible.value = true
}

const showActivityDialogInViewer = (photo: Photo) => {
  showActivityDialog(photo)
}

// 确认添加到活动
const confirmAddToActivity = async () => {
  if (!selectedActivityId.value && selectedActivityId.value !== 0) {
    MessagePlugin.warning('请选择活动')
    return
  }
  
  try {
    for (const photo of pendingPhotos.value) {
      await updateImageApi({
        id: photo.id,
        activity: selectedActivityId.value
      })
      photo.activity = selectedActivityId.value
    }
    
    MessagePlugin.success('已添加到活动')
    activityDialogVisible.value = false
    refresh()
  } catch (error) {
    console.error('Add to activity error:', error)
    MessagePlugin.error('操作失败')
  }
}

// 批量添加到活动
const batchAddToActivity = (photos: Photo[]) => {
  pendingPhotos.value = photos
  selectedActivityId.value = null
  activityDialogVisible.value = true
}

// 批量切换收藏
const batchToggleStar = async (photos: Photo[]) => {
  try {
    const allStarred = photos.every(p => p.star)
    const newStarValue = !allStarred
    
    for (const photo of photos) {
      await updateImageApi({
        id: photo.id,
        star: newStarValue
      })
      photo.star = newStarValue
    }
    
    MessagePlugin.success(newStarValue ? '已批量收藏' : '已批量取消收藏')
    refresh()
  } catch (error) {
    console.error('Batch toggle star error:', error)
    MessagePlugin.error('操作失败')
  }
}

// 批量删除/恢复
const batchTrash = async (photos: Photo[]) => {
  try {
    const ids = photos.map(p => p.id)
    await trashPhotosApi(ids)
    MessagePlugin.success(props.trashed 
      ? `已恢复 ${ids.length} 张照片`
      : `已将 ${ids.length} 张照片移至回收站`)
    
    emit('total-count-change', -ids.length)
    photoGridRef.value?.clearSelection()
    refresh()
  } catch (error) {
    console.error('Batch trash error:', error)
    MessagePlugin.error(props.trashed ? '批量恢复失败' : '批量删除失败')
  }
}

// 批量恢复（回收站专用）
const batchRestore = async (photos: Photo[]) => {
  await batchTrash(photos)
}

// 处理标签变化
const handleTagChange = async (value: string[]) => {
  if (!photoGridRef.value?.currentPhoto) return
  
  try {
    const tagString = value.filter(t => t.trim()).join(',')
    
    await updateImageApi({
      id: photoGridRef.value.currentPhoto.id,
      tags: tagString
    })
    
    photoGridRef.value.currentPhoto.tags = tagString
    
    const currentIndex = photoGridRef.value.currentPhotoIndex
    const cacheKey = Math.floor(currentIndex / 100) * 100
    const cachePhotos = photoGridRef.value.photoCache.get(cacheKey)
    if (cachePhotos) {
      const photoIndex = currentIndex - cacheKey
      if (cachePhotos[photoIndex]) {
        cachePhotos[photoIndex].tags = tagString
      }
    }
    
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

// 监听 loadPhotosParams 变化
watch(() => props.loadPhotosParams, () => {
  refresh()
}, { deep: true })

// 暴露方法给父组件
defineExpose({
  refresh,
  clearSelection: () => photoGridRef.value?.clearSelection()
})

onMounted(async () => {
  await Promise.all([loadAllTags(), loadActivities()])
})
</script>
