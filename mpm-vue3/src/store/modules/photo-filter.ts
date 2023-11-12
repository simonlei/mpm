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
    },
    clear() {
      this.trashed = false;
      this.star = null;
      this.video = null;
      this.order = "-takenDate";
      this.dateKey = null;
      this.path = null;
      this.tag = null;
    }
  },
  persist: true,
});
