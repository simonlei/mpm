<template>
  <div class="faces-container">
    <t-card title="人脸管理" :bordered="false">
      <!-- 过滤器 -->
      <div class="filter-bar">
        <t-space>
          <t-switch v-model="showHidden" label="显示隐藏" @change="loadFaces" />
          
          <t-input
            v-model="nameFilter"
            placeholder="搜索姓名"
            clearable
            style="width: 200px"
            @change="loadFaces"
          >
            <template #prefix-icon>
              <t-icon name="search" />
            </template>
          </t-input>
        </t-space>
        
        <div class="stats">
          共 {{ faceData.total }} 个人脸
        </div>
      </div>
      
      <!-- 人脸网格 -->
      <div v-loading="loading" class="faces-grid">
        <div
          v-for="face in faceData.faces"
          :key="face.faceId"
          class="face-card"
        >
          <div class="face-avatar">
            <img
              :src="`/get_face_img/${face.faceId}/0`"
              :alt="face.name || '未命名'"
              @error="handleImageError"
            />
          </div>
          
          <div class="face-info">
            <div class="face-name">
              {{ face.name || '未命名' }}
            </div>
            <div class="face-count">
              {{ face.count }} 张照片
            </div>
          </div>
          
          <div class="face-actions">
            <t-space>
              <t-button
                size="small"
                variant="text"
                @click="editFace(face)"
              >
                <t-icon name="edit" />
              </t-button>
              
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
                @click="viewFacePhotos(face)"
              >
                <t-icon name="browse" />
              </t-button>
            </t-space>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-wrapper">
        <t-pagination
          v-model="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="faceData.total"
          @change="loadFaces"
        />
      </div>
    </t-card>
    
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
    
    <!-- 照片查看对话框 -->
    <t-dialog
      v-model:visible="photosVisible"
      :header="`${currentFace?.name || '未命名'} 的照片`"
      width="90%"
      :footer="false"
      destroy-on-close
    >
      <div v-loading="photosLoading" class="face-photos">
        <div class="photos-grid-small">
          <img
            v-for="photo in facePhotos"
            :key="photo.id"
            :src="`/cos/${photo.thumb}`"
            :alt="photo.name"
            class="photo-thumb"
            @click="viewPhoto(photo)"
          />
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import {
  getFacesApi,
  updateFaceApi,
  getPicsApi,
  Face,
  FacesResponse,
  Photo
} from '@/api'

const loading = ref(false)
const showHidden = ref(false)
const nameFilter = ref('')

const faceData = reactive<FacesResponse>({
  total: 0,
  faces: []
})

const pagination = reactive({
  current: 1,
  pageSize: 24
})

const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({
  faceId: 0,
  name: '',
  hidden: false,
  collected: false
})

const photosVisible = ref(false)
const photosLoading = ref(false)
const currentFace = ref<Face | null>(null)
const facePhotos = ref<Photo[]>([])

const loadFaces = async () => {
  loading.value = true
  try {
    const res = await getFacesApi({
      showHidden: showHidden.value,
      page: pagination.current,
      size: pagination.pageSize,
      nameFilter: nameFilter.value
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

const editFace = (face: Face) => {
  editForm.faceId = face.faceId
  editForm.name = face.name
  editForm.hidden = !!face.hidden
  editForm.collected = !!face.collected
  editVisible.value = true
}

const saveFace = async () => {
  saving.value = true
  try {
    await updateFaceApi({
      faceId: editForm.faceId,
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

const toggleCollect = async (face: Face) => {
  try {
    await updateFaceApi({
      faceId: face.faceId,
      collected: !face.collected
    })
    face.collected = face.collected ? 0 : 1
    MessagePlugin.success(face.collected ? '已收藏' : '已取消收藏')
  } catch (error) {
    console.error('Toggle collect error:', error)
  }
}

const viewFacePhotos = async (face: Face) => {
  currentFace.value = face
  photosVisible.value = true
  photosLoading.value = true
  
  try {
    const res = await getPicsApi({
      faceId: face.faceId,
      trashed: false,
      start: 0,
      size: 100
    })
    
    if (res.code === 0) {
      facePhotos.value = res.data.data
    }
  } catch (error) {
    console.error('Load face photos error:', error)
  } finally {
    photosLoading.value = false
  }
}

const viewPhoto = (photo: Photo) => {
  window.open(`/cos/small/${photo.name}`, '_blank')
}

const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"%3E%3Crect fill="%23ddd" width="100" height="100"/%3E%3Ctext x="50" y="55" text-anchor="middle" font-size="16" fill="%23999"%3E无图片%3C/text%3E%3C/svg%3E'
}

onMounted(() => {
  loadFaces()
})
</script>

<style scoped>
.faces-container {
  height: 100%;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.stats {
  font-size: 14px;
  color: var(--td-text-color-secondary);
}

.faces-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  min-height: 400px;
}

.face-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.face-card:hover {
  transform: translateY(-4px);
}

.face-avatar {
  width: 120px;
  height: 120px;
  margin: 0 auto 12px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--td-bg-color-container);
}

.face-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.face-info {
  margin-bottom: 12px;
}

.face-name {
  font-size: 16px;
  font-weight: 500;
  color: var(--td-text-color-primary);
  margin-bottom: 4px;
}

.face-count {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.face-actions {
  display: flex;
  justify-content: center;
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.face-photos {
  max-height: 70vh;
  overflow-y: auto;
}

.photos-grid-small {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
}

.photo-thumb {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s;
}

.photo-thumb:hover {
  transform: scale(1.05);
}
</style>
