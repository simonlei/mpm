<template>
  <div class="map-container">
    <t-card :bordered="false" class="map-card">
      <div id="map" class="map-view"></div>
      
      <!-- 照片预览侧边栏 -->
      <t-drawer
        v-model:visible="drawerVisible"
        title="照片详情"
        :size="400"
        :footer="false"
      >
        <div v-if="selectedMarker" class="marker-detail">
          <img
            :src="`/cos/small/${selectedMarker.name}`"
            :alt="selectedMarker.name"
            class="detail-image"
          />
          
          <t-descriptions :column="1" bordered>
            <t-descriptions-item label="位置">
              {{ selectedMarker.latitude.toFixed(6) }}, {{ selectedMarker.longitude.toFixed(6) }}
            </t-descriptions-item>
          </t-descriptions>
          
          <t-button
            theme="primary"
            block
            style="margin-top: 16px"
            @click="viewFullPhoto"
          >
            查看完整照片
          </t-button>
        </div>
      </t-drawer>
    </t-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { loadMarkersGeoJsonApi, GeoJsonFeature } from '@/api'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

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
const drawerVisible = ref(false)
const selectedMarker = ref<GeoJsonFeature['properties'] | null>(null)

const initMap = () => {
  map = L.map('map').setView([39.9042, 116.4074], 5)
  
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors'
  }).addTo(map)
}

const loadMarkers = async () => {
  try {
    const res = await loadMarkersGeoJsonApi()
    
    if (res.features && res.features.length > 0) {
      const markers = res.features
      
      markers.forEach((feature: GeoJsonFeature) => {
        const { coordinates } = feature.geometry
        const marker = L.marker([coordinates[1], coordinates[0]])
        
        marker.on('click', () => {
          selectedMarker.value = feature.properties
          drawerVisible.value = true
        })
        
        marker.addTo(map!)
      })
      
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

const viewFullPhoto = () => {
  if (selectedMarker.value) {
    window.open(`/cos/small/${selectedMarker.value.name}`, '_blank')
  }
}

onMounted(() => {
  initMap()
  loadMarkers()
})

onBeforeUnmount(() => {
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

.marker-detail {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-image {
  width: 100%;
  border-radius: 8px;
}
</style>
