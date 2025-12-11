<template>
  <div class="timeline-container">
    <t-card title="时间线" :bordered="false">
      <div class="filter-bar">
        <t-space>
          <t-switch v-model="showStar" label="仅收藏" @change="loadTimeline" />
        </t-space>
      </div>
      
      <t-tree
        v-loading="loading"
        :data="treeData"
        :expanded="expandedKeys"
        :activable="true"
        hover
        transition
        @click="handleNodeClick"
      />
    </t-card>
    
    <!-- 照片预览对话框 -->
    <t-dialog
      v-model:visible="previewVisible"
      :header="`${selectedNode?.title} 的照片`"
      width="90%"
      :footer="false"
      destroy-on-close
    >
      <div v-loading="photoLoading" class="photos-preview">
        <div class="photos-grid">
          <div
            v-for="photo in selectedPhotos"
            :key="photo.id"
            class="photo-item"
          >
            <img
              :src="`/cos/${photo.thumb}`"
              :alt="photo.name"
              class="photo-thumb"
              @click="viewPhoto(photo)"
            />
          </div>
        </div>
        
        <div class="pagination-wrapper">
          <t-pagination
            v-model="photoPagination.current"
            v-model:page-size="photoPagination.pageSize"
            :total="photoPagination.total"
            @change="loadNodePhotos"
          />
        </div>
      </div>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getPicsDateApi, getPicsApi, TreeNode, Photo } from '@/api'

const loading = ref(false)
const showStar = ref(false)
const treeData = ref<any[]>([])
const expandedKeys = ref<number[]>([])

const previewVisible = ref(false)
const photoLoading = ref(false)
const selectedNode = ref<TreeNode | null>(null)
const selectedPhotos = ref<Photo[]>([])
const photoPagination = reactive({
  current: 1,
  pageSize: 50,
  total: 0
})

const loadTimeline = async () => {
  loading.value = true
  try {
    const res = await getPicsDateApi({
      trashed: false,
      star: showStar.value
    })
    
    if (res.code === 0) {
      treeData.value = convertToTreeData(res.data)
    }
  } catch (error) {
    console.error('Load timeline error:', error)
  } finally {
    loading.value = false
  }
}

const convertToTreeData = (nodes: TreeNode[]): any[] => {
  return nodes.map(node => ({
    value: node.id,
    label: node.title,
    children: node.children ? convertToTreeData(node.children) : undefined,
    raw: node
  }))
}

const handleNodeClick = (context: any) => {
  const node = context.node?.raw as TreeNode
  if (!node) return
  
  selectedNode.value = node
  photoPagination.current = 1
  loadNodePhotos()
  previewVisible.value = true
}

const loadNodePhotos = async () => {
  if (!selectedNode.value) return
  
  photoLoading.value = true
  try {
    const dateKey = selectedNode.value.id.toString()
    const res = await getPicsApi({
      dateKey,
      trashed: false,
      star: showStar.value || undefined,
      start: (photoPagination.current - 1) * photoPagination.pageSize,
      size: photoPagination.pageSize,
      order: '-taken_date'
    })
    
    if (res.code === 0) {
      selectedPhotos.value = res.data.data
      photoPagination.total = res.data.totalRows
    }
  } catch (error) {
    console.error('Load node photos error:', error)
  } finally {
    photoLoading.value = false
  }
}

const viewPhoto = (photo: Photo) => {
  window.open(`/cos/small/${photo.name}`, '_blank')
}

onMounted(() => {
  loadTimeline()
})
</script>

<style scoped>
.timeline-container {
  height: 100%;
}

.filter-bar {
  margin-bottom: 16px;
}

.photos-preview {
  max-height: 70vh;
  overflow-y: auto;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.photo-item {
  aspect-ratio: 1;
  overflow: hidden;
  border-radius: 4px;
  cursor: pointer;
  transition: transform 0.2s;
}

.photo-item:hover {
  transform: scale(1.05);
}

.photo-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}
</style>
