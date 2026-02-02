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
      <PhotoGrid
        ref="photoGridRef"
        :load-photos-api="loadPhotosApi"
        :total-count="totalCount"
        :empty-text="emptyText"
      >
        <!-- 过滤器插槽 -->
        <template #filters>
          <!-- 时间筛选提示 -->
          <t-tag
            v-if="filters.date_key !== null"
            theme="primary"
            variant="light"
            closable
            @close="handleDateSelect(null)"
          >
            <template #icon><t-icon name="time" /></template>
            {{ getDateKeyLabel(filters.date_key) }}
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
            :options="orderOptions"
            @change="resetAndLoad"
          />
          
          <t-select
            v-model="searchTag"
            placeholder="选择标签"
            clearable
            filterable
            style="width: 200px"
            :options="tagOptions"
            @change="resetAndLoad"
          >
            <template #prefixIcon>
              <t-icon name="discount" />
            </template>
          </t-select>
        </template>
        
        <!-- 照片操作插槽 -->
        <template #photo-actions="{ photo }">
          <t-dropdown-item @click="toggleStar(photo)">
            <template #prefix-icon>
              <t-icon :name="photo.star ? 'star-filled' : 'star'" />
            </template>
            {{ photo.star ? '取消收藏' : '收藏' }}
          </t-dropdown-item>
          
          <t-dropdown-item @click="showActivityDialog(photo)">
            <template #prefix-icon>
              <t-icon name="calendar" />
            </template>
            {{ photo.activity ? '修改活动' : '添加到活动' }}
          </t-dropdown-item>
          
          <t-dropdown-item theme="error" @click="trashPhoto(photo)">
            <template #prefix-icon>
              <t-icon name="delete" />
            </template>
            删除
          </t-dropdown-item>
        </template>
        
        <!-- 标签编辑器插槽 -->
        <template #tag-editor="{ photo, tags, onTagChange }">
          <t-select
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
          <t-dropdown trigger="click">
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
              <t-dropdown-item @click="batchAddToActivity(selectedPhotosList)">
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
        </template>
        
        <!-- 查看器操作插槽 -->
        <template #viewer-actions="{ photo }">
          <t-space direction="vertical" style="width: 100%">
            <t-button theme="primary" block @click="toggleStarInViewer(photo)">
              <template #icon>
                <t-icon :name="photo.star ? 'star-filled' : 'star'" />
              </template>
              {{ photo.star ? '取消收藏' : '收藏' }}
            </t-button>
            
            <t-button block @click="showActivityDialogInViewer(photo)">
              <template #icon><t-icon name="calendar" /></template>
              {{ photo.activity ? '修改活动' : '添加到活动' }}
            </t-button>
            
            <t-button theme="danger" block @click="deletePhotoInViewer(photo)">
              <template #icon><t-icon name="delete" /></template>
              删除
            </t-button>
          </t-space>
        </template>
      </PhotoGrid>
    </div>
    
    <!-- 活动选择对话框 -->
    <t-dialog
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { 
  getPicsApi, 
  getCountApi, 
  getAllTagsApi,
  getActivitiesApi,
  updateImageApi, 
  trashPhotosApi, 
  Photo,
  Activity
} from '@/api'
import DateTree from '@/components/DateTree.vue'
import PhotoGrid from '@/components/PhotoGrid.vue'

const photoGridRef = ref<InstanceType<typeof PhotoGrid> | null>(null)
const dateTreeRef = ref<InstanceType<typeof DateTree> | null>(null)

const totalCount = ref(0)
const searchTag = ref('')
const allTags = ref<string[]>([])
const currentPhotoTags = ref<string[]>([])
const activities = ref<Activity[]>([])
const activityDialogVisible = ref(false)
const selectedActivityId = ref<number | null>(null)
const pendingPhotos = ref<Photo[]>([])

const filters = reactive({
  star: false,
  video: false,
  order: '-taken_date' as string,
  date_key: null as number | null
})

const emptyText = computed(() => {
  if (filters.date_key !== null) {
    return '该时间段暂无照片'
  }
  return '请在左侧选择时间段'
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

// 获取日期key的标签文本
const getDateKeyLabel = (dateKey: number | null): string => {
  if (dateKey === null) return ''
  
  if (dateKey >= 1000000) {
    // 活动 ID（活动ID + 1000000）
    const activityId = dateKey - 1000000
    const activity = activities.value.find(a => a.id === activityId)
    return activity ? activity.name : '活动'
  } else if (dateKey >= 100000) {
    // 年月格式 YYYYMM
    const year = Math.floor(dateKey / 100)
    const month = dateKey % 100
    return `${year}年${month}月`
  } else {
    // 年份 YYYY
    return `${dateKey}年`
  }
}

// 切换过滤器
const toggleFilter = (key: 'star' | 'video') => {
  filters[key] = !filters[key]
  
  // 如果切换了星标过滤，刷新日期树
  if (key === 'star') {
    dateTreeRef.value?.refreshTree()
  }
  
  resetAndLoad()
}

// 处理日期选择
const handleDateSelect = (date_key: number | null) => {
  filters.date_key = date_key
  resetAndLoad()
}

// 加载照片的 API 函数
const loadPhotosApi = async (start: number, size: number): Promise<Photo[]> => {
  try {
    const res = await getPicsApi({
      star: filters.star || undefined,
      video: filters.video || undefined,
      trashed: false,
      start,
      size,
      order: filters.order,
      tag: searchTag.value || undefined,
      date_key: filters.date_key?.toString() || undefined
    })
    
    if (res.code === 0) {
      return res.data.data
    }
    return []
  } catch (error) {
    console.error('Load photos error:', error)
    return []
  }
}

// 加载照片总数
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
  try {
    const res = await getActivitiesApi()
    if (res.code === 0) {
      activities.value = res.data
    }
  } catch (error) {
    console.error('Load activities error:', error)
  }
}

// 重置并加载
const resetAndLoad = async () => {
  await loadCount()
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

// 删除照片
const trashPhoto = async (photo: Photo) => {
  try {
    await trashPhotosApi([photo.id])
    MessagePlugin.success('已移至回收站')
    
    // 更新总数并刷新
    totalCount.value--
    photoGridRef.value?.refresh()
  } catch (error) {
    console.error('Trash photo error:', error)
    MessagePlugin.error('删除失败')
  }
}

// 在查看器中删除照片
const deletePhotoInViewer = async (photo: Photo) => {
  try {
    const currentIndex = photoGridRef.value?.currentPhotoIndex || 0
    
    await trashPhotosApi([photo.id])
    MessagePlugin.success('已移至回收站')
    
    // 更新总数
    totalCount.value--
    
    // 清除缓存
    if (photoGridRef.value) {
      photoGridRef.value.photoCache.clear()
    }
    
    // 切换到下一张或关闭查看器
    if (photoGridRef.value) {
      await photoGridRef.value.switchPhotoInViewer(currentIndex)
    }
  } catch (error) {
    console.error('Delete photo error:', error)
    MessagePlugin.error('删除失败')
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
    photoGridRef.value?.refresh()
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
    // 判断是全部收藏还是全部取消
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
    photoGridRef.value?.refresh()
  } catch (error) {
    console.error('Batch toggle star error:', error)
    MessagePlugin.error('操作失败')
  }
}

// 批量删除
const batchTrash = async (photos: Photo[]) => {
  try {
    const ids = photos.map(p => p.id)
    await trashPhotosApi(ids)
    MessagePlugin.success(`已将 ${ids.length} 张照片移至回收站`)
    
    totalCount.value -= ids.length
    photoGridRef.value?.clearSelection()
    photoGridRef.value?.refresh()
  } catch (error) {
    console.error('Batch trash error:', error)
    MessagePlugin.error('批量删除失败')
  }
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
    
    // 更新当前照片对象的tags
    photoGridRef.value.currentPhoto.tags = tagString
    
    // 同步更新缓存中的照片数据
    const currentIndex = photoGridRef.value.currentPhotoIndex
    const cacheKey = Math.floor(currentIndex / 100) * 100
    const cachePhotos = photoGridRef.value.photoCache.get(cacheKey)
    if (cachePhotos) {
      const photoIndex = currentIndex - cacheKey
      if (cachePhotos[photoIndex]) {
        cachePhotos[photoIndex].tags = tagString
      }
    }
    
    // 添加新标签到列表
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

onMounted(async () => {
  await Promise.all([loadCount(), loadAllTags(), loadActivities()])
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
  min-width: 0;
}
</style>
