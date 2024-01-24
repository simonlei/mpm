import {defineStore} from 'pinia';

export const photoFilterStore = defineStore('photoFilter', {
  state: () => {
    return {
      trashed: false,
      star: null,
      video: null,
      order: "-takenDate",
      dateKey: null,
      path: null,
      tag: null,
      faceId: null,
    }
  },
  getters: {},
  actions: {
    change(values: {}) {
      Object.assign(this, values);
      return values;
    },
  },
  persist: true,
});
