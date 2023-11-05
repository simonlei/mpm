import {defineStore} from 'pinia';
import {photoModuleStore} from "@/store/modules/photo-module";
import {selectModuleStore} from "@/store/modules/select-module";


async function getPhotoItem(index: number) {
  const photoStore = photoModuleStore();
  return await photoStore.getPhotoById(photoStore.idList[index].id, index);
}

export const detailViewModuleStore = defineStore('detailViewModule', {
  state: () => ({
    detailViewShowIndex: 0,
    detailVisible: false,
    detailImages: [],
  }),
  actions: {
    async showDetailView(index: number) {
      const photoStore = photoModuleStore();
      const selectStore = selectModuleStore();
      this.detailImages = [];
      if (photoStore.idList.length == 0) {
        this.detailVisible = false;
        return;
      }
      console.log("before " + this.detailImages);
      this.detailViewShowIndex = 0;
      if (index > 0) {
        this.detailImages.push("/cos/small/" + (await getPhotoItem(index - 1)).name);
        this.detailViewShowIndex = 1;
      }
      console.log("1 " + this.detailImages);
      this.detailImages.push("/cos/small/" + (await getPhotoItem(index)).name);
      console.log("2 " + this.detailImages);
      if (index < photoStore.idList.length - 1) {
        this.detailImages.push("/cos/small/" + (await getPhotoItem(index + 1)).name);
      }
      this.detailVisible = true;
      console.log("after " + this.detailImages);
      selectStore.selectIndex(index, false, false);
    }
  },
});
