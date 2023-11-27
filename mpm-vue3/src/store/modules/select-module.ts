import {defineStore} from 'pinia';
import {detailViewModuleStore, photoModuleStore} from "@/store";

export const selectModuleStore = defineStore('selectModule', {
  state: () => ({
    selectedIndexes: new Set<number>([]),
    lastSelectedIndex: -1,
  }),
  getters: {
    isSelected: (state) => {
      return (index: number) => {
        // console.log(state.selectedIndexes + ' index ' + index);
        return state.selectedIndexes.has(index);
      };
    }
  },
  actions: {
    clearSelect() {
      this.selectedIndexes.clear();
      this.lastSelectedIndex = -1;
    },
    selectIndex(index: number, metaKey: boolean, shiftKey: boolean) {
      if (this.lastSelectedIndex == null) this.lastSelectedIndex = -1;
      const selectedIndexes = this.selectedIndexes;
      console.log('last ' + this.lastSelectedIndex + ' selectedIndexes ' + selectedIndexes + ' index ' + index);
      if (shiftKey && this.lastSelectedIndex >= 0) {
        for (let i = Math.min(this.lastSelectedIndex, index); i <= Math.max(this.lastSelectedIndex, index); i++) {
          selectedIndexes.add(i);
        }
        this.lastSelectedIndex = index;
      } else if (metaKey) {
        this.lastSelectedIndex = index;
        if (selectedIndexes.has(index)) {
          selectedIndexes.delete(index);
        } else {
          selectedIndexes.add(index);
        }
      } else {
        selectedIndexes.clear();
        selectedIndexes.add(index);
        this.lastSelectedIndex = index;
      }
    },
    async changeSelectedIndex(delta: number, shiftKey: boolean) {
      const detailViewStore = detailViewModuleStore();
      const photoStore = photoModuleStore();

      console.log("from " + this.lastSelectedIndex + " with delta " + delta);
      let nextIndex = this.lastSelectedIndex + delta;
      if (nextIndex < 0) nextIndex = 0;
      if (nextIndex > photoStore.idList.length - 1) nextIndex = photoStore.idList.length - 1;
      console.log("changed to " + nextIndex);

      if (detailViewStore.detailVisible)
        await detailViewStore.showDetailView(nextIndex);

      this.selectIndex(nextIndex, false, shiftKey);
    }
  },
  persist: {
    afterRestore: (ctx) => {
      console.log(`just restored '${ctx.store.$id}'`);
      ctx.store.selectedIndexes = new Set([]);
    }
  },
});
