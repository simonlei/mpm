<template>
  <div class="admin-container">
    <t-card title="系统管理" bordered>
      <div class="admin-content">
        <t-space direction="vertical" size="large">
          <!-- 媒体文件修复区域 -->
          <t-card title="媒体文件修复工具" hoverable>
            <div class="tool-item">
              <div class="tool-info">
                <h3>修复宽高为0的图片和视频</h3>
                <p class="tool-description">
                  扫描数据库中宽高为0的图片和视频文件。<br/>
                  <strong>图片：</strong>从COS下载原图计算宽高并更新，同时生成缩略图上传到COS。<br/>
                  <strong>视频：</strong>从COS下载视频文件，使用ffprobe获取宽高、时长、录像时间等元数据并更新。<br/>
                  适用于超大文件或格式特殊导致COS无法获取元数据的情况。
                </p>
              </div>
              <t-button 
                theme="primary" 
                :loading="fixingPhotos"
                @click="handleFixZeroDimensionPhotos"
              >
                {{ fixingPhotos ? '修复中...' : '开始修复' }}
              </t-button>
            </div>
          </t-card>

          <!-- 按ID强制修复区域 -->
          <t-card title="按ID强制修复" hoverable>
            <div class="tool-item">
              <div class="tool-info">
                <h3>强制修复指定照片/视频</h3>
                <p class="tool-description">
                  对指定ID的照片/视频进行强制修复，支持智能类型识别和转换。<br/>
                  <strong>图片类型：</strong>尝试按图片修复，失败则按视频修复，成功后自动转换类型并迁移文件。<br/>
                  <strong>视频类型：</strong>使用ffprobe获取完整元数据，自动检查并生成缺失的缩略图。<br/>
                  适用于单个文件问题修复、类型错误纠正等场景。
                </p>
              </div>
              <div class="fix-by-id-controls">
                <t-input-number
                  v-model="fixById"
                  :min="1"
                  placeholder="请输入照片ID"
                  style="width: 200px;"
                  :disabled="fixingById"
                />
                <t-button 
                  theme="primary" 
                  :loading="fixingById"
                  :disabled="!fixById || fixById <= 0"
                  @click="handleForceFixById"
                >
                  {{ fixingById ? '修复中...' : '开始修复' }}
                </t-button>
              </div>
            </div>
          </t-card>

          <!-- 修复结果显示 -->
          <t-card v-if="fixResult" title="修复结果">
            <div class="result-content">
              <div class="result-item">
                <span class="result-label">总数：</span>
                <span class="result-value">{{ fixResult.total }}</span>
              </div>
              <div class="result-item">
                <span class="result-label">成功：</span>
                <span class="result-value success">{{ fixResult.success }}</span>
              </div>
              <div class="result-item">
                <span class="result-label">失败：</span>
                <span class="result-value failed">{{ fixResult.failed }}</span>
              </div>
              <div class="result-item">
                <span class="result-label">消息：</span>
                <span class="result-value">{{ fixResult.message }}</span>
              </div>
            </div>
          </t-card>

          <!-- 单个修复结果显示 -->
          <t-card v-if="singleFixResult" title="修复结果">
            <div class="result-content">
              <div class="result-item">
                <span class="result-label">状态：</span>
                <span class="result-value success">{{ singleFixResult.message }}</span>
              </div>
              <div class="result-item">
                <span class="result-label">ID：</span>
                <span class="result-value">{{ singleFixResult.photo.id }}</span>
              </div>
              <div class="result-item">
                <span class="result-label">类型：</span>
                <span class="result-value">{{ singleFixResult.photo.mediaType === 'video' ? '视频' : '图片' }}</span>
              </div>
              <div class="result-item">
                <span class="result-label">尺寸：</span>
                <span class="result-value">{{ singleFixResult.photo.width }} × {{ singleFixResult.photo.height }}</span>
              </div>
              <div v-if="singleFixResult.photo.mediaType === 'video'" class="result-item">
                <span class="result-label">时长：</span>
                <span class="result-value">{{ singleFixResult.photo.duration?.toFixed(2) }}秒</span>
              </div>
              <div class="result-item">
                <span class="result-label">名称：</span>
                <span class="result-value">{{ singleFixResult.photo.name }}</span>
              </div>
            </div>
          </t-card>
        </t-space>
      </div>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { fixZeroDimensionPhotosApi, forceFixPhotoByIdApi } from '@/api'

const fixingPhotos = ref(false)
const fixResult = ref<any>(null)
const fixingById = ref(false)
const fixById = ref<number | undefined>(undefined)
const singleFixResult = ref<any>(null)

const handleFixZeroDimensionPhotos = async () => {
  fixingPhotos.value = true
  fixResult.value = null
  singleFixResult.value = null
  
  try {
    const response = await fixZeroDimensionPhotosApi()
    
    console.log('API Response:', response)
    
    if (response.code === 0) {
      fixResult.value = response.data
      console.log('Fix Result:', fixResult.value)
      MessagePlugin.success('修复完成！')
    } else {
      MessagePlugin.error('修复失败：未知错误')
    }
  } catch (error: any) {
    console.error('修复媒体文件失败:', error)
    MessagePlugin.error('修复失败：' + (error.message || '网络错误'))
  } finally {
    fixingPhotos.value = false
  }
}

const handleForceFixById = async () => {
  if (!fixById.value || fixById.value <= 0) {
    MessagePlugin.warning('请输入有效的照片ID')
    return
  }

  fixingById.value = true
  singleFixResult.value = null
  fixResult.value = null
  
  try {
    const response = await forceFixPhotoByIdApi({ id: fixById.value })
    
    console.log('Force Fix API Response:', response)
    
    if (response.code === 0) {
      singleFixResult.value = response.data
      console.log('Single Fix Result:', singleFixResult.value)
      MessagePlugin.success(`照片/视频 ${fixById.value} 修复成功！`)
    } else {
      MessagePlugin.error('修复失败：未知错误')
    }
  } catch (error: any) {
    console.error('强制修复失败:', error)
    MessagePlugin.error('修复失败：' + (error.message || '网络错误'))
  } finally {
    fixingById.value = false
  }
}
</script>

<style scoped>
.admin-container {
  max-width: 1200px;
  margin: 0 auto;
}

.admin-content {
  padding: 20px 0;
}

.tool-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
}

.tool-info {
  flex: 1;
}

.tool-info h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--td-text-color-primary);
}

.tool-description {
  margin: 0;
  font-size: 14px;
  color: var(--td-text-color-secondary);
  line-height: 1.6;
}

.fix-by-id-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.result-label {
  font-weight: 500;
  color: var(--td-text-color-secondary);
  min-width: 80px;
}

.result-value {
  color: var(--td-text-color-primary);
  font-weight: 500;
}

.result-value.success {
  color: var(--td-success-color);
}

.result-value.failed {
  color: var(--td-error-color);
}
</style>
