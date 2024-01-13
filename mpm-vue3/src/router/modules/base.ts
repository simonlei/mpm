import Layout from '@/layouts/index.vue';
import DashboardIcon from '@/assets/assets-slide-dashboard.svg';

export default [
  {
    path: '/dashboard',
    component: Layout,
    redirect: '/dashboard/base',
    name: 'dashboard',
    meta: {title: '照片', icon: DashboardIcon},
    children: [
      {
        path: 'base',
        name: 'PhotoByDate',
        component: () => import('@/pages/dashboard/photos/index.vue'),
        meta: {title: '按时间查看'},
      },
      {
        path: 'folder',
        name: 'PhotoByFolder',
        component: () => import('@/pages/dashboard/photos/index2.vue'),
        meta: {title: '按相册查看'},
      },
      {
        path: 'map',
        name: 'PhotoByMap',
        component: () => import('@/pages/dashboard/photos/index3.vue'),
        meta: {title: '按地图查看'},
      },
    ],
  },
];
