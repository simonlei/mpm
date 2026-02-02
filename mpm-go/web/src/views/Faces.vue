<template>
  <div class="faces-container">
    <!-- 左侧人脸列表 -->
    <div class="sidebar">
      <t-card title="人脸" :bordered="false" class="faces-list-card">
        <!-- 过滤器 -->
        <div class="filter-section">
          <t-input
            v-model="nameFilter"
            placeholder="搜索姓名"
            clearable
            @change="loadFaces"
          >
            <template #prefix-icon>
              <t-icon name="search" />
            </template>
          </t-input>
          
          <t-switch v-model="showHidden" label="显示隐藏" @change="loadFaces" />
        </div>
        
        <!-- 人脸列表 -->
        <div v-loading="loading" class="faces-list">
          <div
            v-for="face in faceData.faces"
            :key="face.face_id"
            class="face-item"
            :class="{ 'face-item-active': selectedFace?.face_id === face.face_id }"
            @click="selectFace(face)"
          >
            <div class="face-avatar-small">
              <img
                :src="`/get_face_img/${face.face_id}/${face.selected_face || 0}`"
                :alt="face.name || '未命名'"
                @error="handleImageError"
              />
            </div>
            
            <div class="face-info">
              <div class="face-name">{{ face.name || '未命名' }}</div>
              <div class="face-count">{{ face.count }} 张</div>
            </div>
            
            <div class="face-actions" @click.stop>
              <t-button
                size="small"
                variant="text"
                :theme="face.collected ? 'warning' : 'default'"
                @click="toggleCollect(face)"
              >
                <t-icon :name="face.collected ? 'star-filled' : 'star'" />
              </t-button>
              
              <t-button
                size="small"
                variant="text"
                @click="editFace(face)"
              >
                <t-icon name="edit" />
              </t-button>
            </div>
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <t-pagination
            v-model="pagination.current"
            v-model:page-size="pagination.pageSize"
            :total="faceData.total"
            :page-size-options="[20, 50, 100]"
            size="small"
            @change="loadFaces"
          />
        </div>
      </t-card>
    </div>
    
    <!-- 右侧照片网格 -->
    <div class="main-content">
      <PhotoGridView
        ref="photoGridViewRef"
        :load-photos-params="loadPhotosParams"
        :total-count="totalCount"
        :empty-text="emptyText"
        :show-activity-action="false"
        @total-count-change="handleTotalCountChange"
      >
        <template #filters>
          <t-tag
            v-if="selectedFace"
            theme="primary"
            variant="light"
            closable
            @close="clearFaceSelection"
          >
            <template #icon><t-icon name="user" /></template>
            {{ selectedFace.name || '未命名' }}
          </t-tag>
        </template>
      </PhotoGridView>
    </div>
    
    <!-- 编辑人脸对话框 -->
    <t-dialog
      v-model:visible="editVisible"
      header="编辑人脸"
      :on-confirm="saveFace"
      :confirm-btn="{ loading: saving }"
    >
      <t-form :data="editForm" label-align="top">
        <t-form-item label="姓名">
          <t-input v-model="editForm.name" placeholder="请输入姓名" />
        </t-form-item>
        
        <t-form-item label="状态">
          <t-space>
            <t-checkbox v-model="editForm.hidden">隐藏</t-checkbox>
            <t-checkbox v-model="editForm.collected">收藏</t-checkbox>
          </t-space>
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import {
  getFacesApi,
  updateFaceApi,
  getPicsApi,
  Face,
  FacesResponse
} from '@/api'
import PhotoGridView from '@/components/PhotoGridView.vue'

const photoGridViewRef = ref<InstanceType<typeof PhotoGridView> | null>(null)

const loading = ref(false)
const showHidden = ref(false)
const nameFilter = ref('')
const selectedFace = ref<Face | null>(null)
const totalCount = ref(0)

const faceData = reactive<FacesResponse>({
  total: 0,
  faces: []
})

const pagination = reactive({
  current: 1,
  pageSize: 20
})

const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({
  face_id: 0,
  name: '',
  hidden: false,
  collected: false
})

const emptyText = computed(() => {
  if (!selectedFace.value) {
    return '暂无照片'
  }
  return '该人脸暂无照片'
})

// 加载照片的参数
const loadPhotosParams = computed(() => {
  if (selectedFace.value) {
    return { face_id: selectedFace.value.face_id }
  }
  return {}
})

// 加载人脸列表
const loadFaces = async () => {
  loading.value = true
  try {
    const res = await getFacesApi({
      show_hidden: showHidden.value,
      page: pagination.current,
      size: pagination.pageSize,
      name_filter: nameFilter.value
    })
    
    if (res.code === 0) {
      Object.assign(faceData, res.data)
    }
  } catch (error) {
    console.error('Load faces error:', error)
  } finally {
    loading.value = false
  }
}

// 选择人脸
const selectFace = async (face: Face) => {
  selectedFace.value = face
  totalCount.value = face.count
  // 等待下一个 tick，确保 computed 属性已更新
  await nextTick()
  photoGridViewRef.value?.refresh()
}

// 清除人脸选择
const clearFaceSelection = async () => {
  selectedFace.value = null
  await loadAllPhotosCount()
  // 等待下一个 tick，确保 computed 属性已更新
  await nextTick()
  photoGridViewRef.value?.refresh()
}

// 加载所有照片总数
const loadAllPhotosCount = async () => {
  try {
    const res = await getPicsApi({
      trashed: false,
      start: 0,
      size: 1
    })
    if (res.code === 0) {
      totalCount.value = res.data.total_rows
    }
  } catch (error) {
    console.error('Load all photos count error:', error)
  }
}

// 处理总数变化
const handleTotalCountChange = (delta: number) => {
  totalCount.value += delta
  if (selectedFace.value) {
    selectedFace.value.count += delta
  }
}

// 编辑人脸
const editFace = (face: Face) => {
  editForm.face_id = face.face_id
  editForm.name = face.name
  editForm.hidden = !!face.hidden
  editForm.collected = !!face.collected
  editVisible.value = true
}

// 保存人脸
const saveFace = async () => {
  saving.value = true
  try {
    await updateFaceApi({
      face_id: editForm.face_id,
      name: editForm.name || undefined,
      hidden: editForm.hidden,
      collected: editForm.collected
    })
    MessagePlugin.success('保存成功')
    editVisible.value = false
    loadFaces()
  } catch (error) {
    console.error('Save face error:', error)
  } finally {
    saving.value = false
  }
}

// 切换收藏
const toggleCollect = async (face: Face) => {
  try {
    await updateFaceApi({
      face_id: face.face_id,
      collected: !face.collected
    })
    face.collected = face.collected ? 0 : 1
    MessagePlugin.success(face.collected ? '已收藏' : '已取消收藏')
  } catch (error) {
    console.error('Toggle collect error:', error)
  }
}

const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%23ddd" width="100" height="100"/%3E%3Ctext x="50" y="55" text-anchor="middle" font-size="16" fill="%23999"%3E无图片%3C/text%3E%3C/svg%3E'
}

onMounted(async () => {
  await loadFaces()
  await loadAllPhotosCount()
  photoGridViewRef.value?.refresh()
})
</script>

<style scoped>
.faces-container {
  height: 100%;
  display: flex;
  gap: 16px;
}

.sidebar {
  width: 280px;
  flex-shrink: 0;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.faces-list-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.faces-list-card :deep(.t-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.filter-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.faces-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 16px;
}

.face-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 8px;
}

.face-item:hover {
  background: var(--td-bg-color-container-hover);
}

.face-item-active {
  background: var(--td-brand-color-light);
  border: 1px solid var(--td-brand-color);
}

.face-avatar-small {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: var(--td-bg-color-container);
}

.face-avatar-small img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.face-info {
  flex: 1;
  min-width: 0;
}

.face-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--td-text-color-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.face-count {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.face-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.pagination-wrapper {
  padding-top: 12px;
  border-top: 1px solid var(--td-component-border);
}
</style>
