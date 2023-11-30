import {defineStore} from 'pinia';
import {photoModuleStore} from "@/store/modules/photo-module";
import {selectModuleStore} from "@/store/modules/select-module";
import {getPhotoDetails} from "@/api/model/photos";


async function getPhotoItem(index: number) {
  const photoStore = photoModuleStore();
  return await photoStore.getPhotoById(photoStore.idList[index].id, index);
}

export const detailViewModuleStore = defineStore('detailViewModule', {
  state: () => ({
    detailVisible: false,
    detailTitle: "",
    currentPhoto: null,
  }),
  getters: {
    currentPhotoName(): string {
      return this.currentPhoto?.name;
    },
    currentPhotoSmallUrl(): string {
      return "/cos/small/" + this.currentPhoto?.name;
    }
  },
  actions: {
    async showDetailView(index: number) {
      const photoStore = photoModuleStore();
      const selectStore = selectModuleStore();
      if (photoStore.idList.length == 0) {
        this.detailVisible = false;
        return;
      }
      this.currentPhoto = await getPhotoItem(index);
      this.detailTitle = getPhotoDetails(this.currentPhoto);
      // TODO: 完善size和时间，以及格式
      // + `时间：${DateFormat('y/M/d').format(takenDate)}`;`;
      this.detailVisible = true;
      selectStore.selectIndex(index, false, false);
    }
  },
});
