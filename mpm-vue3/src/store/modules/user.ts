import {defineStore} from 'pinia';
import {store} from '@/store';
import {checkPassword} from "@/api/users";


export const useUserStore = defineStore('user', {
  state: () => ({
    account: '',
    name: '',
    isAdmin: false,
  }),
  getters: {},
  actions: {
    async login(account: string, password: string) {
      let loginUserInfo = await checkPassword(account, password);
      if (loginUserInfo != null) {
        this.account = loginUserInfo.account;
        this.name = loginUserInfo.name;
        this.isAdmin = loginUserInfo.isAdmin;
      } else {
        throw '密码不正确，请重新输入';
      }
    },
    async logout() {
      this.account = '';
      this.isAdmin = false;
    },
  },
  persist: true,
});

export function getUserStore() {
  return useUserStore(store);
}
