<template>
  <div class="activities-container">
    <t-card title="活动管理" :bordered="false">
      <template #actions>
        <t-button theme="primary" @click="createActivity">
          <template #icon><t-icon name="add" /></template>
          新建活动
        </t-button>
      </template>
      
      <t-table
        v-loading="loading"
        :data="activities"
        :columns="columns"
        row-key="id"
        :pagination="pagination"
        @page-change="loadActivities"
      >
        <template #startDate="{ row }">
          {{ formatDate(row.start_date) }}
        </template>
        
        <template #endDate="{ row }">
          {{ formatDate(row.end_date) }}
        </template>
        
        <template #location="{ row }">
          <span v-if="row.latitude && row.longitude">
            {{ row.latitude.toFixed(4) }}, {{ row.longitude.toFixed(4) }}
          </span>
          <span v-else class="text-placeholder">-</span>
        </template>
        
        <template #operation="{ row }">
          <t-space>
            <t-button
              theme="primary"
              variant="text"
              size="small"
              @click="editActivity(row)"
            >
              编辑
            </t-button>
            
            <t-popconfirm
              content="确定删除此活动吗？"
              @confirm="deleteActivity(row)"
            >
              <t-button
                theme="danger"
                variant="text"
                size="small"
              >
                删除
              </t-button>
            </t-popconfirm>
          </t-space>
        </template>
      </t-table>
    </t-card>
    
    <!-- 创建/编辑活动对话框 -->
    <t-dialog
      v-model:visible="dialogVisible"
      :header="editingActivity ? '编辑活动' : '新建活动'"
      width="600px"
      :on-confirm="saveActivity"
      :confirm-btn="{ loading: saving }"
    >
      <t-form ref="formRef" :data="formData" :rules="rules" label-align="top">
        <t-form-item label="活动名称" name="name">
          <t-input v-model="formData.name" placeholder="请输入活动名称" />
        </t-form-item>
        
        <t-form-item label="活动描述" name="description">
          <t-textarea
            v-model="formData.description"
            placeholder="请输入活动描述"
            :autosize="{ minRows: 3, maxRows: 6 }"
          />
        </t-form-item>
        
        <t-form-item label="开始日期" name="start_date">
          <t-date-picker
            v-model="formData.start_date"
            enable-time-picker
            format="YYYY-MM-DD HH:mm:ss"
            value-type="YYYY-MM-DD HH:mm:ss"
            clearable
            style="width: 100%"
          />
        </t-form-item>
        
        <t-form-item label="结束日期" name="end_date">
          <t-date-picker
            v-model="formData.end_date"
            enable-time-picker
            format="YYYY-MM-DD HH:mm:ss"
            value-type="YYYY-MM-DD HH:mm:ss"
            clearable
            style="width: 100%"
          />
        </t-form-item>
        
        <t-form-item label="纬度" name="latitude">
          <t-input-number
            v-model="formData.latitude"
            placeholder="请输入纬度"
            :decimal-places="6"
            style="width: 100%"
          />
        </t-form-item>
        
        <t-form-item label="经度" name="longitude">
          <t-input-number
            v-model="formData.longitude"
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
import {
  getActivitiesApi,
  createOrUpdateActivityApi,
  deleteActivityApi,
  Activity
} from '@/api'
import dayjs from 'dayjs'

const loading = ref(false)
const activities = ref<Activity[]>([])
const dialogVisible = ref(false)
const saving = ref(false)
const editingActivity = ref<Activity | null>(null)

const columns = [
  { colKey: 'id', title: 'ID', width: 80 },
  { colKey: 'name', title: '名称', width: 200 },
  { colKey: 'description', title: '描述', ellipsis: true },
  { colKey: 'startDate', title: '开始日期', width: 120 },
  { colKey: 'endDate', title: '结束日期', width: 120 },
  { colKey: 'location', title: '位置', width: 180 },
  { colKey: 'operation', title: '操作', width: 150 }
]

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0
})

const formRef = ref()
const formData = reactive({
  name: '',
  description: '',
  start_date: '',
  end_date: '',
  latitude: 0,
  longitude: 0
})

const rules = {
  name: [{ required: true, message: '请输入活动名称' }],
  start_date: [{ required: true, message: '请选择开始日期' }],
  end_date: [{ required: true, message: '请选择结束日期' }]
}

const loadActivities = async () => {
  loading.value = true
  try {
    const res = await getActivitiesApi()
    if (res.code === 0) {
      activities.value = res.data
      pagination.total = res.data.length
    }
  } catch (error) {
    console.error('Load activities error:', error)
  } finally {
    loading.value = false
  }
}

const createActivity = () => {
  editingActivity.value = null
  Object.assign(formData, {
    name: '',
    description: '',
    start_date: '',
    end_date: '',
    latitude: 0,
    longitude: 0
  })
  dialogVisible.value = true
}

const editActivity = (activity: Activity) => {
  editingActivity.value = activity
  Object.assign(formData, {
    name: activity.name,
    description: activity.description,
    start_date: activity.start_date,
    end_date: activity.end_date,
    latitude: activity.latitude,
    longitude: activity.longitude
  })
  dialogVisible.value = true
}

const saveActivity = async () => {
  const valid = await formRef.value?.validate()
  if (!valid) return
  
  saving.value = true
  try {
    const activityData: Activity = {
      id: editingActivity.value?.id || 0,
      name: formData.name,
      description: formData.description,
      start_date: formData.start_date,
      end_date: formData.end_date,
      latitude: formData.latitude,
      longitude: formData.longitude
    }
    
    await createOrUpdateActivityApi({
      activity: activityData,
      fromPhoto: 0
    })
    
    MessagePlugin.success(editingActivity.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadActivities()
  } catch (error) {
    console.error('Save activity error:', error)
  } finally {
    saving.value = false
  }
}

const deleteActivity = async (activity: Activity) => {
  try {
    await deleteActivityApi({ id: activity.id })
    MessagePlugin.success('删除成功')
    loadActivities()
  } catch (error) {
    console.error('Delete activity error:', error)
  }
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(() => {
  loadActivities()
})
</script>

<style scoped>
.activities-container {
  height: 100%;
}

.text-placeholder {
  color: var(--td-text-color-placeholder);
}
</style>
