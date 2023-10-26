import {defineStore} from 'pinia';

export const selectModuleStore = defineStore('selectModule', {
  state: () => ({
    selectedPhotoIds: [],
    lastSelectedIndex: null,
  }),
  getters: {},
  actions: {
    clearSelect() {
      // 暂时没用
      this.selectedPhotoIds = [];
      this.lastSelectedIndex = null;
    },
    selectIndex(index: number) {
      this.selectedPhotoIds = [index];
      this.lastSelectedIndex = index;
    }
  },
  persist: true,
});
