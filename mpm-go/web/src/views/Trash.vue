<template>
  <div class="trash-container">
    <!-- 左侧时间线树 -->
    <div class="sidebar">
      <DateTree
        ref="dateTreeRef"
        :star="false"
        :trashed="true"
        @select="handleDateSelect"
      />
    </div>
    
    <!-- 右侧主内容区 -->
    <div class="main-content">
      <PhotoGrid
        ref="photoGridRef"
        :load-photos-api="loadPhotosWrapper"
        :total-count="totalCount"
        :trashed="true"
        empty-text="回收站为空"
      >
        <!-- 过滤器插槽 -->
        <template #filters>
          <!-- 时间筛选提示 -->
          <t-tag
            v-if="dateKey !== null"
            theme="primary"
            variant="light"
            closable
            @close="handleDateSelect(null)"
          >
            <template #icon><t-icon name="time" /></template>
            {{ getDateKeyLabel(dateKey) }}
          </t-tag>
          
          <t-alert theme="warning" message="回收站中的照片可以恢复" />
        </template>
      
      <!-- 批量操作插槽 -->
      <template #batch-actions="{ selectedPhotosList }">
        <t-button size="small" theme="primary" @click="batchRestore(selectedPhotosList)">
          <template #icon><t-icon name="rollback" /></template>
          批量恢复
        </t-button>
      </template>
      
      <!-- 额外操作按钮插槽 -->
      <template #extra-actions>
        <t-popconfirm
          content="确定清空回收站吗？此操作不可恢复！"
          @confirm="emptyTrash"
        >
          <t-button theme="danger" size="small" :loading="emptying">
            <template #icon><t-icon name="delete" /></template>
            清空回收站
          </t-button>
        </t-popconfirm>
      </template>
      
      <!-- 照片操作菜单插槽 -->
      <template #photo-actions="{ photo }">
        <t-dropdown-item @click="restorePhoto(photo)">
          <template #prefix-icon>
            <t-icon name="rollback" />
          </template>
          恢复
        </t-dropdown-item>
      </template>
      
      <!-- 查看器操作插槽 -->
      <template #viewer-actions="{ photo }">
        <t-button theme="primary" block @click="restorePhoto(photo)">
          <template #icon><t-icon name="rollback" /></template>
          恢复照片
        </t-button>
      </template>
      </PhotoGrid>
    </div>
    
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
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import {
  getPicsApi,
  getCountApi,
  trashPhotosApi,
  emptyTrashApi,
  getProgressApi,
  Photo
} from '@/api'
import PhotoGrid from '@/components/PhotoGrid.vue'
import DateTree from '@/components/DateTree.vue'

const photoGridRef = ref<InstanceType<typeof PhotoGrid> | null>(null)
const dateTreeRef = ref<InstanceType<typeof DateTree> | null>(null)
const totalCount = ref(0)
const emptying = ref(false)
const dateKey = ref<string | null>(null)

const progressVisible = ref(false)
const progressData = reactive({
  total: 0,
  count: 0,
  progress: 0
})

const progress = ref(0)
let progressTimer: number | null = null

// 处理时间线选择
const handleDateSelect = (nodeId: number | null) => {
  if (nodeId === null) {
    dateKey.value = null
  } else if (nodeId >= 1000000) {
    // 活动节点: ID = 1000000 + activityId
    dateKey.value = String(nodeId)
  } else if (nodeId >= 100) {
    // 月份节点: ID = year * 100 + month
    const year = Math.floor(nodeId / 100)
    const month = nodeId % 100
    dateKey.value = `${year}${String(month).padStart(2, '0')}`
  } else {
    // 年份节点: ID = year
    dateKey.value = String(nodeId)
  }
  
  // 刷新照片列表
  photoGridRef.value?.refresh()
}

// 获取 dateKey 的显示标签
const getDateKeyLabel = (key: string) => {
  // 根据 dateKey 的格式判断类型
  if (key.length === 4) {
    // 年份: "2025"
    return `${key} 年`
  } else if (key.length === 6) {
    // 月份: "202501"
    const year = key.substring(0, 4)
    const month = parseInt(key.substring(4, 6))
    return `${year} 年 ${month} 月`
  } else if (parseInt(key) >= 1000000) {
    // 活动: "1000001"
    return '活动筛选'
  } else {
    return key
  }
}

// 加载照片的包装函数
const loadPhotosWrapper = async (start: number, size: number): Promise<Photo[]> => {
  try {
    const res = await getPicsApi({
      trashed: true,
      start,
      size,
      order: '-taken_date',
      dateKey: dateKey.value || undefined
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

// 加载总数
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

// 恢复单张照片
const restorePhoto = async (photo: Photo) => {
  if (!photoGridRef.value) return
  
  try {
    // 保存当前查看器状态
    const isViewerOpen = photoGridRef.value.viewerVisible
    const currentIndex = photoGridRef.value.currentPhotoIndex
    
    await trashPhotosApi([photo.id])
    MessagePlugin.success('已恢复')
    
    // 减少计数
    totalCount.value--
    
    // 如果查看器是打开的，需要智能切换照片
    if (isViewerOpen) {
      // 清除缓存
      photoGridRef.value.photoCache.clear()
      
      // 等待一下确保数据已更新
      await new Promise(resolve => setTimeout(resolve, 100))
      
      // 智能切换到下一张照片
      await photoGridRef.value.switchPhotoInViewer(currentIndex)
    } else {
      // 如果查看器未打开，只需刷新列表
      photoGridRef.value?.refresh()
    }
  } catch (error) {
    console.error('Restore photo error:', error)
    MessagePlugin.error('恢复失败')
  }
}

// 批量恢复
const batchRestore = async (photos: Photo[]) => {
  if (photos.length === 0) return
  
  try {
    const idsToRestore = photos.map(p => p.id)
    
    await trashPhotosApi(idsToRestore)
    MessagePlugin.success(`已恢复 ${idsToRestore.length} 张照片`)
    
    // 重新加载
    await loadCount()
    photoGridRef.value?.refresh()
  } catch (error) {
    console.error('Batch restore error:', error)
    MessagePlugin.error('批量恢复失败')
  }
}

// 清空回收站
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

// 清空完成后重置
const resetAndLoadAfterEmpty = async () => {
  await loadCount()
  photoGridRef.value?.refresh()
}

// 进度轮询
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

// 初始化
onMounted(async () => {
  await loadCount()
})

onBeforeUnmount(() => {
  stopProgressPolling()
})
</script>

<style scoped>
.trash-container {
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
