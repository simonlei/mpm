<template>
  <t-layout class="main-layout">
    <t-header class="main-header">
      <div class="header-left">
        <!-- 菜单触发区域 -->
        <div 
          class="menu-trigger-area"
          @mouseenter="showMenu = true"
        >
          <t-icon name="menu-fold" size="20px" />
          <h2 class="logo">MPM</h2>
        </div>
        
        <!-- 下拉菜单 -->
        <div 
          v-show="showMenu"
          class="menu-dropdown"
          @mouseleave="showMenu = false"
        >
          <t-menu
            :value="activeMenu"
            theme="light"
            @change="handleMenuChange"
          >
            <t-menu-item value="photos">
              <template #icon>
                <t-icon name="image" />
              </template>
              照片
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
            
            <t-menu-item value="albums">
              <template #icon>
                <t-icon name="view-module" />
              </template>
              相册
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
            
            <!-- 管理菜单 - 仅管理员可见 -->
            <t-menu-item v-if="userStore.user?.is_admin" value="admin">
              <template #icon>
                <t-icon name="setting" />
              </template>
              管理
            </t-menu-item>
            
            <t-menu-item value="logout" @click="handleLogout">
              <template #icon>
                <t-icon name="poweroff" />
              </template>
              退出登录
            </t-menu-item>
          </t-menu>
        </div>
      </div>
      
      <div class="header-right">
        <span class="user-info">{{ userStore.user?.name }}</span>
      </div>
    </t-header>
    
    <t-content class="main-content">
      <router-view />
    </t-content>
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
const showMenu = ref(false)

watch(() => route.name, (newName) => {
  if (newName && typeof newName === 'string') {
    activeMenu.value = newName.toLowerCase()
  }
}, { immediate: true })

const handleMenuChange = (value: string) => {
  if (value === 'logout') return // 退出登录不跳转
  showMenu.value = false // 点击菜单后隐藏
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
  display: flex;
  flex-direction: column;
}

.main-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid var(--td-border-level-1-color);
  flex-shrink: 0;
  position: relative;
  z-index: 2000;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
  position: relative;
}

.menu-trigger-area {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.menu-trigger-area:hover {
  background-color: var(--td-bg-color-container-hover);
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: var(--td-brand-color);
  margin: 0;
  white-space: nowrap;
}

.menu-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 8px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 200px;
  z-index: 2100;
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.menu-dropdown :deep(.t-menu) {
  border: none;
  background: transparent;
}

.menu-dropdown :deep(.t-menu__item) {
  padding: 12px 20px;
}

.menu-dropdown :deep(.t-menu__item:hover) {
  background-color: var(--td-bg-color-container-hover);
}

.menu-dropdown :deep(.t-menu__item.t-is-active) {
  color: var(--td-brand-color);
  background-color: var(--td-brand-color-light);
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
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: var(--td-bg-color-container);
}
</style>
