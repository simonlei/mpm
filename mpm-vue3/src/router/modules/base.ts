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
    ],
  },
];
