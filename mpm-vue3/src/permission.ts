import {MessagePlugin} from 'tdesign-vue-next';
import NProgress from 'nprogress'; // progress bar
import 'nprogress/nprogress.css'; // progress bar style
import {getPermissionStore, getUserStore} from '@/store';
import router from '@/router';

NProgress.configure({showSpinner: false});

router.beforeEach(async (to, from, next) => {
  NProgress.start();

  const userStore = getUserStore();
  const permissionStore = getPermissionStore();
  const {whiteListRouters} = permissionStore;

  const {account, isAdmin} = userStore;
  if (account != '') {
    if (to.path === '/login') {
      next();
      return;
    }
    console.log('............. init route');
    if (permissionStore.inited) {
      next();
    } else {
      try {
        await permissionStore.initRoutes(isAdmin);

        if (router.hasRoute(to.name)) {
          next();
        } else {
          next(`/`);
        }
      } catch (error) {
        MessagePlugin.error(error);
        next({
          path: '/login',
          query: {redirect: encodeURIComponent(to.fullPath)},
        });
        NProgress.done();
      }
    }
  } else {
    /* white list router */
    if (whiteListRouters.indexOf(to.path) !== -1) {
      next();
    } else {
      next({
        path: '/login',
        query: {redirect: encodeURIComponent(to.fullPath)},
      });
    }
    NProgress.done();
  }
});

router.afterEach((to) => {
  if (to.path === '/login') {
    const userStore = getUserStore();
    const permissionStore = getPermissionStore();

    userStore.logout();
    permissionStore.restore();
  }
  NProgress.done();
});
