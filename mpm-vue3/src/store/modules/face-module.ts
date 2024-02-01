import {defineStore} from 'pinia';
import {FaceInfo, PhotoFace} from "@/api/model/photos";
import {getFaces} from "@/api/photos";

export const faceModule = defineStore('faceModule',
  {
    state: () => {
      return {
        faces: [] as FaceInfo[],
        showHidden: false,
        circleFace: false,
        photoFaces: [] as PhotoFace[],
      }
    },
    getters: {},
    actions: {
      async changeSelectedFace() {
        this.faces = await getFaces();
      },
    },
    persist: false,
  });
