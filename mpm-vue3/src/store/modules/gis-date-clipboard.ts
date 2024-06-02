import {defineStore} from "pinia";

export const gisDateClipboardStore = defineStore('gisDateClipboard',
  {
    state: () => {
      return {
        latitude: String,
        longitude: String,
        taken_date: Date,
      }
    },
    persist: true,
  });
