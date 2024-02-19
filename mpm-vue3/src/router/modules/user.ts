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
        meta: {title: '创建用户', roleCode: 'admin',},
      },
      {
        path: 'list',
        name: 'ListUser',
        component: () => import('@/pages/user/list.vue'),
        meta: {title: '用户列表', roleCode: 'admin',},
      },
      {
        path: 'edit/:id',
        name: 'EditUser',
        component: () => import('@/pages/user/edit.vue'),
        meta: {title: '编辑用户', roleCode: 'admin', hidden: true},
      },
    ],
  },
];
