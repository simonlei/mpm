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
        </t-space>
      </div>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { fixZeroDimensionPhotosApi } from '@/api'

const fixingPhotos = ref(false)
const fixResult = ref<any>(null)

const handleFixZeroDimensionPhotos = async () => {
  fixingPhotos.value = true
  fixResult.value = null
  
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
