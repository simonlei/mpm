import {defineStore} from 'pinia';
import {Photo} from "@/api/model/photos";
import {getPics} from "@/api/photos";

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
        if (i >= 0) state.loadingIndex.splice(i);
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
  actions: {},
  persist: false,
});
