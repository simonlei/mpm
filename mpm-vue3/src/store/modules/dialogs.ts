import {defineStore} from 'pinia';

export const dialogsStore = defineStore('dialogsStore', {
  state: () => {
    return {
      datePickerDlg: false,
      datePicked: null,
    }
  },
  getters: {},
  actions: {},
  persist: false,
});
