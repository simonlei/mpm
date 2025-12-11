<template>
  <t-layout class="main-layout">
    <t-aside :width="200">
      <div class="sidebar">
        <div class="sidebar-header">
          <h2>MPM</h2>
        </div>
        
        <t-menu
          :value="activeMenu"
          :collapsed="false"
          theme="light"
          @change="handleMenuChange"
        >
          <t-menu-item value="photos">
            <template #icon>
              <t-icon name="image" />
            </template>
            照片
          </t-menu-item>
          
          <t-menu-item value="timeline">
            <template #icon>
              <t-icon name="time" />
            </template>
            时间线
          </t-menu-item>
          
          <t-menu-item value="map">
            <template #icon>
              <t-icon name="location" />
            </template>
            地图
          </t-menu-item>
          
          <t-menu-item value="faces">
            <template #icon>
              <t-icon name="user" />
            </template>
            人脸
          </t-menu-item>
          
          <t-menu-item value="activities">
            <template #icon>
              <t-icon name="calendar" />
            </template>
            活动
          </t-menu-item>
          
          <t-menu-item value="folders">
            <template #icon>
              <t-icon name="folder" />
            </template>
            文件夹
          </t-menu-item>
          
          <t-menu-item value="upload">
            <template #icon>
              <t-icon name="upload" />
            </template>
            上传
          </t-menu-item>
          
          <t-menu-item value="trash">
            <template #icon>
              <t-icon name="delete" />
            </template>
            回收站
          </t-menu-item>
        </t-menu>
        
        <div class="sidebar-footer">
          <t-button
            theme="default"
            variant="text"
            block
            @click="handleLogout"
          >
            <template #icon>
              <t-icon name="poweroff" />
            </template>
            退出登录
          </t-button>
        </div>
      </div>
    </t-aside>
    
    <t-layout>
      <t-header class="main-header">
        <div class="header-left">
          <h3>{{ currentTitle }}</h3>
        </div>
        <div class="header-right">
          <span class="user-info">{{ userStore.user?.name }}</span>
        </div>
      </t-header>
      
      <t-content class="main-content">
        <router-view />
      </t-content>
    </t-layout>
  </t-layout>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { DialogPlugin } from 'tdesign-vue-next'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = ref('photos')

const currentTitle = computed(() => {
  return route.meta.title || '照片'
})

watch(() => route.name, (newName) => {
  if (newName && typeof newName === 'string') {
    activeMenu.value = newName.toLowerCase()
  }
}, { immediate: true })

const handleMenuChange = (value: string) => {
  router.push(`/${value}`)
}

const handleLogout = () => {
  const dialog = DialogPlugin.confirm({
    header: '退出登录',
    body: '确定要退出登录吗？',
    onConfirm: () => {
      userStore.logout()
      router.push('/login')
      dialog.destroy()
    }
  })
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid var(--td-border-level-1-color);
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--td-border-level-1-color);
}

.sidebar-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--td-brand-color);
}

.sidebar-footer {
  margin-top: auto;
  padding: 16px;
  border-top: 1px solid var(--td-border-level-1-color);
}

.main-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--td-border-level-1-color);
}

.header-left h3 {
  font-size: 18px;
  font-weight: 500;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  font-size: 14px;
  color: var(--td-text-color-secondary);
}

.main-content {
  padding: 24px;
  overflow-y: auto;
  background: var(--td-bg-color-container);
}
</style>
