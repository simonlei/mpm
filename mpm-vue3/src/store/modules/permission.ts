import {defineStore} from 'pinia';
import {RouteRecordRaw} from 'vue-router';
import router, {asyncRouterList} from '@/router';
import {store} from '@/store';

function filterPermissionsRouters(routes: Array<RouteRecordRaw>) {
  const res = [];
  const removeRoutes = [];
  routes.forEach((route) => {
    const children = [];
    route.children?.forEach((childRouter) => {
      const roleCode = childRouter.meta?.roleCode || childRouter.name;
      console.log('roleCode ', roleCode, childRouter);
      if ('admin' == roleCode) { // 只有 roleCode == admin 的才需要 remove 掉
        removeRoutes.push(childRouter);
      } else {
        children.push(childRouter);
      }
    });
    if (children.length > 0) {
      route.children = children;
      res.push(route);
    }
  });
  return {accessedRouters: res, removeRoutes};
}

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    inited: false,
    whiteListRouters: ['/login'],
    routers: [],
    removeRoutes: [],
  }),
  actions: {
    async initRoutes(isAdmin: boolean) {
      console.log('init routes');
      let accessedRouters = [];

      let removeRoutes = [];
      // special token
      if (isAdmin) {
        accessedRouters = asyncRouterList;
      } else {
        const res = filterPermissionsRouters(asyncRouterList);
        accessedRouters = res.accessedRouters;
        removeRoutes = res.removeRoutes;
      }

      this.routers = accessedRouters;
      this.removeRoutes = removeRoutes;

      removeRoutes.forEach((item: RouteRecordRaw) => {
        if (router.hasRoute(item.name)) {
          router.removeRoute(item.name);
        }
      });
      this.inited = true;
    },
    async restore() {
      this.inited = false;
      this.removeRoutes.forEach((item: RouteRecordRaw) => {
        router.addRoute(item);
      });
    },
  },
});

export function getPermissionStore() {
  return usePermissionStore(store);
}
