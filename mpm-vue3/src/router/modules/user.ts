import Layout from "@/layouts/index.vue";
import {UserIcon} from "tdesign-icons-vue-next";

export default [
  {
    path: '/user',
    component: Layout,
    redirect: '/user/index',
    name: 'user',
    meta: {title: '用户', icon: UserIcon},
    children: [
      {
        path: 'index',
        name: 'UserIndex',
        component: () => import('@/pages/user/index.vue'),
        meta: {title: '个人中心'},
      },
      {
        path: 'create',
        name: 'CreateUser',
        component: () => import('@/pages/user/create.vue'),
        meta: {title: '创建用户'},
      },
    ],
  },
];
