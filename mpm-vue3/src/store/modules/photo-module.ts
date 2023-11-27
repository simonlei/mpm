import {defineStore} from 'pinia';
import {Photo} from "@/api/model/photos";
import {getPics, trashPhotos} from "@/api/photos";
import {selectModuleStore} from "@/store/modules/select-module";

export const photoModuleStore = defineStore('photoModule', {
  state: () => ({
    idList: [],
    picsMap: new Map<number, Photo>(),
    loadingIndex: [],
    otherCount: 0,
  }),
  getters: {
    getPhotoById: (state) => {
      function isLoading(index) {
        let loading = false;
        state.loadingIndex.forEach((value: number) => {
          if (index >= value && index < value + 150) loading = true;
        });
        // console.log('loading ' + index + ' ' + loading);
        return loading;
      }

      function sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
      }

      async function loadingIndex(index) {
        let startIndex = Math.max(0, index - 50);
        state.loadingIndex.push(startIndex);
        const result = (await getPics(startIndex, 150));
        const loadData = result.data;
        loadData.forEach((value) => state.picsMap.set(value.id, value));
        var i = state.loadingIndex.indexOf(startIndex);
        if (i >= 0) state.loadingIndex.splice(i, 1);
      }

      return async (id, index) => {
        // console.log('id ' + id + ' index ' + index);
        while (state.picsMap.get(id) == null) {
          if (isLoading(index)) {
            await sleep(50);
            // await loadingIndex(index);
          } else {
            await loadingIndex(index);
          }
        }
        return state.picsMap.get(id);
      }

    },
  },
  actions: {
    async deleteSelectedPhotos() {
      const selectStore = selectModuleStore();

      let selectedIndexes = selectStore.selectedIndexes;
      let names = [];
      selectedIndexes.forEach((number) => {
        console.log(number + ":" + this.idList[number].id);
        let item = this.picsMap.get(this.idList[number].id);
        if (item != null)
          names.push(item.name);
      });
      await trashPhotos(names);

      let indexes = [...selectedIndexes].sort();
      for (let i = indexes.length - 1; i >= 0; i--) {
        this.idList.splice(indexes[i], 1);
        console.log(indexes[i]);
      }
      const index = Math.min(this.idList.length - 1, selectStore.lastSelectedIndex);
      selectStore.selectIndex(index, false, false);
    }
  },
  persist: false,
});
