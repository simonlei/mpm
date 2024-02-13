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
        page: 1,
        size: 100,
        total: 0,
        nameFilter: '',
      }
    },
    getters: {},
    actions: {
      async changeSelectedFace() {
        const result = await getFaces();
        this.faces = result['faces'];
        this.total = result['total'];
      },
    },
    persist: false,
  });
