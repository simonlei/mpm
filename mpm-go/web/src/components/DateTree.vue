<template>
  <div class="date-tree-container">
    <div class="tree-header">
      <h3>时间线</h3>
      <t-button
        v-if="selectedKey"
        size="small"
        variant="text"
        @click="clearSelection"
      >
        清除筛选
      </t-button>
    </div>
    
    <div class="tree-content">
      <t-loading v-if="loading" text="加载中..." />
      
      <t-tree
        v-else
        :data="treeData"
        :keys="{ value: 'id', label: 'title', children: 'children' }"
        :activable="true"
        :active="selectedKey ? [selectedKey] : []"
        :expand-all="false"
        :expand-level="1"
        hover
        transition
        @active="handleNodeClick"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getPicsDateApi, TreeNode } from '@/api'

interface Props {
  star?: boolean
  trashed?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  star: false,
  trashed: false
})

const emit = defineEmits<{
  'select': [nodeId: number | null]
}>()

const loading = ref(false)
const treeData = ref<TreeNode[]>([])
const selectedKey = ref<number | null>(null)

// 加载时间线数据
const loadDateTree = async () => {
  loading.value = true
  try {
    const res = await getPicsDateApi({
      trashed: props.trashed,
      star: props.star
    })
    
    if (res.code === 0) {
      // 直接使用后台返回的树形结构
      treeData.value = res.data
    }
  } catch (error) {
    console.error('Load date tree error:', error)
  } finally {
    loading.value = false
  }
}

// 处理节点点击
const handleNodeClick = (value: number[]) => {
  if (value.length === 0) {
    clearSelection()
    return
  }
  
  const nodeId = value[0]
  selectedKey.value = nodeId
  
  // 触发选择事件，传递节点 ID
  emit('select', nodeId)
}

// 清除选择
const clearSelection = () => {
  selectedKey.value = null
  emit('select', null)
}

// 监听 props 变化，重新加载数据
watch(() => [props.star, props.trashed], () => {
  clearSelection()
  loadDateTree()
}, { deep: true })

onMounted(() => {
  loadDateTree()
})

// 暴露方法
defineExpose({
  refresh: loadDateTree,
  clearSelection
})
</script>

<style scoped>
.date-tree-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.tree-header {
  padding: 16px;
  border-bottom: 1px solid var(--td-component-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: var(--td-text-color-primary);
}

.tree-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.tree-node-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
}

.photo-count {
  font-size: 12px;
  color: var(--td-text-color-placeholder);
  background: var(--td-bg-color-container);
  padding: 2px 8px;
  border-radius: 10px;
  margin-left: 8px;
}

/* 自定义滚动条 */
.tree-content::-webkit-scrollbar {
  width: 6px;
}

.tree-content::-webkit-scrollbar-track {
  background: transparent;
}

.tree-content::-webkit-scrollbar-thumb {
  background: var(--td-scrollbar-color);
  border-radius: 3px;
}

.tree-content::-webkit-scrollbar-thumb:hover {
  background: var(--td-scrollbar-hover-color);
}
</style>
