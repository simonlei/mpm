import {defineStore} from 'pinia';
import {TOKEN_NAME} from '@/config/global';
import {store, usePermissionStore} from '@/store';
import {getLoginUserInfo} from "@/api/users";

const InitUserInfo = {
  roles: [],
};

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_NAME), // || 'main_token', // 默认token不走权限
    userInfo: {...InitUserInfo},
  }),
  getters: {
    roles: (state) => {
      return state.userInfo?.roles;
    },
  },
  actions: {
    async login(userInfo: Record<string, unknown>) {
      console.log(userInfo);
      let loginUserInfo = await getLoginUserInfo(userInfo);
      if (loginUserInfo.ok == 'ok') {
        this.token = 'main_token';
      } else {
        throw '密码不正确，请重新输入';
      }
    },
    async getUserInfo() {
      const mockRemoteUserInfo = async (token: string) => {
        if (token === 'main_token') {
          return {
            name: 'td_main',
            roles: ['all'],
          };
        }
        return {
          name: 'td_dev',
          roles: ['UserIndex', 'DashboardBase', 'login'],
        };
      };

      const res = await mockRemoteUserInfo(this.token);

      this.userInfo = res;
    },
    async logout() {
      localStorage.removeItem(TOKEN_NAME);
      this.token = '';
      this.userInfo = {...InitUserInfo};
    },
    async removeToken() {
      this.token = '';
    },
  },
  persist: {
    afterRestore: (ctx) => {
      if (ctx.store.roles && ctx.store.roles.length > 0) {
        const permissionStore = usePermissionStore();
        permissionStore.initRoutes(ctx.store.roles);
      }
    },
  },
});

export function getUserStore() {
  return useUserStore(store);
}
