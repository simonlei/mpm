import {defineStore} from 'pinia';

export const selectModuleStore = defineStore('selectModule', {
  state: () => ({
    selectedPhotoIds: [],
    selectedIndex: null,
  }),
  getters: {},
  actions: {
    clearSelect() {
      // 暂时没用
      this.selectedPhotoIds = [];
      this.selectedIndex = 0;
    },
    selectIndex(index: number) {
      this.selectedPhotoIds = [index];
      this.selectedIndex = index;
    }
  },
  persist: true,
});
