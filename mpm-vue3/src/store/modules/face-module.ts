import {defineStore} from 'pinia';
import {FaceInfo} from "@/api/model/photos";
import {getFaces} from "@/api/photos";

export const faceModule = defineStore('faceModule',
  {
    state: () => {
      return {
        faces: [] as FaceInfo[],
      }
    },
    getters: {},
    actions: {
      async changeSelectedFace() {
        this.faces = await getFaces();
      },
    },
    persist: true,
  });
