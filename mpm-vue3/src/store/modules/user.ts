import {defineStore} from 'pinia';
import {store} from '@/store';
import {checkPassword} from "@/api/users";


export const useUserStore = defineStore('user', {
  state: () => ({
    id: 0,
    account: '',
    name: '',
    isAdmin: false,
    signature: '',
  }),
  getters: {},
  actions: {
    async login(account: string, password: string) {
      let loginUserInfo = await checkPassword(account, password);
      if (loginUserInfo != null) {
        this.id = loginUserInfo.id;
        this.account = loginUserInfo.account;
        this.name = loginUserInfo.name;
        this.isAdmin = loginUserInfo.isAdmin;
        this.signature = loginUserInfo.signature;
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
