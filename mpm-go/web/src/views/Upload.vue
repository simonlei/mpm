<template>
  <div class="upload-container">
    <t-card title="照片上传" :bordered="false">
      <div class="upload-area">
        <t-upload
          v-model="files"
          :auto-upload="false"
          :multiple="true"
          :max="100"
          accept="image/*,video/*"
          theme="file-flow"
          @change="handleFileChange"
        >
          <template #drag-content>
            <div class="drag-content">
              <t-icon name="cloud-upload" size="48px" />
              <div class="tip">点击或拖拽文件到此区域上传</div>
              <div class="sub-tip">支持批量上传，支持图片和视频</div>
            </div>
          </template>
        </t-upload>
      </div>
      
      <div v-if="files.length > 0" class="action-bar">
        <t-space>
          <t-button
            theme="primary"
            :loading="uploading"
            :disabled="files.length === 0"
            @click="startUpload"
          >
            <template #icon><t-icon name="upload" /></template>
            开始上传 ({{ files.length }})
          </t-button>
          
          <t-button
            theme="default"
            :disabled="uploading"
            @click="clearFiles"
          >
            清空列表
          </t-button>
        </t-space>
        
        <div class="progress-info">
          <span>已上传: {{ uploadedCount }} / {{ files.length }}</span>
          <t-progress
            v-if="uploading"
            :percentage="uploadProgress"
            :label="false"
            style="width: 200px; margin-left: 16px;"
          />
        </div>
      </div>
      
      <!-- 上传列表 -->
      <div v-if="files.length > 0" class="upload-list">
        <div
          v-for="(file, index) in files"
          :key="index"
          class="upload-item"
        >
          <div class="file-info">
            <t-icon name="file" />
            <span class="file-name">{{ file.raw?.name }}</span>
            <span class="file-size">{{ formatSize(file.raw?.size || 0) }}</span>
          </div>
          
          <div class="file-status">
            <t-tag
              v-if="file.status === 'success'"
              theme="success"
              variant="light"
            >
              上传成功
            </t-tag>
            <t-tag
              v-else-if="file.status === 'fail'"
              theme="danger"
              variant="light"
            >
              上传失败
            </t-tag>
            <t-loading v-else-if="file.status === 'progress'" size="small" />
            <t-tag v-else theme="default" variant="light">
              等待上传
            </t-tag>
          </div>
        </div>
      </div>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import axios from 'axios'
import { useUserStore } from '@/stores/user'

interface UploadFile {
  raw?: File
  status?: 'waiting' | 'progress' | 'success' | 'fail'
  [key: string]: any
}

const userStore = useUserStore()
const files = ref<UploadFile[]>([])
const uploading = ref(false)
const uploadedCount = ref(0)
const batchId = ref('')

const uploadProgress = computed(() => {
  if (files.value.length === 0) return 0
  return Math.round((uploadedCount.value / files.value.length) * 100)
})

const handleFileChange = (fileList: UploadFile[]) => {
  files.value = fileList.map(f => ({
    ...f,
    status: 'waiting'
  }))
}

const clearFiles = () => {
  files.value = []
  uploadedCount.value = 0
}

const startUpload = async () => {
  if (files.value.length === 0) return
  
  uploading.value = true
  uploadedCount.value = 0
  batchId.value = Date.now().toString()
  
  for (let i = 0; i < files.value.length; i++) {
    const file = files.value[i]
    file.status = 'progress'
    
    try {
      await uploadFile(file.raw!)
      file.status = 'success'
      uploadedCount.value++
    } catch (error) {
      console.error('Upload error:', error)
      file.status = 'fail'
    }
  }
  
  uploading.value = false
  MessagePlugin.success(`上传完成！成功 ${uploadedCount.value} 个`)
}

const uploadFile = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('batchId', batchId.value)
  formData.append('lastModified', file.lastModified.toString())
  
  await axios.post('/api/uploadPhoto', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Signature': userStore.signature,
      'Account': userStore.account
    }
  })
}

const formatSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}
</script>

<style scoped>
.upload-container {
  height: 100%;
}

.upload-area {
  margin-bottom: 24px;
}

.drag-content {
  text-align: center;
  padding: 40px;
}

.tip {
  margin-top: 16px;
  font-size: 16px;
  color: var(--td-text-color-primary);
}

.sub-tip {
  margin-top: 8px;
  font-size: 14px;
  color: var(--td-text-color-placeholder);
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--td-border-level-1-color);
}

.progress-info {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: var(--td-text-color-secondary);
}

.upload-list {
  margin-top: 16px;
  max-height: 400px;
  overflow-y: auto;
}

.upload-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid var(--td-border-level-1-color);
}

.upload-item:hover {
  background: var(--td-bg-color-container-hover);
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.file-name {
  font-size: 14px;
  color: var(--td-text-color-primary);
}

.file-size {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
}

.file-status {
  min-width: 100px;
  text-align: right;
}
</style>
