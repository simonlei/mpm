<template>
  <div class="folders-container">
    <t-card title="文件夹管理" :bordered="false">
      <div class="filter-bar">
        <t-space>
          <t-switch v-model="showStar" label="仅收藏" @change="loadFolders" />
        </t-space>
      </div>
      
      <t-table
        v-loading="loading"
        :data="folders"
        :columns="columns"
        row-key="id"
        :pagination="pagination"
      >
        <template #operation="{ row }">
          <t-space>
            <t-button
              theme="primary"
              variant="text"
              size="small"
              @click="viewFolderPhotos(row)"
            >
              查看照片
            </t-button>
            
            <t-button
              theme="default"
              variant="text"
              size="small"
              @click="batchUpdateDate(row)"
            >
              批量改日期
            </t-button>
            
            <t-button
              theme="default"
              variant="text"
              size="small"
              @click="batchUpdateGis(row)"
            >
              批量改位置
            </t-button>
            
            <t-popconfirm
              content="确定移至回收站吗？"
              @confirm="trashFolder(row)"
            >
              <t-button
                theme="danger"
                variant="text"
                size="small"
              >
                回收站
              </t-button>
            </t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </t-card>
    
    <!-- 批量修改日期对话框 -->
    <t-dialog
      v-model:visible="dateDialogVisible"
      header="批量修改日期"
      :on-confirm="confirmUpdateDate"
      :confirm-btn="{ loading: updating }"
    >
      <t-form label-align="top">
        <t-form-item label="新日期">
          <t-date-picker
            v-model="newDate"
            enable-time-picker
            format="YYYY-MM-DD HH:mm:ss"
            value-type="YYYY-MM-DD HH:mm:ss"
            clearable
            style="width: 100%"
          />
        </t-form-item>
      </t-form>
    </t-dialog>
    
    <!-- 批量修改位置对话框 -->
    <t-dialog
      v-model:visible="gisDialogVisible"
      header="批量修改位置"
      :on-confirm="confirmUpdateGis"
      :confirm-btn="{ loading: updating }"
    >
      <t-form label-align="top">
        <t-form-item label="纬度">
          <t-input-number
            v-model="newLatitude"
            placeholder="请输入纬度"
            :decimal-places="6"
            style="width: 100%"
          />
        </t-form-item>
        
        <t-form-item label="经度">
          <t-input-number
            v-model="newLongitude"
            placeholder="请输入经度"
            :decimal-places="6"
            style="width: 100%"
          />
        </t-form-item>
      </t-form>
    </t-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { useRouter } from 'vue-router'
import {
  getFoldersDataApi,
  updateFolderDateApi,
  updateFolderGisApi,
  switchTrashFolderApi,
  FolderData
} from '@/api'

const router = useRouter()
const loading = ref(false)
const showStar = ref(false)
const folders = ref<FolderData[]>([])

const columns = [
  { colKey: 'id', title: 'ID', width: 80 },
  { colKey: 'path', title: '路径', ellipsis: true },
  { colKey: 'title', title: '标题', width: 200 },
  { colKey: 'operation', title: '操作', width: 400 }
]

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const dateDialogVisible = ref(false)
const gisDialogVisible = ref(false)
const updating = ref(false)
const currentFolder = ref<FolderData | null>(null)
const newDate = ref('')
const newLatitude = ref(0)
const newLongitude = ref(0)

const loadFolders = async () => {
  loading.value = true
  try {
    const res = await getFoldersDataApi({
      trashed: false,
      star: showStar.value,
      parentId: -1
    })
    
    if (res.code === 0) {
      folders.value = res.data
      pagination.total = res.data.length
    }
  } catch (error) {
    console.error('Load folders error:', error)
  } finally {
    loading.value = false
  }
}

const viewFolderPhotos = (folder: FolderData) => {
  router.push({
    name: 'Photos',
    query: { path: folder.path }
  })
}

const batchUpdateDate = (folder: FolderData) => {
  currentFolder.value = folder
  newDate.value = ''
  dateDialogVisible.value = true
}

const confirmUpdateDate = async () => {
  if (!currentFolder.value || !newDate.value) {
    MessagePlugin.warning('请选择日期')
    return
  }
  
  updating.value = true
  try {
    const res = await updateFolderDateApi({
      path: currentFolder.value.path,
      toDate: newDate.value
    })
    
    if (res.code === 0) {
      MessagePlugin.success(`已更新 ${res.data} 张照片`)
      dateDialogVisible.value = false
    }
  } catch (error) {
    console.error('Update folder date error:', error)
  } finally {
    updating.value = false
  }
}

const batchUpdateGis = (folder: FolderData) => {
  currentFolder.value = folder
  newLatitude.value = 0
  newLongitude.value = 0
  gisDialogVisible.value = true
}

const confirmUpdateGis = async () => {
  if (!currentFolder.value) return
  
  if (newLatitude.value === 0 || newLongitude.value === 0) {
    MessagePlugin.warning('请输入有效的经纬度')
    return
  }
  
  updating.value = true
  try {
    const res = await updateFolderGisApi({
      path: currentFolder.value.path,
      latitude: newLatitude.value,
      longitude: newLongitude.value
    })
    
    if (res.code === 0) {
      MessagePlugin.success(`已更新 ${res.data} 张照片`)
      gisDialogVisible.value = false
    }
  } catch (error) {
    console.error('Update folder gis error:', error)
  } finally {
    updating.value = false
  }
}

const trashFolder = async (folder: FolderData) => {
  try {
    const res = await switchTrashFolderApi({
      to: true,
      path: folder.path
    })
    
    if (res.code === 0) {
      MessagePlugin.success(`已移动 ${res.data} 张照片到回收站`)
      loadFolders()
    }
  } catch (error) {
    console.error('Trash folder error:', error)
  }
}

onMounted(() => {
  loadFolders()
})
</script>

<style scoped>
.folders-container {
  height: 100%;
}

.filter-bar {
  margin-bottom: 16px;
}
</style>
