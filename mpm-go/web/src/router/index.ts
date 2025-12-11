import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/photos',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'photos',
        name: 'Photos',
        component: () => import('@/views/Photos.vue'),
        meta: { title: '照片' }
      },
      {
        path: 'timeline',
        name: 'Timeline',
        component: () => import('@/views/Timeline.vue'),
        meta: { title: '时间线' }
      },
      {
        path: 'map',
        name: 'Map',
        component: () => import('@/views/Map.vue'),
        meta: { title: '地图' }
      },
      {
        path: 'faces',
        name: 'Faces',
        component: () => import('@/views/Faces.vue'),
        meta: { title: '人脸' }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('@/views/Activities.vue'),
        meta: { title: '活动' }
      },
      {
        path: 'folders',
        name: 'Folders',
        component: () => import('@/views/Folders.vue'),
        meta: { title: '文件夹' }
      },
      {
        path: 'trash',
        name: 'Trash',
        component: () => import('@/views/Trash.vue'),
        meta: { title: '回收站' }
      },
      {
        path: 'upload',
        name: 'Upload',
        component: () => import('@/views/Upload.vue'),
        meta: { title: '上传' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth !== false && !userStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && userStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router
