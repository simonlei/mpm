<template>
  <div class="photo-detail-container" :class="layoutClass">
    <!-- 媒体展示区域 -->
    <div class="media-section">
      <!-- 视频播放器 -->
      <video
        v-if="photo.media_type === 'video'"
        :src="`/cos/video_t/${photo.name}.mp4`"
        controls
        class="media-video"
      >
        您的浏览器不支持视频播放
      </video>
      
      <!-- 图片展示（带人脸框） -->
      <div
        v-else
        class="media-image-container"
      >
        <div class="image-wrapper" :style="{ transform: `rotate(${photo.rotate || 0}deg)` }">
          <img
            ref="viewerImageRef"
            :src="`/cos/small/${photo.name}`"
            :alt="photo.name"
            class="media-image"
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
              @click.stop="editFaceName(face)"
            >
              <div class="face-name">{{ face.name || '未命名' }}</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 旋转按钮 -->
      <div v-if="photo.media_type !== 'video'" class="rotate-buttons">
        <t-button
          theme="default"
          variant="outline"
          shape="circle"
          size="small"
          :loading="rotating"
          @click="rotatePhoto(-90)"
        >
          <template #icon><t-icon name="rollback" /></template>
        </t-button>
        <t-button
          theme="default"
          variant="outline"
          shape="circle"
          size="small"
          :loading="rotating"
          @click="rotatePhoto(90)"
        >
          <template #icon><t-icon name="rollfront" /></template>
        </t-button>
      </div>
    </div>
    
    <!-- 信息展示区域 -->
    <div class="info-section">
      <t-descriptions :column="1" bordered>
        <t-descriptions-item v-if="photo.media_type === 'video' && photo.duration" label="时长">
          {{ formatDuration(photo.duration) }}
        </t-descriptions-item>
        <t-descriptions-item label="大小">
          {{ formatFileSize(photo.size) }}
        </t-descriptions-item>
        <t-descriptions-item label="宽度">
          {{ photo.width }} px
        </t-descriptions-item>
        <t-descriptions-item label="高度">
          {{ photo.height }} px
        </t-descriptions-item>
        <t-descriptions-item label="描述">
          <div v-if="!editingDescription" class="description-display">
            <span v-if="photo.description" class="description-text">{{ photo.description }}</span>
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
            <span class="description-text">{{ formatDateTime(photo.taken_date) }}</span>
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
          <slot name="tag-editor" :photo="photo" :tags="currentPhotoTags">
            <span v-if="photo.tags">{{ photo.tags }}</span>
            <span v-else class="empty-description">无标签</span>
          </slot>
        </t-descriptions-item>
        <t-descriptions-item v-if="photo.address" label="地址">
          {{ photo.address }}
        </t-descriptions-item>
        <t-descriptions-item v-if="photo.latitude" label="位置">
          {{ photo.latitude.toFixed(6) }}, {{ photo.longitude.toFixed(6) }}
        </t-descriptions-item>
        <t-descriptions-item v-if="photo.activity_desc" label="所属活动">
          {{ photo.activity_desc }}
        </t-descriptions-item>
      </t-descriptions>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <t-button
          v-if="photo.media_type !== 'video'"
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
        
        <slot name="actions" :photo="photo"></slot>
      </div>
    </div>
    
    <!-- 编辑人脸名字对话框 -->
    <t-dialog
      v-model:visible="editFaceNameVisible"
      header="编辑人脸名字"
      :on-confirm="saveFaceName"
      :confirm-btn="{ loading: savingFaceName }"
    >
      <t-form :data="editFaceNameForm" label-align="top">
        <t-form-item label="姓名">
          <t-input 
            v-model="editFaceNameForm.name" 
            placeholder="请输入姓名"
            @keyup.enter="saveFaceName"
          />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { 
  Photo, 
  FaceForPhoto, 
  getFacesForPhotoApi, 
  updateFaceApi,
  updateImageApi
} from '@/api'
import dayjs from 'dayjs'

interface Props {
  photo: Photo
  layout?: 'vertical' | 'horizontal' // vertical: 上下结构, horizontal: 左右结构
}

const props = withDefaults(defineProps<Props>(), {
  layout: 'horizontal'
})

const emit = defineEmits<{
  'update:photo': [photo: Photo]
}>()

const viewerImageRef = ref<HTMLImageElement | null>(null)

// 计算布局类名
const layoutClass = computed(() => {
  return props.layout === 'vertical' ? 'layout-vertical' : 'layout-horizontal'
})

// 人脸圈选功能
const showFaceBoxes = ref(false)
const loadingFaces = ref(false)
const facesForPhoto = ref<FaceForPhoto[]>([])

// 编辑功能
const editingDescription = ref(false)
const editDescriptionValue = ref('')
const savingDescription = ref(false)
const editingDateTime = ref(false)
const editDateTimeValue = ref('')
const savingDateTime = ref(false)
const currentPhotoTags = ref<string[]>([])

// 编辑人脸名字
const editFaceNameVisible = ref(false)
const savingFaceName = ref(false)
const editFaceNameForm = ref({
  faceId: 0,
  name: ''
})

// 旋转功能
const rotating = ref(false)

// 监听 photo 变化，更新标签
watch(() => props.photo, (newPhoto) => {
  if (newPhoto) {
    currentPhotoTags.value = newPhoto.tags ? newPhoto.tags.split(',').filter(t => t.trim()) : []
    // 重置编辑状态
    editingDescription.value = false
    editDescriptionValue.value = ''
    editingDateTime.value = false
    editDateTimeValue.value = ''
    showFaceBoxes.value = false
    facesForPhoto.value = []
  }
}, { immediate: true })

// 描述编辑功能
const startEditDescription = () => {
  editDescriptionValue.value = props.photo.description || ''
  editingDescription.value = true
}

const cancelEditDescription = () => {
  editingDescription.value = false
  editDescriptionValue.value = ''
}

const saveDescription = async () => {
  if (savingDescription.value) return
  
  savingDescription.value = true
  try {
    const response = await updateImageApi({
      id: props.photo.id,
      description: editDescriptionValue.value
    })
    
    if (response.code === 0 && response.data) {
      emit('update:photo', { ...props.photo, description: response.data.description })
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
const startEditDateTime = () => {
  editDateTimeValue.value = formatDateTime(props.photo.taken_date)
  editingDateTime.value = true
}

const cancelEditDateTime = () => {
  editingDateTime.value = false
  editDateTimeValue.value = ''
}

const saveDateTime = async () => {
  if (savingDateTime.value || !editDateTimeValue.value) return
  
  savingDateTime.value = true
  try {
    const localDateString = dayjs(editDateTimeValue.value).format('YYYY-MM-DDTHH:mm:ss+08:00')
    
    const response = await updateImageApi({
      id: props.photo.id,
      taken_date: localDateString
    })
    
    if (response.code === 0 && response.data) {
      emit('update:photo', { ...props.photo, taken_date: response.data.taken_date })
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

// 切换人脸框显示
const toggleFaceBoxes = async () => {
  if (showFaceBoxes.value) {
    showFaceBoxes.value = false
    facesForPhoto.value = []
  } else {
    await loadFacesForPhoto()
  }
}

// 加载照片的人脸信息
const loadFacesForPhoto = async () => {
  if (loadingFaces.value) return
  
  loadingFaces.value = true
  try {
    const res = await getFacesForPhotoApi({ id: props.photo.id })
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

// 计算人脸框的样式
const getFaceBoxStyle = (face: FaceForPhoto) => {
  if (!viewerImageRef.value) return {}
  
  const img = viewerImageRef.value
  const imgDisplayWidth = img.offsetWidth
  const imgDisplayHeight = img.offsetHeight
  const imgNaturalWidth = img.naturalWidth
  const imgNaturalHeight = img.naturalHeight
  
  const scaleX = imgDisplayWidth / imgNaturalWidth
  const scaleY = imgDisplayHeight / imgNaturalHeight
  
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

// 编辑人脸名字
const editFaceName = (face: FaceForPhoto) => {
  editFaceNameForm.value = {
    faceId: face.faceId,
    name: face.name || ''
  }
  editFaceNameVisible.value = true
}

// 保存人脸名字
const saveFaceName = async () => {
  if (savingFaceName.value) return
  
  savingFaceName.value = true
  try {
    await updateFaceApi({
      faceId: editFaceNameForm.value.faceId,
      name: editFaceNameForm.value.name || undefined
    })
    
    const faceIndex = facesForPhoto.value.findIndex(f => f.faceId === editFaceNameForm.value.faceId)
    if (faceIndex !== -1) {
      facesForPhoto.value[faceIndex].name = editFaceNameForm.value.name
    }
    
    MessagePlugin.success('保存成功')
    editFaceNameVisible.value = false
  } catch (error) {
    console.error('Save face name error:', error)
    MessagePlugin.error('保存失败')
  } finally {
    savingFaceName.value = false
  }
}

// 旋转照片
const rotatePhoto = async (degrees: number) => {
  if (rotating.value) return
  
  rotating.value = true
  try {
    const currentRotate = props.photo.rotate || 0
    const newRotate = (currentRotate + degrees) % 360
    
    const response = await updateImageApi({
      id: props.photo.id,
      rotate: newRotate
    })
    
    if (response.code === 0 && response.data) {
      emit('update:photo', { 
        ...props.photo, 
        rotate: response.data.rotate,
        thumb: response.data.thumb
      })
    }
    
    MessagePlugin.success('旋转成功')
  } catch (error) {
    console.error('Rotate photo error:', error)
    MessagePlugin.error('旋转失败')
  } finally {
    rotating.value = false
  }
}

// 下载原图
const downloadOriginal = () => {
  const downloadUrl = `/cos/origin/${props.photo.name}`
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = props.photo.name
  link.click()
}

const formatDateTime = (date: string) => {
  if (!date) return ''
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
    return `${hours}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
  } else {
    return `${minutes}:${String(secs).padStart(2, '0')}`
  }
}
</script>

<style scoped>
/* 照片详情容器 */
.photo-detail-container {
  display: flex;
  gap: 0; /* 移除gap，改用padding */
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 垂直布局（上下结构）- 用于地图页面 */
.photo-detail-container.layout-vertical {
  flex-direction: column;
}

.photo-detail-container.layout-vertical .media-section {
  width: 100%;
  margin-bottom: 16px;
}

.photo-detail-container.layout-vertical .info-section {
  width: 100%;
}

/* 水平布局（左右结构）- 用于首页、相册等页面 */
.photo-detail-container.layout-horizontal {
  flex-direction: row;
  align-items: flex-start;
}

.photo-detail-container.layout-horizontal .media-section {
  flex: 0 0 79%;
  width: 79%;
  max-width: 79%;
  min-width: 0;
  box-sizing: border-box;
}

.photo-detail-container.layout-horizontal .info-section {
  flex: 0 0 21%;
  width: 21%;
  max-width: 21%;
  min-width: 0;
  box-sizing: border-box;
  overflow-x: hidden;
  padding-left: 20px; /* 左侧留出间隙 */
}

/* 媒体展示区域 */
.media-section {
  position: relative;
  background: #f5f5f5;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.media-video {
  max-width: 100%;
  max-height: 500px;
  border-radius: 4px;
}

.media-image-container {
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

.media-image {
  max-width: 100%;
  max-height: 500px;
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
  cursor: pointer;
  pointer-events: auto;
}

.face-box:hover {
  border-color: #0034b5;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.9), 0 0 15px rgba(0, 82, 217, 0.8);
  transform: scale(1.02);
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
  backdrop-filter: blur(4px);
  cursor: pointer;
  transition: all 0.2s;
}

.face-name:hover {
  background: rgba(0, 52, 181, 0.95);
  transform: translateX(-50%) scale(1.05);
}

.rotate-buttons {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  background: rgba(255, 255, 255, 0.95);
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 5;
}

/* 信息展示区域 */
.info-section {
  display: flex;
  flex-direction: column;
  gap: 12px; /* 减小间距 */
  width: 100%;
  min-width: 0; /* 允许内容收缩 */
  max-width: 100%;
  box-sizing: border-box;
  padding: 0; /* 确保没有额外的内边距 */
}

/* 强制限制 descriptions 表格宽度 */
.info-section :deep(.t-descriptions) {
  width: 100% !important;
  max-width: 100% !important;
  min-width: 0 !important;
  table-layout: fixed !important; /* 固定表格布局，防止单元格撑开 */
  overflow: hidden;
  box-sizing: border-box;
  margin: 0 !important; /* 移除外边距 */
}

.info-section :deep(.t-descriptions table) {
  width: 100% !important;
  max-width: 100% !important;
  min-width: 0 !important;
  table-layout: fixed !important;
  box-sizing: border-box;
  margin: 0 !important;
}

.info-section :deep(.t-descriptions-item__label),
.info-section :deep(.t-descriptions-item__content) {
  word-break: break-word;
  overflow-wrap: break-word;
  max-width: 100%;
  min-width: 0;
  overflow: hidden;
  white-space: normal; /* 允许换行 */
  padding: 6px 8px !important; /* 减小内边距 */
  box-sizing: border-box;
}

/* 限制 label 列宽度 */
.info-section :deep(.t-descriptions-item__label) {
  width: 60px !important; /* 适合4个中文字 */
  min-width: 60px !important;
  max-width: 60px !important;
  font-size: 12px;
  padding: 4px 6px !important;
  line-height: 1.5;
  word-break: keep-all;
  text-align: left;
}

/* 限制 descriptions 内容区域的子元素 */
.info-section :deep(.t-descriptions-item__content) {
  width: calc(100% - 60px) !important; /* 剩余宽度给内容 */
  font-size: 12px;
  padding: 4px 6px !important;
}

.info-section :deep(.t-descriptions-item__content) > * {
  max-width: 100% !important;
  box-sizing: border-box;
  overflow: hidden;
}

/* 特别限制 select 组件 */
.info-section :deep(.t-select),
.info-section :deep(.t-select__wrap) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
}

/* 确保表单元素不会溢出 */
.info-section :deep(.t-textarea__inner),
.info-section :deep(.t-input),
.info-section :deep(.t-input__inner),
.info-section :deep(.t-select),
.info-section :deep(.t-select__wrap),
.info-section :deep(.t-date-picker),
.info-section :deep(.t-date-picker__input-container) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
  font-size: 13px !important; /* 减小字体 */
}

.description-editor {
  width: 100%;
  box-sizing: border-box;
}

.description-editor :deep(.t-textarea),
.description-editor :deep(.t-date-picker) {
  width: 100% !important;
}

.description-display {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.description-text {
  flex: 1;
  white-space: pre-wrap;
  word-break: break-all; /* 强制在任意字符处换行 */
  overflow-wrap: break-word;
  max-width: 100%;
  overflow: hidden;
}

.empty-description {
  color: var(--td-text-color-placeholder);
  font-size: 12px;
}

.description-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

/* 强制限制编辑器内的所有元素 */
.description-editor > * {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
}

.description-editor :deep(.t-textarea),
.description-editor :deep(.t-textarea__inner),
.description-editor :deep(.t-date-picker),
.description-editor :deep(.t-date-picker__panel) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px; /* 减小间距 */
  padding-top: 12px; /* 减小上边距 */
  border-top: 1px solid var(--td-component-border);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

/* 强制限制操作按钮宽度 */
.action-buttons :deep(.t-button) {
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
  font-size: 13px !important; /* 减小按钮字体 */
  padding: 6px 12px !important; /* 减小按钮内边距 */
}
</style>
