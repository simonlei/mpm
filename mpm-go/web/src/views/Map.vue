<template>
  <div class="map-container">
    <t-card :bordered="false" class="map-card">
      <div id="map" class="map-view"></div>
      
      <!-- 照片详情侧边栏 -->
      <t-drawer
        v-model:visible="drawerVisible"
        :title="selectedPhoto ? selectedPhoto.name : '照片详情'"
        :size="800"
        :footer="false"
        destroy-on-close
      >
        <PhotoDetail
          v-if="selectedPhoto"
          :photo="selectedPhoto"
          layout="vertical"
          @update:photo="handlePhotoUpdate"
        >
          <template #tag-editor="{ photo }">
            <t-select
              v-model="currentPhotoTags"
              placeholder="选择或输入标签"
              multiple
              filterable
              creatable
              clearable
              :options="tagOptions"
              @change="handleTagChange"
            />
          </template>
          <template #actions="{ photo }">
            <t-button
              theme="primary"
              block
              @click="toggleStar"
            >
              <template #icon>
                <t-icon :name="photo.star ? 'star-filled' : 'star'" />
              </template>
              {{ photo.star ? '取消收藏' : '收藏' }}
            </t-button>
            
            <t-button
              block
              @click="showActivityDialog"
            >
              <template #icon><t-icon name="calendar" /></template>
              {{ photo.activity ? '修改活动' : '添加到活动' }}
            </t-button>
            
            <t-button
              theme="danger"
              block
              @click="deletePhoto"
            >
              <template #icon><t-icon name="delete" /></template>
              删除
            </t-button>
          </template>
        </PhotoDetail>
      </t-drawer>

      
      <!-- 聚合照片列表侧边栏 -->
      <t-drawer
        v-model:visible="clusterDrawerVisible"
        :title="`聚合照片 (${clusterPhotos.length})`"
        :size="600"
        :footer="false"
      >
        <div class="cluster-photos-grid">
          <div 
            v-for="feature in clusterPhotos" 
            :key="feature.properties.id"
            class="cluster-photo-item"
            @click="viewClusterPhoto(feature)"
          >
            <img
              :src="`/cos/small/${feature.properties.name}`"
              :alt="feature.properties.name"
              class="cluster-photo-thumb"
            />
          </div>
        </div>
      </t-drawer>
      
      <!-- 活动选择对话框 -->
      <t-dialog
        v-model:visible="activityDialogVisible"
        header="选择活动"
        width="500px"
        :on-confirm="confirmAddToActivity"
      >
        <t-select
          v-model="selectedActivityId"
          placeholder="选择活动"
          clearable
          filterable
          style="width: 100%"
        >
          <t-option
            v-for="activity in activities"
            :key="activity.id"
            :value="activity.id"
            :label="activity.name"
          />
        </t-select>
      </t-dialog>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { MessagePlugin } from 'tdesign-vue-next'
import { 
  loadMarkersGeoJsonApi, 
  GeoJsonFeature, 
  Photo, 
  getPhotoByIdApi,
  updateImageApi,
  getAllTagsApi,
  getActivitiesApi,
  Activity,
  trashPhotosApi
} from '@/api'
import PhotoDetail from '@/components/PhotoDetail.vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import 'leaflet.markercluster'
import 'leaflet.markercluster/dist/MarkerCluster.css'
import 'leaflet.markercluster/dist/MarkerCluster.Default.css'

// 修复 Leaflet 默认图标问题
import iconUrl from 'leaflet/dist/images/marker-icon.png'
import iconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png'
import shadowUrl from 'leaflet/dist/images/marker-shadow.png'

delete (L.Icon.Default.prototype as any)._getIconUrl
L.Icon.Default.mergeOptions({
  iconUrl,
  iconRetinaUrl,
  shadowUrl
})

let map: L.Map | null = null
let markerClusterGroup: L.MarkerClusterGroup | null = null
const drawerVisible = ref(false)
const selectedPhoto = ref<Photo | null>(null)
const clusterPhotos = ref<GeoJsonFeature[]>([])
const clusterDrawerVisible = ref(false)

const currentPhotoTags = ref<string[]>([])

// 标签和活动
const allTags = ref<string[]>([])
const activities = ref<Activity[]>([])
const activityDialogVisible = ref(false)
const selectedActivityId = ref<number | null>(null)

// 标签选项
const tagOptions = computed(() => {
  return allTags.value.map(tag => ({
    label: tag,
    value: tag
  }))
})

const initMap = () => {
  map = L.map('map').setView([39.9042, 116.4074], 5)
  
  // 使用高德地图瓦片服务（国内访问快速稳定）
  // 可选图层：
  // - 'vec' 矢量底图
  // - 'img' 影像底图
  // - 'cva' 矢量注记（道路、地名等）
  // - 'cia' 影像注记
  
  // 方案1: 矢量地图（推荐）
  L.tileLayer('https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
    subdomains: ['1', '2', '3', '4'],
    attribution: '© 高德地图'
  }).addTo(map)
  
  // 方案2: 卫星影像（取消注释使用）
  // L.tileLayer('https://webst0{s}.is.autonavi.com/appmaptile?style=6&x={x}&y={y}&z={z}', {
  //   subdomains: ['1', '2', '3', '4'],
  //   attribution: '© 高德地图'
  // }).addTo(map)
  // 
  // // 叠加注记层
  // L.tileLayer('https://webst0{s}.is.autonavi.com/appmaptile?style=8&x={x}&y={y}&z={z}', {
  //   subdomains: ['1', '2', '3', '4']
  // }).addTo(map)
}

const loadMarkers = async () => {
  try {
    const res = await loadMarkersGeoJsonApi()
    
    if (res.features && res.features.length > 0) {
      const markers = res.features
      
      // 创建 MarkerCluster 组
      markerClusterGroup = L.markerClusterGroup({
        // 自定义聚合图标
        iconCreateFunction: (cluster) => {
          const count = cluster.getChildCount()
          let size = 'small'
          let colorClass = 'marker-cluster-small'
          
          if (count > 100) {
            size = 'large'
            colorClass = 'marker-cluster-large'
          } else if (count > 10) {
            size = 'medium'
            colorClass = 'marker-cluster-medium'
          }
          
          return L.divIcon({
            html: `<div><span>${count}</span></div>`,
            className: `marker-cluster ${colorClass}`,
            iconSize: L.point(40, 40)
          })
        },
        // 点击聚合点时显示照片列表
        clusterClick: (e: any) => {
          const cluster = e.layer
          const childMarkers = cluster.getAllChildMarkers()
          clusterPhotos.value = childMarkers.map((m: any) => m.options.feature)
          clusterDrawerVisible.value = true
        },
        maxClusterRadius: 80, // 聚合半径（像素）
        spiderfyOnMaxZoom: true, // 最大缩放时展开聚合
        showCoverageOnHover: false, // 悬停时不显示覆盖范围
        zoomToBoundsOnClick: false // 点击时不自动缩放
      })
      
      markers.forEach((feature: GeoJsonFeature) => {
        const { coordinates } = feature.geometry
        const marker = L.marker([coordinates[1], coordinates[0]], {
          feature: feature
        } as any)
        
        marker.on('click', async () => {
          await loadPhotoDetail(feature.properties.id)
          drawerVisible.value = true
        })
        
        markerClusterGroup!.addLayer(marker)
      })
      
      map!.addLayer(markerClusterGroup)
      
      // 自动调整视图以显示所有标记
      const bounds = L.latLngBounds(
        markers.map(f => [f.geometry.coordinates[1], f.geometry.coordinates[0]] as [number, number])
      )
      map?.fitBounds(bounds, { padding: [50, 50] })
    }
  } catch (error) {
    console.error('Load markers error:', error)
  }
}

const viewClusterPhoto = async (feature: GeoJsonFeature) => {
  clusterDrawerVisible.value = false
  await loadPhotoDetail(feature.properties.id)
  drawerVisible.value = true
}

// 加载照片详情
const loadPhotoDetail = async (photoId: number) => {
  try {
    const res = await getPhotoByIdApi({ id: photoId })
    
    if (res.code === 0 && res.data) {
      selectedPhoto.value = res.data
      currentPhotoTags.value = res.data.tags ? res.data.tags.split(',').filter(t => t.trim()) : []
    } else {
      MessagePlugin.error('未找到照片信息')
    }
  } catch (error) {
    console.error('Load photo detail error:', error)
    MessagePlugin.error('加载照片详情失败')
  }
}

// 处理照片更新
const handlePhotoUpdate = (updatedPhoto: Photo) => {
  selectedPhoto.value = updatedPhoto
}

// 标签变化
const handleTagChange = async (value: string[]) => {
  if (!selectedPhoto.value) return
  
  try {
    const tagString = value.filter(t => t.trim()).join(',')
    
    await updateImageApi({
      id: selectedPhoto.value.id,
      tags: tagString
    })
    
    selectedPhoto.value.tags = tagString
    
    // 添加新标签到列表
    value.forEach(tag => {
      if (tag && !allTags.value.includes(tag)) {
        allTags.value.push(tag)
      }
    })
    
    MessagePlugin.success('标签已更新')
  } catch (error) {
    console.error('Update tag error:', error)
    MessagePlugin.error('标签更新失败')
  }
}

// 切换收藏
const toggleStar = async () => {
  if (!selectedPhoto.value) return
  
  try {
    await updateImageApi({
      id: selectedPhoto.value.id,
      star: !selectedPhoto.value.star
    })
    selectedPhoto.value.star = !selectedPhoto.value.star
    MessagePlugin.success(selectedPhoto.value.star ? '已收藏' : '已取消收藏')
  } catch (error) {
    console.error('Toggle star error:', error)
    MessagePlugin.error('操作失败')
  }
}

// 显示活动对话框
const showActivityDialog = () => {
  if (!selectedPhoto.value) return
  selectedActivityId.value = selectedPhoto.value.activity || null
  activityDialogVisible.value = true
}

// 确认添加到活动
const confirmAddToActivity = async () => {
  if (!selectedPhoto.value || (!selectedActivityId.value && selectedActivityId.value !== 0)) {
    MessagePlugin.warning('请选择活动')
    return
  }
  
  try {
    await updateImageApi({
      id: selectedPhoto.value.id,
      activity: selectedActivityId.value
    })
    selectedPhoto.value.activity = selectedActivityId.value
    MessagePlugin.success('已添加到活动')
    activityDialogVisible.value = false
  } catch (error) {
    console.error('Add to activity error:', error)
    MessagePlugin.error('操作失败')
  }
}

// 删除照片
const deletePhoto = async () => {
  if (!selectedPhoto.value) return
  
  try {
    await trashPhotosApi([selectedPhoto.value.name])
    MessagePlugin.success('已移至回收站')
    drawerVisible.value = false
    // 重新加载地图标记
    await loadMarkers()
  } catch (error) {
    console.error('Delete photo error:', error)
    MessagePlugin.error('删除失败')
  }
}

onMounted(() => {
  initMap()
  loadMarkers()
  loadAllTags()
  loadActivities()
})

// 加载所有标签
const loadAllTags = async () => {
  try {
    const res = await getAllTagsApi()
    if (res.code === 0) {
      allTags.value = res.data
    }
  } catch (error) {
    console.error('Load tags error:', error)
  }
}

// 加载所有活动
const loadActivities = async () => {
  try {
    const res = await getActivitiesApi()
    if (res.code === 0) {
      activities.value = res.data
    }
  } catch (error) {
    console.error('Load activities error:', error)
  }
}

onBeforeUnmount(() => {
  if (markerClusterGroup) {
    markerClusterGroup.clearLayers()
    markerClusterGroup = null
  }
  if (map) {
    map.remove()
    map = null
  }
})
</script>

<style scoped>
.map-container {
  height: 100%;
}

.map-card {
  height: 100%;
}

.map-view {
  width: 100%;
  height: calc(100vh - 160px);
  border-radius: 8px;
  overflow: hidden;
}

/* 聚合照片网格 */
.cluster-photos-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 8px;
}

.cluster-photo-item {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
  aspect-ratio: 1;
}

.cluster-photo-item:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.cluster-photo-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 自定义聚合点样式 */
:deep(.marker-cluster) {
  background-clip: padding-box;
  border-radius: 50%;
}

:deep(.marker-cluster div) {
  width: 30px;
  height: 30px;
  margin-left: 5px;
  margin-top: 5px;
  text-align: center;
  border-radius: 50%;
  font-weight: 700;
  font-size: 12px;
  line-height: 30px;
  color: #fff;
}

:deep(.marker-cluster-small) {
  background-color: rgba(110, 204, 57, 0.6);
}

:deep(.marker-cluster-small div) {
  background-color: rgba(110, 204, 57, 0.8);
}

:deep(.marker-cluster-medium) {
  background-color: rgba(255, 165, 0, 0.6);
}

:deep(.marker-cluster-medium div) {
  background-color: rgba(255, 165, 0, 0.8);
}

:deep(.marker-cluster-large) {
  background-color: rgba(255, 87, 51, 0.6);
}

:deep(.marker-cluster-large div) {
  background-color: rgba(255, 87, 51, 0.8);
}

/* 聚合点悬停效果 */
:deep(.marker-cluster:hover) {
  transform: scale(1.1);
  transition: transform 0.2s ease;
}
</style>
