import {defineStore} from 'pinia';

export const photoFilterStore = defineStore('photoFilter', {
  state: () => ({
    trashed: false,
    star: null,
    video: null,
    order: "-takenDate",
    dateKey: null,
    path: null,
    tag: null,
  }),
  getters: {},
  actions: {
    change(values: {}) {
      Object.assign(this, values);
    }
  },
  persist: true,
});
