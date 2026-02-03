<template>
  <t-card title="相册" :bordered="false" class="folder-tree-card">
    <template #actions>
      <t-button
        size="small"
        variant="text"
        @click="refreshTree"
      >
        <template #icon><t-icon name="refresh" /></template>
      </t-button>
    </template>
    
    <div v-loading="loading" class="tree-container">
      <t-tree
        v-if="treeData.length > 0"
        :key="treeKey"
        :data="treeData"
        :keys="treeKeys"
        :expand-all="false"
        :expand-level="0"
        :activable="true"
        :active-multiple="false"
        :value="selectedId ? [selectedId] : []"
        v-model:expanded="expandedKeys"
        :lazy="true"
        :load="loadChildren"
        hover
        @active="handleNodeClick"
        @expand="handleExpand"
      >
        <template #label="{ node }">
          <span class="tree-node-label">
            <t-icon name="folder" size="16px" />
            <span class="tree-node-title">{{ node.label }}</span>
          </span>
        </template>
      </t-tree>
      
      <t-empty v-else description="暂无相册" />
    </div>
  </t-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFoldersDataApi, FolderData } from '@/api'

interface TreeNode {
  value: number
  label: string
  children?: TreeNode[] | boolean
  loading?: boolean
  leaf?: boolean
}

interface Props {
  star?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  star: false
})

const emit = defineEmits<{
  'select': [folderId: number | null, folderPath: string | null]
}>()

const loading = ref(false)
const treeData = ref<TreeNode[]>([])
const selectedId = ref<number | null>(null)
const folderMap = ref<Map<number, FolderData>>(new Map())
const expandedKeys = ref<number[]>([])
const treeKey = ref(0)

const treeKeys = {
  value: 'value',
  label: 'label',
  children: 'children'
}

// 加载根节点
const loadRootNodes = async () => {
  loading.value = true
  try {
    const res = await getFoldersDataApi({
      trashed: false,
      star: props.star,
      parent_id: -1
    })
    
    if (res.code === 0) {
      // 保存到 folderMap
      res.data.forEach(folder => {
        folderMap.value.set(folder.id, folder)
      })
      
      // 创建树节点，根据 has_children 字段决定是否显示 + 号
      // 在懒加载模式下：
      // - 有子节点：设置 children: true（显示展开按钮，但不加载子节点）
      // - 无子节点：设置 children: []（不显示展开按钮）
      treeData.value = res.data.map(folder => ({
        value: folder.id,
        label: folder.title,
        children: folder.has_children ? true : []
      }))
    }
  } catch (error) {
    console.error('Load root nodes error:', error)
  } finally {
    loading.value = false
  }
}

// 懒加载子节点
const loadChildren = async (node: any): Promise<TreeNode[]> => {
  const nodeId = node.value
  
  try {
    // 调用 API 获取子节点
    const res = await getFoldersDataApi({
      trashed: false,
      star: props.star,
      parent_id: nodeId
    })
    
    if (res.code === 0) {
      if (res.data.length > 0) {
        // 有子节点，添加子节点数据
        res.data.forEach(folder => {
          folderMap.value.set(folder.id, folder)
        })
        
        return res.data.map(folder => ({
          value: folder.id,
          label: folder.title,
          children: folder.has_children ? true : []
        }))
      } else {
        // 没有子节点，返回空数组（+ 号会消失）
        return []
      }
    }
    
    return []
  } catch (error) {
    console.error('Load children error:', error)
    // 加载失败时返回空数组
    return []
  }
}

const handleNodeClick = (value: number[], context: any) => {
  if (value.length > 0) {
    const nodeId = value[0]
    selectedId.value = nodeId
    const folder = folderMap.value.get(nodeId)
    emit('select', nodeId, folder?.path || null)
  } else {
    selectedId.value = null
    emit('select', null, null)
  }
}

// 处理展开事件
const handleExpand = (value: number[], context: any) => {
  console.log('Expand event:', value, context)
}

// 刷新树（清空缓存，重新加载）
const refreshTree = () => {
  folderMap.value.clear()
  selectedId.value = null
  expandedKeys.value = []
  treeKey.value++ // 强制重新渲染树组件
  loadRootNodes()
}

// 清除选择
const clearSelection = () => {
  selectedId.value = null
}

// 暴露给父组件的方法
const loadTree = () => {
  loadRootNodes()
}

defineExpose({
  loadTree,
  clearSelection,
  refreshTree
})

onMounted(() => {
  loadRootNodes()
})
</script>

<style scoped>
.folder-tree-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.folder-tree-card :deep(.t-card__body) {
  flex: 1;
  overflow: hidden;
  padding: 16px;
}

.tree-container {
  height: 100%;
  overflow-y: auto;
}

.tree-node-label {
  display: flex;
  align-items: center;
  gap: 8px;
}

.tree-node-title {
  font-size: 14px;
}

.tree-container :deep(.t-tree) {
  background: transparent;
}

/* 增强层级视觉效果 */
.tree-container :deep(.t-tree__item) {
  padding: 6px 8px;
  margin: 2px 0;
  border-radius: 4px;
  transition: all 0.2s;
  position: relative;
}

/* 添加左侧缩进指示线 */
.tree-container :deep(.t-tree__item::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 2px;
  background: transparent;
  transition: background-color 0.2s;
}

/* 二级节点 */
.tree-container :deep(.t-tree__item[data-level="1"]) {
  padding-left: 20px;
  background-color: rgba(0, 0, 0, 0.01);
}

.tree-container :deep(.t-tree__item[data-level="1"]::before) {
  background: rgba(var(--td-brand-color-rgb), 0.1);
}

/* 三级节点 */
.tree-container :deep(.t-tree__item[data-level="2"]) {
  padding-left: 36px;
  background-color: rgba(0, 0, 0, 0.02);
}

.tree-container :deep(.t-tree__item[data-level="2"]::before) {
  background: rgba(var(--td-brand-color-rgb), 0.15);
}

/* 四级节点 */
.tree-container :deep(.t-tree__item[data-level="3"]) {
  padding-left: 52px;
  background-color: rgba(0, 0, 0, 0.03);
}

.tree-container :deep(.t-tree__item[data-level="3"]::before) {
  background: rgba(var(--td-brand-color-rgb), 0.2);
}

/* 五级节点 */
.tree-container :deep(.t-tree__item[data-level="4"]) {
  padding-left: 68px;
  background-color: rgba(0, 0, 0, 0.04);
}

.tree-container :deep(.t-tree__item[data-level="4"]::before) {
  background: rgba(var(--td-brand-color-rgb), 0.25);
}

/* 六级及以上 */
.tree-container :deep(.t-tree__item[data-level="5"]),
.tree-container :deep(.t-tree__item[data-level="6"]) {
  padding-left: 84px;
  background-color: rgba(0, 0, 0, 0.05);
}

.tree-container :deep(.t-tree__item[data-level="5"]::before),
.tree-container :deep(.t-tree__item[data-level="6"]::before) {
  background: rgba(var(--td-brand-color-rgb), 0.3);
}

/* 鼠标悬停效果 */
.tree-container :deep(.t-tree__item:hover) {
  background-color: var(--td-bg-color-container-hover);
  transform: translateX(2px);
}

.tree-container :deep(.t-tree__item:hover::before) {
  width: 3px;
  background: var(--td-brand-color) !important;
}

/* 选中状态 */
.tree-container :deep(.t-tree__item.t-is-active) {
  background-color: var(--td-brand-color-light);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.tree-container :deep(.t-tree__item.t-is-active::before) {
  width: 3px;
  background: var(--td-brand-color) !important;
}

.tree-container :deep(.t-tree__item.t-is-active .tree-node-title) {
  color: var(--td-brand-color);
  font-weight: 500;
}

/* 展开/折叠图标增强 */
.tree-container :deep(.t-tree__icon) {
  color: var(--td-text-color-secondary);
  transition: all 0.2s;
}

.tree-container :deep(.t-tree__item:hover .t-tree__icon) {
  color: var(--td-brand-color);
  transform: scale(1.1);
}

/* 文件夹图标颜色 */
.tree-node-label .t-icon {
  color: var(--td-warning-color);
  transition: color 0.2s;
}

.tree-container :deep(.t-tree__item.t-is-active) .tree-node-label .t-icon {
  color: var(--td-brand-color);
}

/* 根节点样式 */
.tree-container :deep(.t-tree > .t-tree__item) {
  font-weight: 500;
  margin-bottom: 4px;
}

/* 修复收起节点后的空白问题 */
.tree-container :deep(.t-tree__children) {
  overflow: hidden;
  transition: height 0.3s ease;
}

.tree-container :deep(.t-tree__item--hidden) {
  display: none !important;
}

</style>
