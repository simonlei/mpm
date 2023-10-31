import {defineStore} from 'pinia';

export const selectModuleStore = defineStore('selectModule', {
  state: () => ({
    selectedIndexes: [],
    lastSelectedIndex: -1,
  }),
  getters: {
    isSelected: (state) => {
      return (index) => {
        if (index == null) return false;
        // console.log('index ' + index + ' state.selectedIndexes.indexOf(index)' + state.selectedIndexes.indexOf(index));
        return state.selectedIndexes.indexOf(index) >= 0
      };
    }
  },
  actions: {
    clearSelect() {
      this.selectedIndexes = [];
      this.lastSelectedIndex = -1;
    },
    selectIndex(index: number, metaKey: boolean, shiftKey: boolean) {
      if (this.lastSelectedIndex == null) this.lastSelectedIndex = -1;
      console.log('last ' + this.lastSelectedIndex + ' selectedIndexes ' + this.selectedIndexes + ' index ' + index);
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
