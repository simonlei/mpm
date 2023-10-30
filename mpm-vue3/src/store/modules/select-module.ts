import {defineStore} from 'pinia';

export const selectModuleStore = defineStore('selectModule', {
  state: () => ({
    selectedIndexes: [],
    lastSelectedIndex: -1,
  }),
  getters: {},
  actions: {
    clearSelect() {
      // 暂时没用
      this.selectedIndexes = [];
      this.lastSelectedIndex = -1;
    },
    selectIndex(index: number, metaKey: boolean, shiftKey: boolean) {
      if (shiftKey && this.lastSelectedIndex >= 0) {
        for (let i = Math.min(this.lastSelectedIndex, index); i <= Math.max(this.lastSelectedIndex, index); i++) {
          this.selectedIndexes.push(i);
        }
        this.lastSelectedIndex = index;
      } else if (metaKey) {
        this.lastSelectedIndex = index;
        if (this.selectedIndexes.indexOf(index) > 0) {
          this.selectedIndexes.splice(this.selectedIndexes.indexOf(index));
        } else {
          this.selectedIndexes.push(index);
        }
      } else {
        this.selectedIndexes = [index];
        this.lastSelectedIndex = index;
      }
      // console.log(this.selectedIndexes + " last " + this.lastSelectedIndex);
    }
  },
  persist: true,
});
